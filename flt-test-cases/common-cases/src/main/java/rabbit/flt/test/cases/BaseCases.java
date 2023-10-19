package rabbit.flt.test.cases;


import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.test.common.mybatis.UserMapper;
import rabbit.flt.test.common.spi.TestTraceHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
}
