package pblair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AtomicCounter {

    private static int counter = 0;
    public static void main(String[] args) {
        class Incrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1_000; i++) {
                    counter++;
                }
            }
        }
        class Decrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1_000; i++) {
                    counter--;
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
        } finally {
            service.shutdown();
        }

    }
}
