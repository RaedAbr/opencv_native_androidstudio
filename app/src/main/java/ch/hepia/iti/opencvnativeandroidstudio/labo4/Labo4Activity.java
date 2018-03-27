package ch.hepia.iti.opencvnativeandroidstudio.labo4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import ch.hepia.iti.opencvnativeandroidstudio.R;

public class Labo4Activity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    SeekBar hueLowSeekBar ;
    SeekBar hueHighSeekBar ;
    SeekBar saturationLowSeekBar ;
    SeekBar saturationHighSeekBar ;
    SeekBar lightnessLowSeekBar;
    SeekBar lightnessHightSeekBar;

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase _cameraBridgeViewBase;

    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load ndk built module, as specified in moduleName in build.gradle
                    // after opencv initialization
                    System.loadLibrary("native-lib");
                    _cameraBridgeViewBase.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_labo4);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(Labo4Activity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        _cameraBridgeViewBase = findViewById(R.id.main_surface);
        _cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        _cameraBridgeViewBase.setCvCameraViewListener(this);


        final ConstraintLayout propertiesLayout = findViewById(R.id.properties_layout);
        propertiesLayout.setVisibility(View.GONE);

        _cameraBridgeViewBase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (propertiesLayout.getVisibility() == View.VISIBLE) {
                    propertiesLayout.setVisibility(View.GONE);
                } else if (propertiesLayout.getVisibility() == View.GONE) {
                    propertiesLayout.setVisibility(View.VISIBLE);
                }
                return Labo4Activity.super.onTouchEvent(event);
            }
        });
        hueLowSeekBar = findViewById(R.id.HueLowSeekBar);
        hueHighSeekBar = findViewById(R.id.HueHighSeekBar);
        saturationLowSeekBar = findViewById(R.id.SaturationLowSeekBar);
        saturationHighSeekBar = findViewById(R.id.SaturationHighSeekBar);
        lightnessLowSeekBar = findViewById(R.id.LightnessLowSeekBar);
        lightnessHightSeekBar = findViewById(R.id.LightnessHightSeekBar);

        hueLowSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((TextView)findViewById(R.id.hltv)));
        hueHighSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((TextView)findViewById(R.id.hhtv)));
        saturationLowSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((TextView)findViewById(R.id.sltv)));
        saturationHighSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((TextView)findViewById(R.id.shtv)));
        lightnessLowSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((TextView)findViewById(R.id.lltv)));
        lightnessHightSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((TextView)findViewById(R.id.lhtv)));
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, _baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Labo4Activity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void disableCamera() {
        if (_cameraBridgeViewBase != null)
            _cameraBridgeViewBase.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat matAddrRGBA = inputFrame.rgba(); // image rgba
        Mat matDest = new Mat(matAddrRGBA.rows(), matAddrRGBA.cols(), matAddrRGBA.type());

        colorDetection(matAddrRGBA.getNativeObjAddr(), matDest.getNativeObjAddr(),
                hueLowSeekBar.getProgress(), hueHighSeekBar.getProgress(),
                saturationLowSeekBar.getProgress(), saturationHighSeekBar.getProgress(),
                lightnessLowSeekBar.getProgress(), lightnessHightSeekBar.getProgress());

        return matDest;
    }

    public native void colorDetection(long matAddrSrc, long matDest,
                                      int hueLow, int hueHigh, int satLow,
                                      int satHigh, int valueLow, int valueHigh);
}
