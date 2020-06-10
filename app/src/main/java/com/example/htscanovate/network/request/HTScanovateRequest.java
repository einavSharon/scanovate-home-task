package com.example.htscanovate.network.request;

import com.example.htscanovate.network.body.HTScanovateHttpBody;
import com.example.htscanovate.network.body.HttpBody;
import com.example.htscanovate.network.respone.HttpResponse;

public class HTScanovateRequest extends HTTPBaseRequest {
    public static final String FLOW_ID_BODY_KEY = "flowId";
    public static final String BODY_VALUE = "2";

    public HTScanovateRequest(String url){
        super(url);
    }

    @Override
    public HttpBody getRequestBodyHandler() {
        if (bodyRequest == null) {
            bodyRequest = new HTScanovateHttpBody();
        }
        return bodyRequest;
    }

    @Override
    protected HttpResponse getResponseHandler() {
        return new HttpResponse();
    }

    public void setParams(String key, String value){
        HTScanovateHttpBody httpBody = (HTScanovateHttpBody) getRequestBodyHandler();
        httpBody.addBodyParam(key, value);
    }

    public String getMethod() {
        return "PUT";
    }
}
