package rabbit.flt.common;

import rabbit.flt.common.exception.QueueFullException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 链表
 */
public class LinkedQueue<T> {

    /**
     * 值：需要取的数据条数
     */
    private Map<Thread, Integer> parkedThreads = new ConcurrentHashMap<>();

    // 容量
    private int capacity;

    // 存量数据个数
    private volatile int length;

    private InnerArrayList<T> list;

    private ReentrantLock lock = new ReentrantLock();

    public LinkedQueue(int capacity) {
        list = new InnerArrayList<>();
        this.capacity = capacity;
        this.length = 0;
    }

    /**
     * 添加数据
     *
     * @param data
     */
    public void add(T data) {
        try {
            lock.lock();
            if (this.capacity > length) {
                list.add(data);
                this.length++;
            } else {
                throw new QueueFullException();
            }
            wakeupThreads();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 添加数据
     *
     * @param data
     */
    public void addAll(List<? extends T> data) {
        try {
            lock.lock();
            if (this.capacity - this.length >= data.size()) {
                this.list.addAll(data);
                this.length += data.size();
            } else {
                throw new QueueFullException();
            }
            wakeupThreads();
        } finally {
            lock.unlock();
        }
    }

    private void wakeupThreads() {
        parkedThreads.forEach((thread, max) -> {
            if (this.length >= max) {
                LockSupport.unpark(thread);
            }
        });
    }

    /**
     * 获取数据
     *
     * @param data
     * @param maxElements
     * @param timeoutMils 最大等待时间
     * @return 读取的数据条数
     */
    public int drainTo(Collection<? super T> data, int maxElements, long timeoutMils) {
        if (timeoutMils <= 0) {
            return drainTo(data, maxElements);
        }
        if (0 != tryRead(data, maxElements)) {
            return data.size();
        }
        long duration = timeoutMils * 1000 * 1000;
        long begin = System.nanoTime();
        while (true) {
            parkedThreads.put(Thread.currentThread(), maxElements);
            LockSupport.parkNanos(duration);
            long now = System.nanoTime();
            long passedTime = now - begin;
            begin = now;
            if (passedTime >= duration) {
                parkedThreads.remove(Thread.currentThread());
                return drainTo(data, maxElements);
            } else {
                duration = duration - passedTime;
                if (0 != tryRead(data, maxElements)) {
                    parkedThreads.remove(Thread.currentThread());
                    return data.size();
                }
            }
        }
    }

    /**
     * 要么读满，要么一条也不读
     * @param data
     * @param maxElements
     * @return
     */
    private int tryRead(Collection<? super T> data, int maxElements) {
        try {
            lock.lock();
            if (this.length >= maxElements) {
                return drainTo(data, maxElements);
            }
        } finally {
            lock.unlock();
        }
        return 0;
    }

    private int drainTo(Collection<? super T> data, int maxElements) {
        try {
            lock.lock();
            if (this.length >= maxElements) {
                data.addAll(this.list.subList(0, maxElements));
                this.list.removeRange(0, maxElements);
                this.length -= maxElements;
                return maxElements;
            } else {
                data.addAll(this.list);
                this.length = 0;
                this.list.clear();
                return data.size();
            }
        } finally {
            lock.unlock();
        }
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    private class InnerArrayList<T> extends LinkedList<T> {
        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }
}
