package rabbit.flt.test.cases;


import junit.framework.TestCase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rabbit.flt.common.Headers;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.exception.FltException;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.test.common.mybatis.UserMapper;
import rabbit.flt.test.common.spi.TestTraceHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public abstract class BaseCases {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected UserMapper userMapper;

    protected void mybatisPlusTest() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite::resource:db/app.s3db");
        try {
            Statement stmt = connection.createStatement();
            String table = "user";
            dropTable(stmt, table);
            createTable(stmt, table);
            stmt.execute("insert into ".concat(table).concat(" (id, user_name) values('1', 'zhang3')"));
            Semaphore semaphore = new Semaphore(0);
            Map<String, TraceData> map = new ConcurrentHashMap<>();
            TestTraceHandler.setDiscardDataHandler(d -> {
                logger.info("{}#{}, {}", d.getTraceId(), d.getSpanId(), d.getNodeName());
                map.put(d.getSpanId(), d);
                semaphore.release();
            });
            TraceContext.openTrace("manual");
            userMapper.plusGetById("1");
            TraceContext.clearContext();
            semaphore.acquire();
            TestCase.assertEquals("plusGetById", map.get("0").getNodeName());
            TestTraceHandler.setDiscardDataHandler(null);
            stmt.close();
        } finally {
            connection.close();
        }
    }

    private void dropTable(Statement stmt, String table) {
        try {
            stmt.execute("drop table ".concat(table));
        } catch (Exception e) {

        }
    }

    private void createTable(Statement stmt, String table) throws SQLException {
        stmt.execute("create table ".concat(table).concat(" (id varchar(10), user_name varchar(10))"));
    }

    /**
     * web flux 专用测试
     *
     * @throws Exception
     */
    protected void contextStatusTest() throws Exception {
        requestLocalhost(20, 50);
        Thread[] list = getThreads();
        for (Thread thread : list) {
            if (!thread.getName().startsWith("reactor-http-nio")) {
                continue;
            }
            String name = "traceOpenStatusContext";
            logger.info("thread [{}]", thread.getName());
            Object traceOpenStatus = readThreadLocal(thread, getStaticFieldValue(name));
            TestCase.assertNull(traceOpenStatus);
        }
    }

    private <T> ThreadLocal<T> getStaticFieldValue(String name) throws Exception {
        Field field = TraceContext.class.getDeclaredField(name);
        field.setAccessible(true);
        return (ThreadLocal<T>) field.get(null);
    }

    private <T> T readThreadLocal(Thread thread, ThreadLocal<T> threadLocal) throws Exception {
        Field threadLocals = Thread.class.getDeclaredField("threadLocals");
        threadLocals.setAccessible(true);
        Object threadLocalMap = threadLocals.get(thread);
        if (null != threadLocalMap) {
            Method getEntry = threadLocalMap.getClass().getDeclaredMethod("getEntry", ThreadLocal.class);
            getEntry.setAccessible(true);
            Object entry = getEntry.invoke(threadLocalMap, threadLocal);
            if (null != entry) {
                Field value = entry.getClass().getDeclaredField("value");
                value.setAccessible(true);
                return (T) value.get(entry);
            }
        }
        return null;
    }

    @NotNull
    private Thread[] getThreads() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] list = new Thread[Thread.activeCount()];
        group.enumerate(list);
        return list;
    }

    private void requestLocalhost(int threadCount, int loop) throws InterruptedException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            CountDownLatch cdl = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    for (int j = 0; j < loop; j++) {
                        try {
                            HttpGet httpGet = new HttpGet("http://localhost:8888/mvc/hello");
                            CloseableHttpResponse resp = client.execute(httpGet);
                            resp.close();
                        } catch (Exception e) {
                            throw new FltException(e);
                        }
                    }
                    cdl.countDown();
                }).start();
            }
            cdl.await();
        } finally {
            client.close();
        }
    }

    /**
     * 递归压栈测试
     */
    protected void pushStackTest() throws Exception {
        Semaphore semaphore = new Semaphore(0);
        List<TraceData> list = new ArrayList<>();
        TestTraceHandler.setDiscardDataHandler(t -> {
            if ("recursivelyAdd".equals(t.getNodeName()) || "count".equals(t.getNodeName())) {
                list.add(t);
                semaphore.release();
            }
        });
        CaseService caseService = new CaseService();
        int result = caseService.recursivelyAdd(3);
        TestCase.assertEquals(53, result);
        semaphore.acquire(6);
        TestCase.assertEquals("0", list.get(5).getSpanId());
        TestTraceHandler.setDiscardDataHandler(null);
    }

    protected void longTraceTest() throws Exception {
        Semaphore semaphore = new Semaphore(0);
        TestTraceHandler.setDiscardDataHandler(t -> {
            semaphore.release();
        });
        CaseService caseService = new CaseService();
        caseService.longTrace(101);
        semaphore.acquire(100);
        TestTraceHandler.setDiscardDataHandler(null);
    }

    protected void httpClientTest() throws Exception {
        Semaphore semaphore = new Semaphore(0);
        Map<String, TraceData> map = new ConcurrentHashMap<>();
        TestTraceHandler.setDiscardDataHandler(d -> {
            logger.info("traceData: {}#{}", d.getNodeName(), d.getSpanId());
            map.put(d.getSpanId(), d);
            semaphore.release();
        });
        CaseService caseService = new CaseService();
        caseService.doHttp3Request();
        semaphore.acquire(4);
        TestCase.assertEquals(4, map.size());
        TestCase.assertEquals("hello", map.get("0-0-0-0").getNodeName());
        TestCase.assertEquals("/mvc/hello", map.get("0-0-0").getNodeName());
        TestCase.assertEquals("HTTP_CLIENT3", map.get("0-0").getNodeName());
        TestCase.assertEquals("doHttp3Request", map.get("0").getNodeName());

        caseService.doHttp4Request();
        semaphore.acquire(4);
        TestCase.assertEquals(4, map.size());
        TestCase.assertEquals("hello", map.get("0-0-0-0").getNodeName());
        TestCase.assertEquals("/mvc/hello", map.get("0-0-0").getNodeName());
        Map<String, Object> headers = map.get("0-0-0").getHttpRequest().getHeaders();
        TestCase.assertTrue(headers.containsKey(Headers.TRACE_ID.toLowerCase()) || headers.containsKey(Headers.TRACE_ID));
        TestCase.assertTrue(headers.containsKey(Headers.SPAN_ID.toLowerCase()) || headers.containsKey(Headers.SPAN_ID));
        TestCase.assertEquals("HTTP_CLIENT4", map.get("0-0").getNodeName());
        TestCase.assertEquals("doHttp4Request", map.get("0").getNodeName());

        caseService.callError();
        semaphore.acquire(4);
        TestCase.assertEquals(4, map.size());
        TestCase.assertEquals("error", map.get("0-0-0-0").getNodeName());
        TestCase.assertTrue(map.get("0-0-0-0").getData().contains("rabbit.flt.common.exception.FltException: hello"));
        TestCase.assertTrue(map.get("0-0-0-0").getStatus() == TraceData.Status.ERR);
        TestCase.assertEquals("/mvc/error", map.get("0-0-0").getNodeName());
        headers = map.get("0-0-0").getHttpRequest().getHeaders();
        TestCase.assertTrue(headers.containsKey(Headers.TRACE_ID.toLowerCase()) || headers.containsKey(Headers.TRACE_ID));
        TestCase.assertTrue(headers.containsKey(Headers.SPAN_ID.toLowerCase()) || headers.containsKey(Headers.SPAN_ID));
        TestCase.assertEquals("HTTP_CLIENT4", map.get("0-0").getNodeName());
        TestCase.assertEquals("callError", map.get("0").getNodeName());
    }
}
