package pblair;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ExecutorExample1 {
    public static void main(String[] args) throws Exception {
        //Runnable task = () -> System.out.println("Thread: " + Thread.currentThread().getName());
        Callable<String> task = () -> {
            //Thread.sleep(300);
            //return "Thread: " + Thread.currentThread().getName();
            throw new IllegalStateException("Threw Exception in " + Thread.currentThread().getName());
        };
        // ExecutorService service = Executors.newSingleThreadExecutor();
        ExecutorService service = Executors.newFixedThreadPool(4);
        HashMap<String,String> map = new HashMap<>();
        try {
            for (int i = 0; i < 10; i++) {
                // new Thread(task).start();
                Future<String> future = service.submit(task);
                System.out.println(future.get(100, MILLISECONDS));
            }
        } finally {
            service.shutdown();
        }

    }
}
