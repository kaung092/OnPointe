package com.example.rene.dancesafe;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)

public class Split_camera extends Activity
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private Chronometer chronometer;    // This is for recording time

    final Context context = this;       // Context of this class "newRecordVideo.this"

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private Handler mHandler = new Handler();
    private static final String TAG = "Camera2VideoFragment";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    public SensorDataThread sensorThread;
    public volatile String liveData1;
    public volatile String liveData2;

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    String videoName;

    private VideoView video_view_record;
    public BufferedReader br_ref;
    public FileInputStream fis_ref;
    private String line_ref;
    public File sensorFile_ref;
    public boolean kill_pressureThread = false;


    static {

        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * Button to record video
     */
    private Button mButtonVideo;

    /**
     * A refernce to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    /**
     * Camera preview.
     */
    private CaptureRequest.Builder mPreviewBuilder;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            kill_pressureThread= true;
            sensorThread.kill();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = Split_camera.this;
            if (null != activity) {
                activity.finish();
                kill_pressureThread =true;
                sensorThread.kill();
            }
        }

    };

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /****************/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /****************/

        setContentView(R.layout.activity_split_camera);

        // Put video name in invisible textView to later retrieve
        Intent intent = getIntent();
        videoName = intent.getStringExtra("my_video");
        final TextView show = (TextView)findViewById(R.id.textView16);
        show.setText(videoName);

        video_view_record = (VideoView) findViewById(R.id.viewVideo_ref);

        mTextureView = (AutoFitTextureView)findViewById(R.id.texture2);
        mButtonVideo = (Button)findViewById(R.id.video2);
        mButtonVideo.setOnClickListener(this);

        /****************/
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mTextureView.getLayoutParams().width = (screenWidth);
        mTextureView.getLayoutParams().height = (screenHeight);
        /****************/

        File file = new File("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/safe2dance_practice/" + videoName + ".mp4");
        sensorFile_ref = new File(Environment.getExternalStorageDirectory().toString() + "/safe2dance_record/" + videoName + ".txt");
        if(file.exists()) {
            Log.e(TAG, "File exists!");
        }else {Log.e(TAG, "File does not exist");}
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        final TextView text = (TextView)findViewById(R.id.textView16);

        String videoName = text.getText().toString();

        savedInstanceState.putString("my_video", videoName);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        String videoName = savedInstanceState.getString("my_video");
        final TextView text = (TextView)findViewById(R.id.textView16);
        text.setText(videoName);

        File file = new File("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/safe2dance_practice/" + videoName + ".mp4");
        if(file.exists()) {
            Log.e(TAG, "File exists!");
        }else {Log.e(TAG, "File does not exist");}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        Toast.makeText(this, "Video recording cancelled", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, practice_existing.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        Log.e(TAG, "mButtonVideo clicked");
        if (mIsRecordingVideo) {


            stopRecordingVideo();

        } else {


            startRecordingVideo();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Split_camera.this);

            builder.setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Split_camera.this, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Split_camera.this.finish();
                                }
                            })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Split_camera.this);
                        final Activity activity = this;
                        builder.setMessage(R.string.permission_request)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        activity.finish();
                                    }
                                })
                                .create().show();
                        break;
                    }
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(Split_camera.this);
                final Activity activity = this;
                builder.setMessage(R.string.permission_request)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.finish();
                            }
                        })
                        .create().show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(Split_camera.this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    private void openCamera(int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = Split_camera.this;
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[1];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            /*
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            */
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<Surface>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Activity activity = Split_camera.this;
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = Split_camera.this;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = Split_camera.this;
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(getVideoFile(activity).getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation);

        mMediaRecorder.setOrientationHint(orientation+180); //Adjust rotation for front camera
        mMediaRecorder.prepare();
    }

    private File getVideoFile(Context context) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/safe2dance_practice/"+videoName+".mp4");
    }

    private void startRecordingVideo() {
        try {

            //Sensor Thread
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File sensorFile = new File(extStorageDirectory + "/safe2dance_practice/" + videoName + ".txt");

                try {
                    sensorFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            sensorThread = new SensorDataThread(sensorFile,"prac");
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            System.out.print("sensor Thread is called");
            sensorThread.start();
            kill_pressureThread = false;
            mHandler.postDelayed(pressure_thread, 200);


            // UI
            mButtonVideo.setBackground(getResources().getDrawable(R.drawable.video_font_awesome_recording));
            chronometer = (Chronometer) findViewById(R.id.chronometer);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            mIsRecordingVideo = true;
            Uri uri_reference = Uri.parse("file:///" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/safe2dance_record/" + videoName + ".mp4");
            video_view_record.setVideoURI(uri_reference);
            video_view_record.start();

            // Start recording
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingVideo() {
        //sensor thread
        sensorThread.kill();
        kill_pressureThread =true;
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setBackground(getResources().getDrawable(R.drawable.video_font_awesome));
        chronometer.stop();

        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        video_view_record.pause();

        Activity activity = Split_camera.this;
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + getVideoFile(activity).getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        }
        startPreview();
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    private Runnable pressure_thread = new Runnable(){

        int ref;
        int prac;
        @Override
        public void run() {

            if(!kill_pressureThread) {

                if (br_ref == null) {
                    try {
                        fis_ref = new FileInputStream(sensorFile_ref);

                        br_ref = new BufferedReader(new InputStreamReader(fis_ref));

                        line_ref = br_ref.readLine();

                        System.out.println("Ref file (" + videoName + ") read " + line_ref);


                        //Compare_and_footMap(line_ref, line_prac);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Playing. br_ref was null and now br_ref = " + br_ref);
                }
                else {
                    try {
                        line_ref = br_ref.readLine();
                        System.out.println("Ref file (" + videoName + ") read " + line_ref);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(liveData1!=null) {
                    prac = Integer.parseInt(liveData1);
                }

                if (line_ref != null) {
                    String[] ref_array = line_ref.split(",");
                    //ref_sec = Integer.parseInt(ref_array[0]);
                    ref = Integer.parseInt(ref_array[1]);
                    System.out.println("compare_ref value: " + ref);
                }


                View p1_ref = findViewById(R.id.p1_ref);
                View p2_ref = findViewById(R.id.p2_ref);

                View p1_prac = findViewById(R.id.p1_prac);
                View p2_prac = findViewById(R.id.p2_prac);

                if (prac <= 15) {
                    p2_prac.setBackgroundResource(R.drawable.gray_circle);
                } else if (prac >= 15 && prac <= 50) {
                    p2_prac.setBackgroundResource(R.drawable.green_circle);
                } else if (prac > 50) {
                    p2_prac.setBackgroundResource(R.drawable.yellow_circle);
                }


                if (ref <= 15) {
                    p2_ref.setBackgroundResource(R.drawable.gray_circle);
                } else if (ref > 15 && ref <= 50) {
                    p2_ref.setBackgroundResource(R.drawable.green_circle);
                } else if (ref > 50) {
                    p2_ref.setBackgroundResource(R.drawable.yellow_circle);
                }


                if ((prac <= ref + 5 && prac >= ref - 5) || (ref <= prac + 5 && ref >= prac - 5)) {
                } else {
                    if (prac <= 25) {
                        p2_prac.setBackgroundResource(R.drawable.gray_circle_wrong);
                    } else if (prac > 25 && prac <= 50) {
                        p2_prac.setBackgroundResource(R.drawable.green_circle_wrong);
                    } else if (prac > 50) {
                        p2_prac.setBackgroundResource(R.drawable.yellow_circle_wrong);
                    }
                }


            }
            mHandler.postDelayed(this, 200);
        }
        };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();



            switch(action) {
                //Start for address 1
                case BluetoothLeService.ACTION_DATA_AVAILABLE_1:
                    liveData1 =intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    sensorThread.setData_1(liveData1);
                    System.out.println("Inside Split_Camera, Data read" + liveData1);
                    break;
                //Start for address 2
                case BluetoothLeService.ACTION_DATA_AVAILABLE_2:
                    liveData2 =intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    sensorThread.setData_2(liveData2);
                    System.out.println("Inside Split_Camera, Data read" + liveData2);
                    break;
            }



        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED_1);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_1);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_1);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_1);
        return intentFilter;
    }

}