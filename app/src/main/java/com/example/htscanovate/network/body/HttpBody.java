package com.example.htscanovate.network.body;

import java.io.OutputStream;
import java.util.HashMap;

public abstract class HttpBody {

    protected HashMap<String, String> bodyParams = new HashMap<String, String>();

    public HashMap<String, String> getBodyParams() {
        return bodyParams;
    }

    public void addBodyParam(String key, String value) {
        this.bodyParams.put(key, value);
    }

    /**
     * Abstract method for writing the HTTP body into a stream
     */
    public abstract void writeToStream(OutputStream outputStream);

    /**
     * Override this method to change the Content-Type header
     * @return
     */
    public String getCustomContentType() {
        return "multipart/form-data";
    }

    public String getContentAsString() { return  null; };

    public abstract byte[] getContentAsBytes();

    public long getContentLength() {
        return getContentAsBytes().length;
    }

    public boolean isEmpty() {
        return bodyParams == null || bodyParams.size() == 0;
    }
}
