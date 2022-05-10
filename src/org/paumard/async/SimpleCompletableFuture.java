package org.paumard.async;

import java.util.concurrent.CompletableFuture;

public class SimpleCompletableFuture {

	public static void main(String[] args) {

		CompletableFuture<Void> cf = new CompletableFuture<>();
		
		Runnable task = () -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			cf.complete(null);
		};
		CompletableFuture.runAsync(task);
		
		Void nil = cf.join();
		System.out.println("We are done");
	}
}
