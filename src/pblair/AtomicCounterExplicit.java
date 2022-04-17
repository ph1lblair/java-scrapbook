package pblair;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounterExplicit {
    private static class MyAtomicInteger extends AtomicInteger {
        private static Unsafe unsafe;
        static {
            Field unsafeField;
            try {
                unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = (Unsafe) unsafeField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private AtomicInteger counter = new AtomicInteger(0);

        public MyAtomicInteger(int counter) {
            super(counter);
        }

        public int myIncrementAndGet() {
            long valueOffset = 0L;
            try {
                valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            int v;
            do {
                v = unsafe.getIntVolatile(this, valueOffset);
                counter.incrementAndGet();
            } while (!unsafe.compareAndSwapInt(this,valueOffset, v, v + 1));
            return v;
        }

        public int getIncrements() {
            return counter.get();
        }
    }
    private static MyAtomicInteger counter = new MyAtomicInteger(0);

    public static void main(String[] args) {
        class Incrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1_000; i++) {
                    counter.myIncrementAndGet();
                }
            }
        }
        class Decrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1_000; i++) {
                    counter.decrementAndGet();
                }
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(8);
        List<Future> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++) {
                futures.add(service.submit(new Incrementer()));
            }
            for (int i = 0; i < 4; i++) {
                futures.add(service.submit(new Decrementer()));
            }
            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.getMessage());
                }
            });

            System.out.println("counter = " + counter);
            System.out.println("increments = " + counter.getIncrements());
        } finally {
            service.shutdown();
        }
    }

}
