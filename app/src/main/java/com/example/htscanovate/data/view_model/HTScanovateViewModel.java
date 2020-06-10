package com.example.htscanovate.data.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.htscanovate.network.HTScanovateNetworkManager;
import com.example.htscanovate.network.request.HTScanovateRequest;
import com.example.htscanovate.network.request.HTTPBaseRequest;

public class HTScanovateViewModel extends ViewModel {
    public static final String TASK_URL = "https://btrustdev.scanovate.com/api/inquiries";
    MutableLiveData<String> scanovateTaskResponse;
    //todo create network manager as singleton at HTScanovateApplication class
    private HTScanovateNetworkManager networkManager = new HTScanovateNetworkManager();
    private String lastJsonResponse;

    public MutableLiveData<String> getScanovateTaskResponse() {
        scanovateTaskResponse = new MutableLiveData<>();
        sendScanovateTask();
        return scanovateTaskResponse;
    }

    private void sendScanovateTask() {
        HTScanovateRequest request = new HTScanovateRequest(TASK_URL);
        request.setParams(HTScanovateRequest.FLOW_ID_BODY_KEY, HTScanovateRequest.BODY_VALUE);
        request.setListener(new HTTPBaseRequest.HTTPBaseRequestListener() {
            @Override
            public void onRequestSuccess(String response) {
                lastJsonResponse = response;
                scanovateTaskResponse.postValue(response);
            }

            @Override
            public void onRequestFail() {
                //todo display error
                scanovateTaskResponse = null;
            }
        });
        networkManager.sendAsync(request);
    }

    public String getLastJsonResponse() {
        return lastJsonResponse;
    }
}
