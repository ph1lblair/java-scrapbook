package pblair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ProducerConsumer {
    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(50);

        class Consumer implements Callable<String> {
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {
                    queue.take();
                }
                return "Consumed " + (count - 1);
            }
        }
        class Producer implements Callable<String> {
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {
                    queue.put(Integer.toString(count));
                }
                return "Produced " + (count - 1);
            }
        }
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            tasks.add(new Producer());
        }
        for (int i = 0; i < 2; i++) {
            tasks.add(new Consumer());
        }
        System.out.println("Producers and Consumers launched");
        ExecutorService service = Executors.newFixedThreadPool(4);
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
            service.shutdown();
        }
    }
}
