package com.example.fleming.androidcamera;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.fleming.androidcamera.base.BaseActivity;
import com.example.fleming.androidcamera.util.PictureUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.video_view)
    VideoView videoView;
    private String filePath;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_VIDEO_CAPTURE = 101;
    private static final int REQUEST_CUSTOM_CAMERA = 102;

    @Override
    protected int addLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState) {
        ButterKnife.bind(this);

        checkPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.system_camera:
                displayDialog();
                return true;
            case R.id.custom_camera:
                startActivityForResult(new Intent(this, CustomCameraActivity.class), REQUEST_CUSTOM_CAMERA);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayDialog() {
        new AlertDialog.Builder(this)
                .setItems(getResources().getStringArray(R.array.mode),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        dispatchTakePictureIntent();
                                        break;
                                    case 1:
                                        dispatchTakeVideoIntent();
                                        break;
                                }
                            }
                        })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            String fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
            filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
            Uri photoUri = Uri.fromFile(new File(filePath));

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    // 获取缩略图
//                Bundle extras = data.getExtras();
//                Bitmap bitmap = (Bitmap) extras.get("data");
//                imageView.setImageBitmap(bitmap);

                    // 保存全尺寸图片
                    showImageView();
                    try {
                        FileInputStream fis = new FileInputStream(new File(filePath));
                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_VIDEO_CAPTURE:
                    showVideoView(View.VISIBLE, View.GONE);
                    Uri videoUri = data.getData();
                    videoView.setVideoURI(videoUri);
                    videoView.start();
                    break;
                case REQUEST_CUSTOM_CAMERA:
                    showImageView();
                    String path = data.getStringExtra("path");
                    displayImage(path);
                    break;
            }
        }
    }

    private void showVideoView(int visible, int gone) {
        videoView.setVisibility(visible);
        imageView.setVisibility(gone);
    }

    private void showImageView() {
        showVideoView(View.GONE, View.VISIBLE);
    }

    private void displayImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);

            int degree = PictureUtils.getBitmapDegree(path);
            Log.d("fleming", "displayImage: degree=" + degree);

            Bitmap bitmap1 = null;
            switch (degree) {
                case 0:
                    bitmap1 = PictureUtils.rotateBitmapByDegree(bitmap, 90);
                    break;
                case 90:
//                    bitmap1 = PictureUtils.rotateBitmapByDegree(bitmap, -90);
                    break;
                case 180:
//                    bitmap1 = PictureUtils.rotateBitmapByDegree(bitmap, 180);
                    break;
                case 270:
//                    bitmap1 = PictureUtils.rotateBitmapByDegree(bitmap, 90);
                    break;
            }
            imageView.setImageBitmap(bitmap1);
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
        }
    }
}
