package ch.hepia.iti.opencvnativeandroidstudio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class Labo2Activity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase _cameraBridgeViewBase;

    private int kernelSize = 3;
    private int thresholdType = 1;
    private int thresholdValue = 127;

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
        setContentView(R.layout.activity_labo2);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(Labo2Activity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        final LinearLayout kernelSizeLinearLayout = findViewById(R.id.kernel_size_linearlayout);
        kernelSizeLinearLayout.setVisibility(View.GONE);

        final Button kernelSizeButton = findViewById(R.id.change_kernel_size_button);
        kernelSizeButton.setVisibility(View.VISIBLE);

        _cameraBridgeViewBase = findViewById(R.id.main_surface);
        _cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        _cameraBridgeViewBase.setCvCameraViewListener(this);

        final TextView kernelSizeTextView = findViewById(R.id.kernel_size);

        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                kernelSize = progress * 2 + 3;
                kernelSizeTextView.setText(Integer.toString(kernelSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar thresholdSeekBar = findViewById(R.id.threshold_value_seekBar);
        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    thresholdValue = progress;
                    TextView tv = findViewById(R.id.threshold_value_textView);
                    tv.setText(Integer.toString(thresholdValue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final ConstraintLayout thresholdLayout = findViewById(R.id.threshold_layout);
        thresholdLayout.setVisibility(View.GONE);
        final Button thresholdButton = findViewById(R.id.threshold_button);
        thresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (thresholdLayout.getVisibility() == View.GONE)
                    thresholdLayout.setVisibility(View.VISIBLE);
                kernelSizeLinearLayout.setVisibility(View.GONE);
            }
        });

        kernelSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kernelSizeLinearLayout.setVisibility(View.VISIBLE);
                thresholdLayout.setVisibility(View.GONE);
            }
        });

        _cameraBridgeViewBase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (kernelSizeLinearLayout.getVisibility() == View.VISIBLE) {
                    kernelSizeLinearLayout.setVisibility(View.GONE);
                }
                if (thresholdLayout.getVisibility() == View.VISIBLE) {
                    thresholdLayout.setVisibility(View.GONE);
                }
                v.performClick();
                return Labo2Activity.super.onTouchEvent(event);
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.binary_radioButton:
                if (checked)
                    thresholdType = 0;
                break;
            case R.id.binary_inv_radioButton:
                if (checked)
                    thresholdType = 1;
                break;
            case R.id.truncate_radioButton:
                if (checked)
                    thresholdType = 2;
                break;
            case R.id.to_zero_radioButton:
                if (checked)
                    thresholdType = 3;
                break;
            case R.id.to_zero_inv_radioButton:
                if (checked)
                    thresholdType = 4;
                break;
        }
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
                    Toast.makeText(Labo2Activity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
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
        Mat matSrc = inputFrame.gray(); // image rgba
        Mat matDest0 = new Mat(matSrc.rows(), matSrc.cols(), matSrc.type());

        blur(matSrc.getNativeObjAddr(), matDest0.getNativeObjAddr(), kernelSize);

        Mat matDest1 = new Mat(matSrc.rows(), matSrc.cols(), matSrc.type());
        laplacian(matDest0.getNativeObjAddr(), matDest1.getNativeObjAddr(), kernelSize);

        Mat matDest2 = new Mat(matSrc.rows(), matSrc.cols(), matSrc.type());
        threshold(matDest1.getNativeObjAddr(), matDest2.getNativeObjAddr(), thresholdType, thresholdValue);
        return matDest2;
    }

    public native void blur(long matAddrGray, long retMat, int kernel_size);
    public native void laplacian(long matAddrGray, long retMat, int kernel_size);
    public native void threshold(long matAddrGray, long retMat, int threshold_type, int threshold_value);
}

