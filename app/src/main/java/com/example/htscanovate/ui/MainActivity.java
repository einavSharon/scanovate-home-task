package com.example.htscanovate.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.htscanovate.R;
import com.example.htscanovate.network.HTScanovateNetworkManager;
import com.example.htscanovate.network.request.HTScanovateRequest;
import com.example.htscanovate.network.request.HTTPBaseRequest;

public class MainActivity extends AppCompatActivity {

    public static final String HTTPS_BTRUSTDEV_SCANOVATE_COM_API_INQUIRIES = "https://btrustdev.scanovate.com/api/inquiries";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, HTScanovateMainActivity.class);
        startActivity(intent);
    }
//        HTScanovateNetworkManager networkManager = new HTScanovateNetworkManager();
//
//        HTScanovateRequest request = new HTScanovateRequest(HTTPS_BTRUSTDEV_SCANOVATE_COM_API_INQUIRIES);
//        request.setParams(HTScanovateRequest.FLOW_ID_BODY_KEY, HTScanovateRequest.BODY_VALUE);
//        request.setListener(new HTTPBaseRequest.HTTPBaseRequestListener() {
//            @Override
//            public void onRequestSuccess(String response) {
//                String res = response;
//            }
//
//            @Override
//            public void onRequestFail( ) {
//                Exception e = new Exception();
//            }
//        });
//        networkManager.sendAsync(request);
//    }
}
