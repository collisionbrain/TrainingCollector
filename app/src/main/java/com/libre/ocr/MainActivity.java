package com.libre.ocr;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar CARD_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        OCR_ACTIVITY       = 101;
    private Display display;
    private Mat mRgba;
    private Mat                    mGray;
    private RelativeLayout rlMain;
    private CameraBridgeViewBase mOpenCvCameraView;
    private int  screenHeigth,screenWidth;
    private Viewport viewport;

    public ImageButton btnCamera,btnClose;
    public Bitmap bmpEntranemiento;
    public boolean comienzaRecoleccion;
    public int indice=0;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");



                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utls.creatDirectories(Constant.pathHost);
        Utls.prepareFiles(getAssets());
        rlMain= (RelativeLayout) findViewById(R.id.rlMain);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_surface_view);
        btnCamera= (ImageButton) findViewById(R.id.camera);
        btnCamera.setOnClickListener(listenerCamera) ;
        btnClose= (ImageButton) findViewById(R.id.close);
        btnClose.setOnClickListener(listenerClose);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        ViewTreeObserver vto = mOpenCvCameraView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewport = (Viewport) findViewById(R.id.viewport);
                viewport.setHeight(rlMain.getHeight());
                display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

                screenHeigth=display.getHeight();
                screenWidth=display.getWidth();
                int rotation=display.getRotation();
                int degrees=0;
                int orientation=getResources().getConfiguration().orientation;
                if( orientation== Configuration.ORIENTATION_LANDSCAPE){
                    if(rotation== Surface.ROTATION_270){
                        degrees=180;
                    }else if(rotation== Surface.ROTATION_180){
                        degrees=180;
                    }
                }else if(orientation== Configuration.ORIENTATION_PORTRAIT){
                    if(rotation== Surface.ROTATION_270){
                        degrees=90;
                    }else if(rotation== Surface.ROTATION_90){
                        degrees=270;
                    }else if(rotation== Surface.ROTATION_180){
                        degrees=180;
                    }else if(rotation== Surface.ROTATION_0){
                        degrees=90;
                    }
                }

                mOpenCvCameraView.setRotationDegrees(degrees);
                mOpenCvCameraView.setScreenSize(screenHeigth,screenWidth);

            }
        });

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        // mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        mRgba = inputFrame.rgba();
        if(comienzaRecoleccion) {

            guardaImagenEntrenamioento(mRgba,indice);
            indice++;
        }
        return mRgba;
    }


    View.OnClickListener listenerClose=new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            finish();
        }

    };

    public void guardaImagenEntrenamioento(Mat matIMG,int indice){
        bmpEntranemiento= Bitmap.createBitmap(matIMG.width(),matIMG.height(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(matIMG,bmpEntranemiento);
        imwrite(Constant.dirImagePart+"img_"+indice+".jpg", matIMG );

    }
    View.OnClickListener listenerCamera=new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            comienzaRecoleccion=true;
        }

    };
    View.OnClickListener listenerGallery=new View.OnClickListener(){

        @Override
        public void onClick(View v) {

        }

    };

}