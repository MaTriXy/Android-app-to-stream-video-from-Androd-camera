package com.app.mobile;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements Session.Callback, SurfaceHolder.Callback, RtspClient.Callback {

    public String TAG = "aaaaaaaaaaa";


    // surfaceview
    private static SurfaceView mSurfaceView;

    // Rtsp session
    private Session mSession;
    private static RtspClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        mSurfaceView.getHolder().addCallback(this);

        // Initialize RTSP client
        initRtspClient();

        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleStreaming();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        toggleStreaming();

        Log.d(TAG, "onPause");
    }

    private void initRtspClient() {
        // Configures the SessionBuilder
        mSession = SessionBuilder.getInstance()
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(8000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setSurfaceView(mSurfaceView).setPreviewOrientation(0)
                .setCallback(this).build();

        // Configures the RTSP client
        mClient = new RtspClient();
        mClient.setSession(mSession);
        mClient.setCallback(this);
        mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

        String ip, port, path;

        // We parse the URI written in the Editext
        Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
        Matcher m = uri.matcher("rtsp://192.168.1.90:1935/live/abc");
        m.find();
        ip = m.group(1);
        port = m.group(2);
        path = m.group(3);

        mClient.setCredentials("user", "password");
        mClient.setServerAddress(ip, Integer.parseInt(port));
        mClient.setStreamPath("/" + path);


        Log.d(TAG, "initRTSPClient");

    }

    private void toggleStreaming() {
        if (!mClient.isStreaming()) {
            // Start camera preview
            mSession.startPreview();

            // Start video stream
            mClient.startStream();

            Log.d(TAG, "start Streaming");
        } else {
            // already streaming, stop streaming
            // stop camera preview
            mSession.stopPreview();

            // stop streaming
            mClient.stopStream();
            Log.d(TAG, "Stopping Streaming");
        }

        Log.d(TAG, "Toggled Streaming");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient.release();
        mSession.release();
        mSurfaceView.getHolder().removeCallback(this);
        Log.d(TAG, "on Destroy");
    }


    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        switch (reason) {
            case Session.ERROR_CAMERA_ALREADY_IN_USE:
                break;
            case Session.ERROR_CAMERA_HAS_NO_FLASH:
                break;
            case Session.ERROR_INVALID_SURFACE:
                break;
            case Session.ERROR_STORAGE_NOT_READY:
                break;
            case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
                break;
            case Session.ERROR_OTHER:
                break;
        }

        if (e != null) {
            alertError(e.getMessage());
            e.printStackTrace();
        }

        Log.d(TAG, "on Session error" + e.getMessage());
    }

    private void alertError(final String msg) {
        final String error = (msg == null) ? "Unknown error: " : msg;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(error).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();


        Log.d(TAG, "on Alert Error");
    }

    @Override
    public void onRtspUpdate(int message, Exception exception) {
        switch (message) {
            case RtspClient.ERROR_CONNECTION_FAILED:
            case RtspClient.ERROR_WRONG_CREDENTIALS:
                alertError(exception.getMessage());
                exception.printStackTrace();
                break;
        }

        Log.d(TAG, "on RTSP Update" + exception.getMessage());
    }

    @Override
    public void onPreviewStarted() {

        Log.d(TAG, "on Preview Started");
    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG, "on Session Configured");
    }

    @Override
    public void onSessionStarted() {
        Log.d(TAG, "on Session Started");
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "on session Stopped");
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.d(TAG, "surface changed");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface created");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG, "on Bitrate Update");

    }

}


