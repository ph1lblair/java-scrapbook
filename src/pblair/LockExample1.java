package pblair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockExample1 {

    public static void main(String[] args) throws InterruptedException {
        List<Integer> buffer = new ArrayList<>();

        Lock lock = new ReentrantLock();
        Condition isEmpty = lock.newCondition();
        Condition isFull = lock.newCondition();

        class Consumer implements Callable<String> {
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {
                    try {
                        lock.lock();
                        while (buffer.isEmpty()) {
                            if (!isEmpty.await(10, TimeUnit.MILLISECONDS)) {
                                throw new TimeoutException("Cosumer timed out");
                            }
                        }
                        buffer.remove(buffer.size()-1);
                        isFull.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }

                return "Consumed " + (count - 1);
            }
        }

        class Producer implements Callable<String> {
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {
                    try {
                        lock.lock();
                        // int i = 10/0;
                        while (buffer.size() >= 50) {
                            isFull.await();
                        }
                        buffer.add(1);
                        isEmpty.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
                return "Produced " + (count - 1);
            }
        }
        List<Producer> producers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            producers.add(new Producer());
        }
        List<Consumer> consumers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            consumers.add(new Consumer());
        }
        System.out.println("Producers and Consumers launched");
        List<Callable<String>> tasks = new ArrayList<>();
        tasks.addAll(producers);
        tasks.addAll(consumers);

        ExecutorService service = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> futures = service.invokeAll(tasks);
            futures.forEach(future -> {
                try {
                    System.out.println(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.getMessage());
                }
            });
        } finally {
            System.out.println("Sevice shutdown");
            service.shutdown();
        }
    }
}
