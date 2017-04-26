package com.example.fleming.androidcamera.lib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

/**
 * GestureDetectManager
 * Created by fleming on 17-4-14.
 */

public class GestureDetectManager {

    private static GestureDetectManager sManager;
    private static Context sContext;

    public GestureDetectManager(Context context) {
        sContext = context;
    }

    public static GestureDetectManager getInstance(Context context) {
        if (sManager == null) {
            synchronized (GestureDetectManager.class) {
                if (sManager == null) {
                    sManager = new GestureDetectManager(context);
                }
            }
        }
        return sManager;
    }

    public void handleSlide(Gesture gesture) {
        switch (gesture) {
            case LEFT:
                openSetting();
                break;
            case RIGHT:
                openModeSetting();
                break;
            case TOP:
                openAlbum();
                break;
            case BOTTOM:
                toggleCamera();
                break;
        }
    }

    /**
     * 左滑打开系统设置
     */
    private void openSetting() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        sContext.startActivity(intent);
    }

    private void openModeSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:"+ sContext.getPackageName()));
        sContext.startActivity(intent);
    }

    /**
     * 打开系统相册
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        sContext.startActivity(intent);
    }

    private void toggleCamera() {
        if (listener != null) {
            listener.onChange();
        }
    }

    private OnCameraChangeListener listener;

    public void setCameraChangeListener(OnCameraChangeListener listener) {
        this.listener = listener;
    }

    public interface OnCameraChangeListener {
        void onChange();
    }
}
