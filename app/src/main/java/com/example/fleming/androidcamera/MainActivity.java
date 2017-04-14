package com.example.fleming.androidcamera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.bt_system_camera)
    Button btSystemCamera;
    @BindView(R.id.bt_custom_camera)
    Button btCustomCamera;
    @BindView(R.id.imageView)
    ImageView imageView;
    private String filePath;
    private static final int REQUEST_SYSTEM_CAMERA = 100;
    private static final int REQUEST_CUSTOM_CAMERA = 101;

    @Override
    protected int addLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState) {
        ButterKnife.bind(this);

        checkPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @OnClick({R.id.bt_system_camera, R.id.bt_custom_camera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_system_camera:
                takePictureBySys();
                break;
            case R.id.bt_custom_camera:
                takePictureByCus();
                break;
        }
    }

    private void takePictureBySys() {

//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_SYSTEM_CAMERA);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
        Uri photoUri = Uri.fromFile(new File(filePath));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQUEST_SYSTEM_CAMERA);
    }

    private void takePictureByCus() {
        startActivityForResult(new Intent(this, CustomCameraActivity.class), REQUEST_CUSTOM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SYSTEM_CAMERA:
                    // 直接获取data内容，不过是编码过的图片，不清晰
//                Bundle extras = data.getExtras();
//                Bitmap bitmap = (Bitmap) extras.get("data");
//                imageView.setImageBitmap(bitmap);

                    // 通过文件的方式获取拍照后的照片，清晰
                    displayImage(filePath);
                    break;
                case REQUEST_CUSTOM_CAMERA:
                    String path = data.getStringExtra("path");
                    if (!TextUtils.isEmpty(path)) {
                        displayImage(path);
                    }
                    break;
            }
        }
    }

    private void displayImage(String path) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }
}
