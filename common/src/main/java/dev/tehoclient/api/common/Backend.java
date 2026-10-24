/*
 * This file is part of Teho Server API, licensed under the MIT License.
 *
 * Copyright (c) 2026 SGM Studios LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.tehoclient.api.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class Backend {
	public static final String DEFAULT_URL = "https://auth.tehoclientprod.com";
	private static final int MAX_RESPONSE = 1024 * 1024;

	public static final class Response {
		public final int status;
		public final Map<String, Object> body;

		Response(int status, Map<String, Object> body) {
			this.status = status;
			this.body = body;
		}

		public boolean ok() {
			return status >= 200 && status < 300;
		}

		public String error() {
			String error = Json.string(body, "error");
			return error.isEmpty() ? "HTTP " + status : error;
		}
	}

	private final ExecutorService pool = Executors.newFixedThreadPool(2, new ThreadFactory() {
		private int count;

		@Override
		public synchronized Thread newThread(Runnable task) {
			Thread thread = new Thread(task, "teho-api-backend-" + (++count));
			thread.setDaemon(true);
			return thread;
		}
	});
	private volatile String url = DEFAULT_URL;
	private volatile String key = "";

	public void configure(String url, String key) {
		String base = url == null || url.trim().isEmpty() ? DEFAULT_URL : url.trim();
		if (!base.startsWith("https://") && !base.startsWith("http://127.0.0.1") && !base.startsWith("http://localhost")) {
			throw new IllegalArgumentException("the Teho backend URL must be https");
		}
		this.url = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
		this.key = key == null ? "" : key.trim();
	}

	public boolean hasKey() {
		return !key.isEmpty();
	}

	public String url() {
		return url;
	}

	public CompletableFuture<Response> get(String path) {
		return call("GET", path, null);
	}

	public CompletableFuture<Response> post(String path, Map<String, Object> body) {
		return call("POST", path, body);
	}

	public CompletableFuture<Response> delete(String path) {
		return call("DELETE", path, null);
	}

	private CompletableFuture<Response> call(final String method, final String path, final Map<String, Object> body) {
		final CompletableFuture<Response> future = new CompletableFuture<>();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					future.complete(exchange(method, path, body));
				} catch (IOException | RuntimeException failed) {
					future.completeExceptionally(failed);
				}
			}
		});
		return future;
	}

	private Response exchange(String method, String path, Map<String, Object> body) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url + path).openConnection();
		connection.setInstanceFollowRedirects(false);
		connection.setConnectTimeout(8000);
		connection.setReadTimeout(12000);
		connection.setRequestMethod(method);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("User-Agent", "teho-server-sdk");
		if (!key.isEmpty()) {
			connection.setRequestProperty("Authorization", "Bearer " + key);
		}
		if (body != null) {
			byte[] payload = Json.write(body).getBytes(StandardCharsets.UTF_8);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setFixedLengthStreamingMode(payload.length);
			try (OutputStream out = connection.getOutputStream()) {
				out.write(payload);
			}
		}
		int status = connection.getResponseCode();
		InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
		String text = stream == null ? "" : read(stream);
		Map<String, Object> parsed;
		try {
			parsed = text.trim().isEmpty() ? Collections.<String, Object>emptyMap() : Json.object(text);
		} catch (IllegalArgumentException notJson) {
			parsed = Collections.<String, Object>singletonMap("error", text.length() > 200 ? text.substring(0, 200) : text);
		}
		return new Response(status, parsed);
	}

	private static String read(InputStream stream) throws IOException {
		try (InputStream in = stream) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
				if (out.size() > MAX_RESPONSE) {
					throw new IOException("backend response too large");
				}
			}
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	public void shutdown() {
		pool.shutdownNow();
	}
}
