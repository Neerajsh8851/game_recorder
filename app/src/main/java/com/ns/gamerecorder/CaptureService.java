package com.ns.gamerecorder;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CaptureService extends Service {

    private final static String TAG = "CaptureService";

    public final static String ACTION_START_SESSION = "service.action.start";
    public final static String ACTION_STOP_SESSION = "service.action.stop";
    private final static String ACTION_RECORDING_START = "recorder.start";
    private final static String ACTION_RECORDING_STOP = "recorder.stop";
    private final static String ACTION_RECORDING_PAUSE = "recorder.pause";
    private final static String ACTION_RECORDING_RESUME = "recorder.resume";

    private MediaProjection projection;
    private MediaRecorder recorder;
    private VirtualDisplay virtualDisplay;
    private ServiceHandler handler;

    public static final int START_RECORDING = 0;
    public static final int STOP_RECORDING = 1;
    public static final int PAUSE_RECORDING = 2;
    public static final int RESUME_RECORDING = 3;

    private boolean isRecording = false;

    // notification layout
    RemoteViews notificationFirstView;
    RemoteViews notificationSecondView;




    private class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_RECORDING:
                    startRecording();
                    break;
                case STOP_RECORDING:
                    stopRecording();
                    break;
                case PAUSE_RECORDING:
                    pauseRecording();
                    break;
                case RESUME_RECORDING:
                    resumeRecording();
            }
        }
    }

    public CaptureService() {
        // empty constructor
    }


    @Override
    public IBinder onBind(Intent intent) {
        // if we don't want to bind this service we should return null;
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: action = " + intent.getAction());
        // start the recording session
        switch (intent.getAction()) {
            case ACTION_START_SESSION:
                // get the projection token
                projection = ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                        .getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra("KEY_PROJECTION_DATA"));

                recorder = new MediaRecorder();

                initRemoteViews();
                createCustomNotification(notificationFirstView);
                break;

            // stop the recording session
            case ACTION_STOP_SESSION:
                if (isRecording && recorder != null) {
                    stopRecording();
                }
                stopSelf();
                break;

            case ACTION_RECORDING_START: {
                Message message = handler.obtainMessage();
                message.what = START_RECORDING;
                handler.sendMessage(message);
                break;
            }
            case ACTION_RECORDING_STOP: {
                Message message = handler.obtainMessage();
                message.what = STOP_RECORDING;
                handler.sendMessage(message);
                break;
            }

            case ACTION_RECORDING_PAUSE: {
                Log.d(TAG, "onStartCommand: action = recording pause");
                Message m = handler.obtainMessage();
                m.what = PAUSE_RECORDING;
                handler.sendMessage(m);
                break;
            }

            case ACTION_RECORDING_RESUME: {
                Log.d(TAG, "onStartCommand: action = recording resume");
                Message m = handler.obtainMessage();
                m.what = RESUME_RECORDING;
                handler.sendMessage(m);
                break;
            }
        }
        return START_NOT_STICKY;

    }


    private void createCustomNotification(RemoteViews views) {
        Notification notification = new NotificationCompat.Builder(getApplication(), App.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.small_icon)
                .setCustomContentView(views)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground(8851, notification);
    }


    private void initRemoteViews() {

        // initialize first notification
        // which is shown to start the recording
        notificationFirstView = new RemoteViews(getPackageName(), R.layout.notification_layout_first);

        // what to happens when user click notification buttons
        Intent service = new Intent(getApplication(), CaptureService.class);
        service.setAction(ACTION_RECORDING_START);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, service, 0);
        notificationFirstView.setOnClickPendingIntent(R.id.iv_toggle_pause_resume, pendingIntent);

        service.setAction(ACTION_STOP_SESSION);
        pendingIntent = PendingIntent.getService(this, 0, service, 0);
        notificationFirstView.setOnClickPendingIntent(R.id.iv_quit, pendingIntent);


        // initialize second notification
        // which is shown after the recording has been started
        notificationSecondView = new RemoteViews(getPackageName(), R.layout.notification_layout_second);

        service.setAction(ACTION_RECORDING_PAUSE);
        pendingIntent = PendingIntent.getService(this, 0, service, 0);
        notificationSecondView.setOnClickPendingIntent(R.id.iv_toggle_pause_resume, pendingIntent);

        service.setAction(ACTION_RECORDING_STOP);
        pendingIntent = PendingIntent.getService(this, 0, service, 0);
        notificationSecondView.setOnClickPendingIntent(R.id.iv_stop, pendingIntent);
    }


    private void dispose() {
        if (recorder!= null) {
            recorder.release();
            recorder = null;
        }

        if (projection != null) {
            projection.stop();
            projection = null;
        }

        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (isRecording) {
            stopRecording();
        }
        stopSelf();
    }


    private void startRecording() {
        if (recorder == null) {
            recorder  = new MediaRecorder();
        } else {
            recorder.reset();
        }

        App app = (App) getApplication();
        RecorderConfig config  = SRef.config;

        // configure the recorder
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(config.audioEncoders[app.getSelectIndex(App.AUDIO_ENCODER_INDEX)]);
        recorder.setVideoEncoder(config.videoEncoders[app.getSelectIndex(App.VIDEO_ENCODER_INDEX)]);


        RecorderConfig.VideoResolution r = config.videoResolutions[app.getSelectIndex(App.VIDEO_RESOLUTION_INDEX)];
        int a = r.w;
        int b = r.h;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int temp = a;
            a = b;
            b = temp;
        }
        recorder.setVideoSize(a, b);

        Log.d(TAG, "startRecording: video size = " + a + " x " + b);

        int videoBitrate = config.bitrates[app.getSelectIndex(App.BITRATE_INDEX)] * 1048576 * 8;  // bits per sec
        Log.d(TAG, "startRecording: video bitrate = " + videoBitrate);

        recorder.setVideoEncodingBitRate(videoBitrate);
        recorder.setVideoFrameRate(config.frameRates[app.getSelectIndex(App.FRAME_RATE_INDEX)]);
        recorder.setOutputFile(createFileName());

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeVirtualDisplay();
        recorder.start();
        isRecording = true;

        // this is to update the notification panel
        // which then can be used to stop the recording
        createCustomNotification(notificationSecondView);
    }

    private void stopRecording() {
        recorder.stop();
        isRecording = false;
        // this is to update the notification panel
        // which then can be used to start recording or stop the session
        createCustomNotification(notificationFirstView);
    }

    private void pauseRecording() {
        recorder.pause();
        notificationSecondView.setImageViewResource(R.id.iv_toggle_pause_resume, R.drawable.ic_vec_resume);
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(), 0,
                new Intent(getApplicationContext(), CaptureService.class).setAction(ACTION_RECORDING_RESUME),
                0
        );
        notificationSecondView.setOnClickPendingIntent(R.id.iv_toggle_pause_resume, pendingIntent);
        createCustomNotification(notificationSecondView);
    }

    private void resumeRecording() {
        recorder.resume();
        notificationSecondView.setImageViewResource(R.id.iv_toggle_pause_resume, R.drawable.vec_ic_pause);
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(), 0,
                new Intent(getApplicationContext(), CaptureService.class).setAction(ACTION_RECORDING_PAUSE),
                0
        );
        notificationSecondView.setOnClickPendingIntent(R.id.iv_toggle_pause_resume, pendingIntent);
        createCustomNotification(notificationSecondView);
    }

    private void initializeVirtualDisplay() {
        App app = (App) getApplication();
        RecorderConfig config  = SRef.config;
        RecorderConfig.VideoResolution r = config.videoResolutions[app.getSelectIndex(App.VIDEO_RESOLUTION_INDEX)];

        int a = r.w;
        int b = r.h;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int temp = a;
            a = b;
            b = temp;
        }
        virtualDisplay =  projection.createVirtualDisplay("screen_recording",
                a,
                b,
                getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                recorder.getSurface(), null, null);
    }

    private DisplayMetrics getDisplayMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }


    private String createFileName() {

        // directory
        File fileUrl =   new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +  "game recorder");

        // name of the file
        String filename = "game recorder " +
                new SimpleDateFormat("dd-M-yyyy hh:mm:ss", Locale.getDefault()).format( new Date()) + ".mp4";

        // create directory if not exist
        if (!fileUrl.exists()) {
            if (!fileUrl.mkdir()) {
                if (recorder != null) {
                    recorder.reset();
                    recorder.release();
                    recorder = null;
                }

                Toast.makeText(this, "cannot make a file", Toast.LENGTH_SHORT).show();
            }
        }


        String uri = fileUrl.getAbsolutePath() + File.separator + filename;

        Log.d(TAG, "createFileName: " + uri);
        return uri;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SRef.captureService = this;

        // initialize background thread
        HandlerThread handlerThread = new HandlerThread("recording_thread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        handler = new ServiceHandler(handlerThread.getLooper());

        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SRef.captureService = null;

        if (isRecording && recorder != null) {
           stopRecording();
        }
        dispose();
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }
}