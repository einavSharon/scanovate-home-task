package com.example.htscanovate.data.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.htscanovate.network.HTScanovateNetworkManager;
import com.example.htscanovate.network.request.HTScanovateRequest;
import com.example.htscanovate.network.request.HTTPBaseRequest;

import static com.example.htscanovate.ui.MainActivity.HTTPS_BTRUSTDEV_SCANOVATE_COM_API_INQUIRIES;

public class HTScanovateViewModel extends ViewModel {
    MutableLiveData<String> scanovateTaskResponse;
    //todo create network manager as singelton at HTScanovateApplication class
    private HTScanovateNetworkManager networkManager = new HTScanovateNetworkManager();
    private String lastJsonResponse;

    public MutableLiveData<String> getScanovateTaskResponse() {
        scanovateTaskResponse = new MutableLiveData<>();
        sendScanovateTask();
        return scanovateTaskResponse;
    }

    private void sendScanovateTask() {
        HTScanovateRequest request = new HTScanovateRequest(HTTPS_BTRUSTDEV_SCANOVATE_COM_API_INQUIRIES);
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
