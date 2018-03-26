package ch.hepia.iti.opencvnativeandroidstudio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;

public class Labo3Activity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private enum FilterType {CANNY, HOUGH_LINES_P, HOUGH_CIRCLE, FIND_CONTOURS, HOUGH_LINES}

    private FilterType actualFilter = FilterType.CANNY;
    private ArrayList<Button> buttons = new ArrayList<>();
    private int lowThreshold = 100;

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
        setContentView(R.layout.activity_labo3);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(Labo3Activity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        _cameraBridgeViewBase = findViewById(R.id.main_surface);
        _cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        _cameraBridgeViewBase.setCvCameraViewListener(this);

        buttons.add((Button) findViewById(R.id.canny_button));
        buttons.add((Button) findViewById(R.id.hough_lines_P_button));
        buttons.add((Button) findViewById(R.id.hough_circle_button));
        buttons.add((Button) findViewById(R.id.find_contours_button));
        buttons.add((Button) findViewById(R.id.hough_lines_button));

        SeekBar minthresholdSeekBar = findViewById(R.id.min_threshold_seek_bar);
        minthresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lowThreshold = progress;
                    TextView tv = findViewById(R.id.min_threshold_text_view);
                    tv.setText(Integer.toString(lowThreshold));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final LinearLayout cannyLinearLayout = findViewById(R.id.canny_linearlayout);
        cannyLinearLayout.setVisibility(View.GONE);

        _cameraBridgeViewBase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (actualFilter) {
                    case CANNY :
                    case FIND_CONTOURS:
                        if (cannyLinearLayout.getVisibility() == View.VISIBLE) {
                            cannyLinearLayout.setVisibility(View.GONE);
                        } else if (cannyLinearLayout.getVisibility() == View.GONE) {
                            cannyLinearLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                    case HOUGH_LINES_P:
                    case HOUGH_CIRCLE:
                    case HOUGH_LINES:
                }
                v.performClick();
                return Labo3Activity.super.onTouchEvent(event);
            }
        });
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
                    Toast.makeText(Labo3Activity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
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

    public void onClickLabo3Button(View view) {
        for (Button b : buttons) {
            b.setBackgroundColor(0xffffffff);
        }
        view.setBackgroundColor(0xffa7a7a7);
        switch (view.getId()) {
            case R.id.canny_button:
                actualFilter = FilterType.CANNY;
                break;
            case R.id.hough_lines_P_button:
                actualFilter = FilterType.HOUGH_LINES_P;
                break;
            case R.id.find_contours_button:
                actualFilter = FilterType.FIND_CONTOURS;
                break;
            case R.id.hough_circle_button:
                actualFilter = FilterType.HOUGH_CIRCLE;
                break;
            case R.id.hough_lines_button:
                actualFilter = FilterType.HOUGH_LINES;
                break;
        }
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat matSrc = inputFrame.rgba(); // image rgba
        Mat matDest = new Mat(matSrc.rows(), matSrc.cols(), matSrc.type());
        switch (actualFilter) {
            case CANNY :
                canny(matSrc.getNativeObjAddr(), matDest.getNativeObjAddr(), lowThreshold);
                break;
            case HOUGH_LINES_P:
                houghLinesP(matSrc.getNativeObjAddr(), matDest.getNativeObjAddr());
                break;
            case HOUGH_CIRCLE:
                houghCircles(matSrc.getNativeObjAddr(), matDest.getNativeObjAddr());
                break;
            case FIND_CONTOURS:
                findContours(matSrc.getNativeObjAddr(), matDest.getNativeObjAddr(), lowThreshold);
                break;
            case HOUGH_LINES:
                houghLines(matSrc.getNativeObjAddr(), matDest.getNativeObjAddr());
                break;
        }
        return matDest;
    }

    public native void canny(long matSrc, long matDest, int lowThreshold);
    public native void houghLinesP(long matSrc, long matDest);
    public native void houghCircles(long matSrc, long matDest);
    public native void findContours(long matSrc, long matDest, int lowThreshold);
    public native void houghLines(long matSrc, long matDest);
}
