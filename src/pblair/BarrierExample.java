package pblair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class BarrierExample {
    public static void main(String[] args) {
        class Friend implements Callable<String> {
            private CyclicBarrier barrier;
            public Friend(CyclicBarrier barrier) {
                this.barrier = barrier;
            }
            @Override
            public String call() throws Exception {
                Random rand = new Random();
                Thread.sleep(rand.nextInt(20) * 100 + 100);
                System.out.println("I just arrived, waiting for others...");

                try {
                    barrier.await();
                    System.out.println("Let's go to the cinema");
                    return "Ok";
                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                }
                return "Not Ok";
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(4, () -> System.out.println("Barrier opening ..."));
        List<Future<String>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++) {
                Friend friend = new Friend(barrier);
                futures.add(service.submit(friend));
            }
            futures.forEach(future -> {
                try {
                    future.get(400, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.getMessage());
                } catch (TimeoutException e) {
                    System.out.println("Timed out");
                    future.cancel(true);
                }
            });
        } finally {
            service.shutdown();
        }
    }
}
