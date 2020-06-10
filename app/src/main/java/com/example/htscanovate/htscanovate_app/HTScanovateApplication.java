package com.example.htscanovate.htscanovate_app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

public class HTScanovateApplication extends Application implements CameraXConfig.Provider{
    //todo init network
    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
