package com.example.htscanovate.network;

import android.util.Log;

import com.example.htscanovate.network.body.HttpBody;
import com.example.htscanovate.network.request.HTTPBaseRequest;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

public class HTScanovateNetworkManager implements HTTPBaseRequest.sessionExpiredListener {
    public static final String HTSCANOVATE_NETWORK = "HTScanovateNetwork";
    /***
     * Max retries when the request fails
     */
    protected static int MAX_RETRY_COUNT = 3;
    protected static int TIMEOUT = 60 * 1000;
    protected static int READ_TIMEOUT = 60 * 1000;
    protected static int MAX_CONCURRENT_REQUESTS = 3;
    protected static int MAX_REDIRECTIONS = 10;

    private int requestId = 0;

    private final PriorityBlockingQueue<HTTPBaseRequest> waitingQueue = new PriorityBlockingQueue<>();
    private final PriorityBlockingQueue<HTTPBaseRequest> onGoingQueue = new PriorityBlockingQueue<>();

    protected int timeout = TIMEOUT;
    protected int readTimeout = READ_TIMEOUT;
    protected int concurrentRequest = MAX_CONCURRENT_REQUESTS;

    protected CookieManager cookieManager = null;

    public HTScanovateNetworkManager() {
        this.cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        startAsyncSender();
    }

    private void startAsyncSender() {
        Thread senderThread = new Thread() {
            @Override
            public void run() {
                super.run();

                while (true) {
                    try {
                        if (onGoingQueue.size() < concurrentRequest) {
                            HTTPBaseRequest request = waitingQueue.take();
                            sendRequestInNewThread(request);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        senderThread.setName("Async Sender");
        senderThread.start();
    }


    /***
     * Creates a new thread and sends the request
     * Response and parsing are on the same thread.
     * @param httpRequestBase - Request to be sent
     */
    public void sendAsync(final HTTPBaseRequest httpRequestBase) {
        synchronized (this) {
            requestId++;
            httpRequestBase.setRequestId(requestId);
        }

        waitingQueue.add(httpRequestBase);
    }

    private void sendRequestInNewThread(final HTTPBaseRequest httpRequestBase) {
        onGoingQueue.add(httpRequestBase);
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpRequestBase.setSessionExpiredListener(HTScanovateNetworkManager.this);

                boolean isSuccess = sendHttpRequest(httpRequestBase);
                if (isSuccess) {
                    httpRequestBase.onRequestSuccess();
                }
                else {
                    httpRequestBase.onRequestFailed();
                }
                onGoingQueue.remove(httpRequestBase);
            }
        }).start();
    }

    /***
     * Sends the request on the same thread which the method
     * was called on.
     * @param httpRequestBase - Request to be sent
     * @return success or fail
     */
    public boolean sendSync(HTTPBaseRequest httpRequestBase) {
        boolean result = sendHttpRequest(httpRequestBase);

        if (result) {
            httpRequestBase.onRequestSuccess();
        } else {
            httpRequestBase.onRequestFailed();
        }

        return result;
    }

    private boolean sendHttpRequest(HTTPBaseRequest httpRequestBase) {
        boolean isSuccess = false;

        int retryCount = 0;

        //While request hasn't succeeded or retry count reached
        while (!isSuccess && retryCount < MAX_RETRY_COUNT) {

            try {
                httpRequestBase.addRetryCount();
                retryCount = httpRequestBase.getRetryCount();

                onWillBuildUrl(httpRequestBase);
                //Sending the HTTP request
                URL url = httpRequestBase.getURL(true);

                onWillSendHttpRequest(httpRequestBase);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(readTimeout);
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestMethod(httpRequestBase.getMethod());

                Log.d(HTSCANOVATE_NETWORK, "Sending " + httpRequestBase.toString());

                HttpBody bodyRequest = httpRequestBase.getRequestBodyHandler();
                boolean hasBody = bodyRequest != null && !bodyRequest.isEmpty();
                if (hasBody) {
                    urlConnection.setDoOutput(true);
                    urlConnection.addRequestProperty("Authorization", "Bearer 2d4d1ba6-3cc5-47d7-999d-b2f20306a2f5");
                    urlConnection.addRequestProperty("Content-Type", bodyRequest.getCustomContentType());
                    BufferedOutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    bodyRequest.writeToStream(outputStream);

                    outputStream.flush();
                    outputStream.close();
                }

                urlConnection.connect();
                extractResponseHeaders(httpRequestBase, urlConnection);

                int responseCode = urlConnection.getResponseCode();
                if (shouldRedirect(responseCode)) {
                    urlConnection = handleRedirection(httpRequestBase, urlConnection);
                }

                //Reading the response
                InputStream inputStream;

                if (responseCode <= 200 && responseCode <= 299) {
                    isSuccess = true;
                    inputStream = urlConnection.getInputStream();
                    httpRequestBase.setResponseStream(inputStream);
                } else {
                    inputStream = urlConnection.getErrorStream();
                    httpRequestBase.setResponseStream(inputStream);
                }


                //Setting the response in the request
                httpRequestBase.setResponseCode(responseCode);

                urlConnection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                httpRequestBase.onRequestFailed();
            }
        }

        return isSuccess;
    }

    protected void onWillBuildUrl(HTTPBaseRequest httpRequestBase) {

    }

    protected void onWillSendHttpRequest(HTTPBaseRequest httpRequestBase) {

    }

    public boolean shouldRedirect(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP;
    }

    public HttpURLConnection handleRedirection(HTTPBaseRequest httpRequestBase, HttpURLConnection originUrlConnection) throws Exception {

        HttpURLConnection lastUrlConnection = null;

        try {
            String location = originUrlConnection.getHeaderField("Location");
            URL base = originUrlConnection.getURL();
            URL next = new URL(base, location);
            Log.d("Network", "Redirection --> " + next.toString());

            extractResponseHeaders(httpRequestBase, originUrlConnection);
            httpRequestBase.onRequestRedirect(next.toString());

            String externalForm = next.toExternalForm();
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(externalForm).openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setRequestMethod("POST");
            addRequestHeaders(httpRequestBase, urlConnection);

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            extractResponseHeaders(httpRequestBase, urlConnection);

            int redirectionCount = httpRequestBase.getRedirectionCount();
            if (redirectionCount > MAX_REDIRECTIONS) {
                throw new ProtocolException("Too many redirections");
            }

            if (shouldRedirect(responseCode)) {
                lastUrlConnection = handleRedirection(httpRequestBase, urlConnection);
                urlConnection.disconnect();
            } else {
                lastUrlConnection = urlConnection;
            }

            return lastUrlConnection;
        } catch (Exception exception) {
            throw exception;
        }
    }

    private void addRequestHeaders(HTTPBaseRequest httpRequestBase,
                                   HttpURLConnection urlConnection) {
        HashMap<String, String> requestHeaders = httpRequestBase.getRequestHeaders();
        Set<String> headerKeysList = requestHeaders.keySet();

        for (String headerKey : headerKeysList) {
            String value = requestHeaders.get(headerKey);
            urlConnection.setRequestProperty(headerKey, value);
        }
    }

    private void extractResponseHeaders(HTTPBaseRequest httpRequestBase, HttpURLConnection urlConnection) {
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        if (headerFields != null) {
            Set<String> headerKeysList = headerFields.keySet();

            for (String headerKey : headerKeysList) {
                List<String> headerValues = headerFields.get(headerKey);
                for (String value : headerValues) {
                    httpRequestBase.addResponseHeader(headerKey, value);
                }
            }
        }
    }

    @Override
    public void onSessionExpired() {

    }
}