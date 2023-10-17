package rabbit.flt.common.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.LinkedQueue;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(JUnit4.class)
public class QueueTest {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    @Test
    public void queueTest() throws Exception {
        LinkedQueue<String> pbq = new LinkedQueue<>(2000);
        pbq.drainTo(new ArrayList<>(), 10, 0);
        AtomicLong counter = new AtomicLong(0);
        Semaphore semaphore = new Semaphore(0);
        int threadCount = 50;
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                while (true) {
                    List<String> c = new ArrayList<>();
                    int count = pbq.drainTo(c, 20, 100);
                    if (0 == count) {
                        if (1000 == counter.get()) {
                            semaphore.release();
                            return;
                        }
                        continue;
                    } else {
                        counter.addAndGet(count);
                        logger.info("consume: {}, total: {}", count, counter.get());
                    }
                }
            }).start();
        }

        for (int i = 0; i < 100; i++) {
            List<String> list = new ArrayList<>();
            list.add("d1");
            list.add("d2");
            list.add("d3");
            list.add("d4");
            pbq.add("d5");
            pbq.add("c1");
            pbq.add("c2");
            pbq.add("c3");
            pbq.add("c4");
            pbq.add("c5");
            pbq.addAll(list);
        }
        semaphore.acquire();
    }
}
