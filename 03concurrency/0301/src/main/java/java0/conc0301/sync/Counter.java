package java0.conc0301.sync;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private AtomicInteger sum = new AtomicInteger(0);
    public void incr() {
        sum.addAndGet(1);
    }
    public int getSum() {
        return sum.intValue();
    }
    
    public static void main(String[] args) throws InterruptedException {
        int loop = 10000;
        
        // test single thread
        Counter counter = new Counter();
        for (int i = 0; i < loop; i++) {
            counter.incr();
        }
        System.out.println("single thread: " + counter.getSum());
    
        // test multiple threads
        final Counter counter2 = new Counter();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < loop / 2; i++) {
                counter2.incr();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < loop / 2; i++) {
                counter2.incr();
            }
        });
        t1.start();
        t2.start();
        //Thread.sleep(300);
        while (Thread.activeCount()>2){//当前线程的线程组中的数量>2
            Thread.yield();
        }
        System.out.println("multiple threads: " + counter2.getSum());
    
    
    }
}
