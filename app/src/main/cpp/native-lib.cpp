#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

extern "C"
{
// labo1 /////////////////////////////////////////////////////////////////////
void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo1Activity_salt(
        JNIEnv *env, jobject instance,
        jlong matAddrGray, jint nbrElem) {

    Mat &mGr = *(Mat *) matAddrGray;
    for (int k = 0; k < nbrElem; k++) {
        int i = rand() % mGr.cols;
        int j = rand() % mGr.rows;
        mGr.at<uchar>(j, i) = 255;
    }
}

void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo1Activity_binary(
        JNIEnv *env, jobject instance,
        jlong matAddrGray) {

    Mat &mGr = *(Mat *) matAddrGray;
    for (int i = 0; i < mGr.rows; i++) {
        for (int j = 0; j < mGr.cols; j++) {
            mGr.at<uchar>(i, j) = (uchar) (mGr.at<uchar>(i, j) < 127 ? 0 : 255);
        }
    }
}

Mat &ScanImageAndReduceIterator(Mat &I, jint n) {
    // accept only char type matrices
    CV_Assert(I.depth() == CV_8U);

    const int channels = I.channels();
    switch (channels) {
        case 1: // pour image en niveau de gris
        {
            MatIterator_<uchar> it, end;
            for (it = I.begin<uchar>(), end = I.end<uchar>(); it != end; ++it) {
                uchar coef = (uchar) (*it / n);
                *it = (uchar) (coef * n + n / 2);
            }
            break;
        }
        case 4: // pour image rgba
        {
            MatIterator_<Vec3b> it, end;
            for (it = I.begin<Vec3b>(), end = I.end<Vec3b>(); it != end; ++it) {
                uchar coef = (uchar) ((*it)[0] / n);
                (*it)[0] = (uchar) (coef * n + n / 2);

                coef = (uchar) ((*it)[1] / n);
                (*it)[1] = (uchar) (coef * n + n / 2);

                coef = (uchar) ((*it)[2] / n);
                (*it)[2] = (uchar) (coef * n + n / 2);

                coef = (uchar) ((*it)[3] / n);
                (*it)[3] = (uchar) (coef * n + n / 2);
            }
            break;
        }
        default:
            break;
    }

    return I;
}

void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo1Activity_reduce(
        JNIEnv *env, jobject instance,
        jlong matAddrGray, jint n) {

    Mat &mGr = *(Mat *) matAddrGray;
    ScanImageAndReduceIterator(mGr, n);
}

void ScanImageAndReduceC(Mat &myImage, Mat &Result) {
    CV_Assert(myImage.depth() == CV_8U);  // accept only uchar images

    const int nChannels = myImage.channels();
    Result.create(myImage.size(), myImage.type());

    for (int j = 1; j < myImage.rows - 1; ++j) {
        const uchar *previous = myImage.ptr<uchar>(j - 1);
        const uchar *current = myImage.ptr<uchar>(j);
        const uchar *next = myImage.ptr<uchar>(j + 1);

        uchar *output = Result.ptr<uchar>(j);

        for (int i = nChannels; i < nChannels * (myImage.cols - 1); ++i) {
            *output++ = saturate_cast<uchar>(
                    5 * current[i] - current[i - nChannels] -
                    current[i + nChannels] - previous[i] - next[i]);
        }
    }

    Result.row(0).setTo(Scalar(0));
    Result.row(Result.rows - 1).setTo(Scalar(0));
    Result.col(0).setTo(Scalar(0));
    Result.col(Result.cols - 1).setTo(Scalar(0));
}

void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo1Activity_accentuation(
        JNIEnv *env, jobject instance,
        jlong matAddrGray, jlong returnedMat) {

    Mat &mGr = *(Mat *) matAddrGray;
    Mat &retMGr = *(Mat *) returnedMat;
    ScanImageAndReduceC(mGr, retMGr);
}

// end labo1 /////////////////////////////////////////////////////////////////////

// labo2 /////////////////////////////////////////////////////////////////////
void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo2Activity_blur(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDestFinalStep,
        jint kernel_size) {

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDestFinalStep;
    CV_Assert(srcMat.depth() == CV_8U);  // accept only uchar images

    /// Initialize arguments for the filter
    Point anchor = Point(-1, -1);
    double delta = 0;
    int ddepth = -1;

    /// Update kernel size for a normalized box filter
    Mat kernel = Mat::ones(kernel_size, kernel_size, CV_32F) / (float) (kernel_size * kernel_size);

    /// Apply filter
    filter2D(srcMat, destMat, ddepth, kernel, anchor, delta, BORDER_DEFAULT);

//    Laplacian( destMatStep1, destMat, ddepth, kernel_size, scale, delta, BORDER_DEFAULT );
}

void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo2Activity_laplacian(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDestFinalStep,
        jint kernel_size) {

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDestFinalStep;
    CV_Assert(srcMat.depth() == CV_8U);  // accept only uchar images

    /// Initialize arguments for Laplacian
    double delta = 0;
    int ddepth = -1;
    int scale = 1;

    Laplacian(srcMat, destMat, ddepth, kernel_size, scale, delta, BORDER_DEFAULT);
}

void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo2Activity_threshold(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDestFinalStep,
        jint threshold_type, jint threshold_value) {

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDestFinalStep;
    CV_Assert(srcMat.depth() == CV_8U);  // accept only uchar images

    /// Initialize arguments for Laplacian
    int max_BINARY_value = 255;

    threshold(srcMat, destMat, threshold_value, max_BINARY_value, threshold_type);
}
// end labo2 /////////////////////////////////////////////////////////////////////

// labo3 /////////////////////////////////////////////////////////////////////////
JNIEXPORT void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo3Activity_canny(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDest,
        jint lowThreshold) {

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDest;

    Mat src_gray;
    Mat detected_edges;

    int ratio = 3;
    int kernel_size = 3;

    /// Create a matrix of the same type and size as src (for dst)
    destMat.create( srcMat.size(), srcMat.type() );

    /// Convert the image to grayscale
    cvtColor( srcMat, src_gray, CV_BGR2GRAY );
    CV_Assert(srcMat.depth() == CV_8U);  // accept only uchar images

    /// Reduce noise with a kernel 3x3
    blur( src_gray, detected_edges, Size(3,3) );

    /// Canny detector
    Canny( detected_edges, detected_edges, lowThreshold, lowThreshold*ratio, kernel_size );

    /// Using Canny's output as a mask, we display our result
    destMat = Scalar::all(0);

    srcMat.copyTo( destMat, detected_edges);

}

JNIEXPORT void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo3Activity_houghLinesP(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDest)
{

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDest;
    int ratio = 3;

    // Edge detection
    Canny(srcMat, destMat, 50, 200, 3);

    // Copy edges to the images that will display the results in BGR
//    cvtColor(destMat, destMat, COLOR_GRAY2BGR);
//    cdstP = cdst.clone();

    // Probabilistic Line Transform
    vector<Vec4i> linesP; // will hold the results of the detection
    HoughLinesP(destMat, linesP, 1, CV_PI/180, 50, 50, 10 ); // runs the actual detection

    /// Convert it to RGB
    cvtColor( destMat, destMat, CV_GRAY2BGR );

    // Draw the lines
    for( size_t i = 0; i < linesP.size(); i++ )
    {
        Vec4i l = linesP[i];
        cv::line( destMat, Point(l[0], l[1]), Point(l[2], l[3]), Scalar(0,0,255), 2, LINE_AA);
    }
}

JNIEXPORT void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo3Activity_houghCircles(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDest)
{

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDest;

    /// Convert it to gray
    cvtColor( srcMat, destMat, CV_BGR2GRAY );

    /// Reduce the noise so we avoid false circle detection
    GaussianBlur( destMat, destMat, Size(9, 9), 2, 2 );

    vector<Vec3f> circles;

    /// Apply the Hough Transform to find the circles
    HoughCircles( destMat, circles, CV_HOUGH_GRADIENT, 1, destMat.rows/8, 200, 100, 0, 0 );

    /// Convert it to RGB
    cvtColor( destMat, destMat, CV_GRAY2BGR );

    /// Draw the circles detected
    for( size_t i = 0; i < circles.size(); i++ )
    {
        Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
        int radius = cvRound(circles[i][2]);
        // circle center
        circle( destMat, center, 3, Scalar(0,255,0), -1, 8, 0 );
        // circle outline
        circle( destMat, center, radius, Scalar(0,0,255), 3, 8, 0 );
    }
}

JNIEXPORT void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo3Activity_findContours(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDest,
        jint lowThreshold)
{

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDest;
    RNG rng(12345);

    /// Convert image to gray and blur it
    cvtColor( srcMat, destMat, CV_BGR2GRAY );
    blur( destMat, destMat, Size(3,3) );

    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;

    /// Detect edges using canny
    Canny( srcMat, destMat, lowThreshold, lowThreshold*2, 3 );
    /// Find contours
    findContours( destMat, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

    /// Convert it to RGB
    cvtColor( destMat, destMat, CV_GRAY2BGR );

    /// Draw contours
    for( int i = 0; i< contours.size(); i++ )
    {
        Scalar color = Scalar( rng.uniform(0, 255), rng.uniform(0,255), rng.uniform(0,255) );
        drawContours( destMat, contours, i, color, 2, 8, hierarchy, 0, Point() );
    }
}

JNIEXPORT void JNICALL Java_ch_hepia_iti_opencvnativeandroidstudio_Labo3Activity_houghLines(
        JNIEnv *env, jobject instance,
        jlong matSrc, jlong matDest)
{

    Mat &srcMat = *(Mat *) matSrc;
    Mat &destMat = *(Mat *) matDest;

    // Edge detection
    Canny(srcMat, destMat, 50, 200, 3);

    // Copy edges to the images that will display the results in BGR
//    cvtColor(destMat, destMat, COLOR_GRAY2BGR);
//    cdstP = cdst.clone();

    // Probabilistic Line Transform
    vector<Vec4i> linesP; // will hold the results of the detection
    HoughLines(destMat, linesP, 1, CV_PI/180, 50, 50, 10 ); // runs the actual detection

    /// Convert it to RGB
    cvtColor( destMat, destMat, CV_GRAY2BGR );

    // Draw the lines
    for( size_t i = 0; i < linesP.size(); i++ )
    {
        Vec4i l = linesP[i];
        cv::line( destMat, Point(l[0], l[1]), Point(l[2], l[3]), Scalar(0,0,255), 2, LINE_AA);
    }
}
// end labo3 ////////////////////////////////////////////////////////////////////
}