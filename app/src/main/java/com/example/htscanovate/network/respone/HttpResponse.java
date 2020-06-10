package com.example.htscanovate.network.respone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpResponse{

    private String response = null;
    private String encoding = "utf-8";


    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public String parseStreamToString(InputStream inputStream) throws IOException {
        String encoding = getEncoding();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        inputStream.close();

        return stringBuilder.toString();
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
