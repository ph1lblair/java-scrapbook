package org.paumard.async;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientExample {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		HttpClient client = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.build();

		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("https://www.amazon.com"))
				.build();

		CompletableFuture<Void> start = new CompletableFuture<>();

		CompletableFuture<HttpResponse<String>> future = 
				start.thenCompose(nil -> 
					client.sendAsync(request,
								     HttpResponse.BodyHandlers.ofString()));
		
		
		future.thenAcceptAsync(
				response -> {
					String body = response.body();
					System.out.println("body = " + body.length() + " [" + Thread.currentThread().getName() + "]");
				}, executor)
			  .thenRun(() -> System.out.println("Done!"));
		
		start.complete(null);

		Thread.sleep(5000);
		
		executor.shutdown();
	}
}
