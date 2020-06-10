package com.example.htscanovate.network.request;

import android.util.Xml;

import com.example.htscanovate.network.body.HttpBody;
import com.example.htscanovate.network.respone.HttpResponse;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class HTTPBaseRequest implements Comparable<HTTPBaseRequest> {

    public interface HTTPBaseRequestListener {
        void onRequestSuccess(String response);

        void onRequestFail();
    }
    public interface sessionExpiredListener {
         void onSessionExpired();
    }


    /**
     * Priority values.  Requests will be processed from higher priorities to
     * lower priorities, in FIFO order.
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    private Priority priority = Priority.NORMAL;
    private HTTPBaseRequestListener listener;
    private sessionExpiredListener sessionExpiredListener;

    private int requestId = 0;
    private int redirectionCount = 0;

    private String Url = null;

    //Request HTTP header
    protected HashMap<String, String> requestHeaders = new HashMap<String, String>();

    //Request query string params
    private HashMap<String, String> queryStringParams = new HashMap<String, String>();

    @Override
    public int compareTo(HTTPBaseRequest other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO ordering.
        return left == right ?
                this.requestId - other.requestId :
                right.ordinal() - left.ordinal();
    }

    //Body
    protected HttpBody bodyRequest = null;

    //Response handler
    protected HttpResponse responseHandler = null;

    //HTTP Response code
    protected int responseCode = 0;
    //HTTP Response Headers
    protected HashMap<String, List<String>> responseHeaders = new HashMap<String, List<String>>();

    private int retryCount = 0;

    public HTTPBaseRequest() {
        super();
    }

    public HTTPBaseRequest(String url) {
        super();
        Url = url;
        setUrl(Url);
    }

    /**
     * Abstract method which returns the body of the request
     *
     * @return NGSHttpBody classes
     */
    public abstract HttpBody getRequestBodyHandler();

    /**
     * Abstract method which return the parser of the request
     *
     * @return NGSHttpResponse classes
     */
    protected abstract HttpResponse getResponseHandler();

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public void setListener(HTTPBaseRequestListener listener) {
        this.listener = listener;
    }

    public HashMap<String, String> getQueryStringParams() {
        return queryStringParams;
    }

    public void addQueryStringParam(String key, String value) {
        this.queryStringParams.put(key, value);
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void addRequestHeader(String key, String value) {
        this.requestHeaders.put(key, value);
    }

    /**
     * The HTTP method, can be overridden in every request type
     */
    public String getMethod() {
        return "PUT";
    }

    /**
     * When the url is different from the pattern of other requests,
     * this method should return true.
     *
     * @return should the network manager build the url on it's own
     */
    public boolean shouldOverrideUrl() {
        return false;
    }

    /**
     * Building the URL of the request (including the query string)
     *
     * @throws MalformedURLException
     */
    public URL getURL(boolean includeQueryString) throws MalformedURLException {
        String urlToSend = getUrl();
        boolean hasQueryStringParams = queryStringParams != null && queryStringParams.size() > 0;
        if (includeQueryString && hasQueryStringParams) {
            StringBuilder queryString = new StringBuilder();
            Set<String> keySet = queryStringParams.keySet();
            for (String key : keySet) {
                String value = queryStringParams.get(key);
                try {
                    value = URLEncoder.encode(value, Xml.Encoding.UTF_8.name());
                    value = value.replace("%2C", ",");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                queryString.append(String.format("%s=%s&", key, value));
            }
            queryString.replace(queryString.length() - 1, queryString.length(), "");
            urlToSend = String.format("%s?%s", urlToSend, queryString.toString());
        }

        return new URL(urlToSend);
    }

    /**
     * Handling the response
     *
     * @param inputStream - The stream of the response
     */
    public void setResponseStream(InputStream inputStream) throws Exception {
        try {
            if (responseHandler == null) {
                responseHandler = getResponseHandler();
                String encoding = getEncoding();
                responseHandler.setEncoding(encoding);
            }
            if (responseHandler != null) {
                String response = responseHandler.parseStreamToString(inputStream);
                responseHandler.setResponse(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected String getEncoding() {
        return "utf-8";
    }

    public String getResponse() {
        if (responseHandler != null) {
            return responseHandler.getResponse();
        }
        return null;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void addResponseHeader(String key, String value) {
        List<String> list = this.responseHeaders.get(key);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(value);
        this.responseHeaders.put(key, list);

    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void addRetryCount() {
        this.retryCount++;
    }

    public void onRequestSuccess() {
        if (listener != null) {
            listener.onRequestSuccess(getResponse());
        }
    }

    public void onRequestFailed() {
        if (listener != null) {
            listener.onRequestFail();
        }
    }

    public void onRequestRedirect(String redirectedUrl) {
        redirectionCount++;
    }

    public int getRedirectionCount() {
        return redirectionCount;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {

        String className = this.getClass().getName();
        String method = getMethod();
        String url = "";
        try {
            url = getURL(true).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpBody bodyRequest = getRequestBodyHandler();
        boolean hasBody = bodyRequest != null && !bodyRequest.isEmpty();
        String bodyString = null;
        if (hasBody) {
            bodyString = bodyRequest.getContentAsString();
            hasBody = bodyString != null && bodyString.length() > 0;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[" + className + "] ");
        builder.append("(" + method + ")" + " ");
        builder.append(url + " ");
        if (hasBody) {
            builder.append("With Body {" + bodyString + "}");
        }

        return builder.toString();
    }

    public void setSessionExpiredListener(sessionExpiredListener sessionExpiredListener) {
        this.sessionExpiredListener = sessionExpiredListener;
    }

    public HTTPBaseRequest.sessionExpiredListener getSessionExpiredListener() {
        return sessionExpiredListener;
    }
}


