package com.foo.durian.io;

import com.foo.durian.json.JsonUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * created by wuyuejia at 2018/9/28 下午5:17
 */
public class HttpClientUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    private static CloseableHttpAsyncClient asyncClient;
    private static int poolSize = 100;
    private static int maxPerRoute = 100;
    private static int connectionTimeout = 60;
    private static int socketTimeout = 60;


    public <T> T post(String url, String requestBody, int timeout, TimeUnit timeUnit, Class<T> clazz) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return JsonUtil.jsonToObject(post(url, requestBody, timeout, timeUnit), clazz);
    }

    /**
     * 同步
     */
    public static String post(String url, String requestBody, int timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        Future<HttpResponse> responseFuture = asyncClient.execute(post, null);
        HttpResponse httpResponse = responseFuture.get(timeout, timeUnit);
        return getResult(httpResponse);
    }

    /**
     * 异步
     */
    public static void asyncPost(String url, String requestBody, FutureCallback<HttpResponse> callback) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        asyncClient.execute(post, callback);
    }

    public static String getResult(HttpResponse response) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private HttpClientUtils() {
    }

    public static CloseableHttpAsyncClient build(int poolSize, int maxPerRoute, int connectionTimeout, int socketTimeout) throws IOReactorException {
        if (hasInit()) {
            return asyncClient;
        }
        HttpClientUtils.poolSize = poolSize;
        HttpClientUtils.maxPerRoute = maxPerRoute;
        HttpClientUtils.connectionTimeout = connectionTimeout;
        HttpClientUtils.socketTimeout = socketTimeout;
        return build();
    }

    public static CloseableHttpAsyncClient build() throws IOReactorException {
        if (hasInit()) {
            return asyncClient;
        }
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).setConnectTimeout(connectionTimeout).setSoTimeout(socketTimeout).build();
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(poolSize);
        connManager.setDefaultMaxPerRoute(maxPerRoute);
        asyncClient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(connManager).build();
        return asyncClient;
    }

    public static void destroy() throws IOException {
        if (hasInit()) {
            asyncClient.close();
        }
    }

    private static boolean hasInit() {
        return asyncClient != null;
    }


}
