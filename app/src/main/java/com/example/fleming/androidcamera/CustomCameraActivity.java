package com.example.fleming.androidcamera;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomCameraActivity extends AppCompatActivity implements GestureDetectManager.OnCameraChangeListener {

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.bt_camera)
    Button btCamera;
    private static final String TAG = "fleming";
    private Camera mCamera;
    private GestureDetectorCompat mDetector;
    private static float FLIP_DISTANCE = 120;
    private Activity mActivity;
    private SurfaceHolder mHolder;
    private int cameraPosition = 1;
    private Camera currentCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        ButterKnife.bind(this);

        mActivity = this;

        initCamera();

        mHolder = surfaceView.getHolder();
        mHolder.addCallback(callback);

        mDetector = new GestureDetectorCompat(this, onGestureListener);

        mDetector.setOnDoubleTapListener(new MyDoubleTapListener());

        GestureDetectManager.getInstance(this).setCameraChangeListener(this);
    }

    private void initCamera() {
        currentCamera = Camera.open();
        startPreview();
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopPreview();
        }
    };

    @OnClick(R.id.bt_camera)
    public void onViewClicked() {
        currentCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    takePicture(camera);
                }
            }
        });
    }

    private void takePicture(Camera camera) {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    File file = File.createTempFile("img", "");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.flush();
                    fos.close();
                    back(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void back(File file) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("path", file.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void startPreview() {
        if (currentCamera != null) {
            try {
                currentCamera.setPreviewDisplay(mHolder);//通过surfaceview显示取景画面
                currentCamera.setDisplayOrientation(90);
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentCamera.startPreview();
        }
    }

    private void stopPreview() {
        if (currentCamera != null) {
            currentCamera.stopPreview();
            currentCamera.release();
            currentCamera = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                GestureDetectManager.getInstance(mActivity)
                        .handleSlide(Gesture.LEFT);
            }

            if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                GestureDetectManager.getInstance(mActivity)
                        .handleSlide(Gesture.RIGHT);
            }

            if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                GestureDetectManager.getInstance(mActivity).
                        handleSlide(Gesture.TOP);
            }

            if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                GestureDetectManager.getInstance(mActivity)
                        .handleSlide(Gesture.BOTTOM);
            }
            return true;
        }
    };

    @Override
    public void onChange() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        Log.d(TAG, "initCamera: cameraCount=" + cameraCount);

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 1) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    stopPreview();
                    currentCamera = Camera.open(i);//打开当前选中的摄像头
                    startPreview();
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    stopPreview();
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    startPreview();
                    cameraPosition = 1;
                    break;
                }
            }
        }
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown: ");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll: distanceX=" + distanceX + ", distanceY=" + distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: velocityX=" + velocityX + ", velocityY=" + velocityY);

            if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                Log.i(TAG, "向左滑...");
                return true;
            }
            if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                Log.i(TAG, "向右滑...");
                return true;
            }
            if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                Log.i(TAG, "向上滑...");
                return true;
            }
            if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                Log.i(TAG, "向下滑...");
                return true;
            }

            Log.d(TAG, e2.getX() + " " + e2.getY());
            return true;
        }
    }

    private class MyDoubleTapListener implements GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: ");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d(TAG, "onDoubleTapEvent: ");
            return true;
        }
    }
}
