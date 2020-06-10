package com.example.htscanovate.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.htscanovate.R;
import com.example.htscanovate.data.view_model.HTScanovateViewModel;

public class HTScanovateMainActivity extends AppCompatActivity {
    private ImageButton openCameraButton;
    private TextView textView;
    private HTScanovateViewModel viewModel;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        init();
    }

    private void init(){
        viewModel = new ViewModelProvider(this).get(HTScanovateViewModel.class);
        setContentView(R.layout.home_task_scanovate_activity_main);
        openCameraButton = findViewById(R.id.cameraButton);
        textView = findViewById(R.id.textView);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraActivity();
            }
        });
        if(viewModel.getLastJsonResponse() != null && !viewModel.getLastJsonResponse().isEmpty() && !viewModel.getLastJsonResponse().equals("")){
            textView.setText(viewModel.getLastJsonResponse());
        }
    }

    private void  openCameraActivity(){
        Intent intent = new Intent(this, HTScanovateCameraActivity.class);
        startActivity(intent);
        sendScanovateTask();
    }

    private void sendScanovateTask(){
        viewModel.getScanovateTaskResponse().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });
    }
}
