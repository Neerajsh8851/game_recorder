package com.ns.gamerecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private final int PROJECTION_REQUEST = 8851;
    private final int PERMISSION_REQUEST_CODE = 9988;
    private  String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    private Intent projectionDataIntent;
    private SwitchCompat switchSession;

    // buttons controls
    private ImageView resolutionLeftKey;
    private ImageView resolutionRightKey;
    private ImageView videoEncoderLeftKey;
    private ImageView videoEncoderRightKey;
    private ImageView audioEncoderLeftKey;
    private ImageView audioEncoderRightKey;
    private ImageView frameRateLeftKey;
    private ImageView frameRateRightKey;
    private ImageView bitrateLeftKey;
    private ImageView bitrateRightKey;

    // text views
    private TextView resolutionValueText;
    private TextView videoEncoderValueText;
    private TextView audioEncoderValueText;
    private TextView frameRateValueText;
    private TextView bitrateValueText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadViewControls();

        // choose default config
        App app = (App) getApplication();
        if (!app.pref.getBoolean("default", false)) {
            app.pref.edit().putInt(App.FRAME_RATE_INDEX, 2).apply();
            app.pref.edit().putInt(App.BITRATE_INDEX, 5).apply();
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    this.permissions[0],
                    this.permissions[1],
                    Manifest.permission.POST_NOTIFICATIONS
            };
        }
        requestPermission();
        SRef.hostActivity = this;

        switchSession = findViewById(R.id.switch_session);

        // check if the recording session is already started
        // and set the state of switch correspondingly to the service
        switchSession.setChecked(SRef.captureService != null);


        switchSession.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // start the recording session
                if (SRef.captureService == null)
                    startRecordingService();
            } else {
                if (SRef.captureService != null)
                    stopRecordingService();
            }
        });


        int rot = getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "onCreate: screen rotation = " + rot);
    }


    private void loadViewControls() {
        resolutionLeftKey = findViewById(R.id.iv_resolution_left_key);
        resolutionRightKey = findViewById(R.id.iv_resolution_right_key);
        videoEncoderLeftKey = findViewById(R.id.iv_video_encoder_left_key);
        videoEncoderRightKey = findViewById(R.id.iv_video_encoder_right_key);
        audioEncoderLeftKey = findViewById(R.id.iv_audio_encoder_left_key);
        audioEncoderRightKey = findViewById(R.id.iv_audio_encoder_right_key);
        frameRateLeftKey = findViewById(R.id.iv_frame_rate_left_key);
        frameRateRightKey = findViewById(R.id.iv_frame_rate_right_key);
        bitrateLeftKey = findViewById(R.id.iv_bitrate_left_key);
        bitrateRightKey = findViewById(R.id.iv_bitrate_right_key);

        resolutionLeftKey.setOnClickListener(this);
        resolutionRightKey.setOnClickListener(this);
        videoEncoderLeftKey.setOnClickListener(this);
        videoEncoderRightKey.setOnClickListener(this);
        audioEncoderLeftKey.setOnClickListener(this);
        audioEncoderRightKey.setOnClickListener(this);
        frameRateLeftKey.setOnClickListener(this);
        frameRateRightKey.setOnClickListener(this);
        bitrateLeftKey.setOnClickListener(this);
        bitrateRightKey.setOnClickListener(this);

        resolutionValueText = findViewById(R.id.tv_resolution_value_text);
        videoEncoderValueText = findViewById(R.id.tv_video_encoder_value_text);
        audioEncoderValueText = findViewById(R.id.tv_audio_encoder_value_text);
        frameRateValueText = findViewById(R.id.tv_frame_rate_value_text);
        bitrateValueText = findViewById(R.id.tv_bitrate_value_text);

        App app = (App) getApplication();
        RecorderConfig config = app.config;

        int selector = app.getSelectIndex(App.VIDEO_RESOLUTION_INDEX);
        resolutionValueText.setText(config.videoResolutions[selector].toString());
        Log.d(TAG, "loadViewControls: video text selected = " + selector);

        selector = app.getSelectIndex(App.VIDEO_ENCODER_INDEX);
        videoEncoderValueText.setText(config.videoEncoderName.get(config.audioEncoders[selector]));

        selector = app.getSelectIndex(App.AUDIO_ENCODER_INDEX);
        audioEncoderValueText.setText(config.audioEncoderName.get(config.audioEncoders[selector]));

        selector = app.getSelectIndex(App.FRAME_RATE_INDEX);
        frameRateValueText.setText(String.valueOf(config.frameRates[selector]));

        selector = app.getSelectIndex(App.BITRATE_INDEX);
        bitrateValueText.setText(config.bitrates[selector] + "MB");
    }


    @Override
    public void onClick(View v) {
        App app = (App) getApplication();
        RecorderConfig config = app.config;
        if (v.getId() == R.id.iv_resolution_left_key) {

            int i = app.getSelectIndex(App.VIDEO_RESOLUTION_INDEX);
            i--;
            int len = config.videoResolutions.length;
            if (i < 0) i = len - 1;
            app.putSelectIndex(App.VIDEO_RESOLUTION_INDEX, i);

            resolutionValueText.setText(config.videoResolutions[i].toString());

        } else if (v.getId() == R.id.iv_resolution_right_key) {
            int i = app.getSelectIndex(App.VIDEO_RESOLUTION_INDEX);
            i++;
            int len = config.videoResolutions.length;
            if (i > len - 1) i = 0;
            app.putSelectIndex(App.VIDEO_RESOLUTION_INDEX, i);
            resolutionValueText.setText(config.videoResolutions[i].toString());

        } else if (v.getId() == R.id.iv_video_encoder_left_key) {

            int i = app.getSelectIndex(App.VIDEO_ENCODER_INDEX);
            i--;
            int len = config.videoEncoders.length;
            if (i < 0) i = len - 1;
            app.putSelectIndex(App.VIDEO_ENCODER_INDEX, i);

            videoEncoderValueText.setText(config.videoEncoderName.get(config.videoEncoders[i]));

        } else if (v.getId() == R.id.iv_video_encoder_right_key) {

            int i = app.getSelectIndex(App.VIDEO_ENCODER_INDEX);
            i++;
            int len = config.videoEncoders.length;
            if (i > len - 1) i = 0;
            app.putSelectIndex(App.VIDEO_ENCODER_INDEX, i);
            videoEncoderValueText.setText(config.videoEncoderName.get(config.videoEncoders[i]));

        } else if (v.getId() == R.id.iv_audio_encoder_left_key) {

            int i = app.getSelectIndex(App.AUDIO_ENCODER_INDEX);
            i--;
            int len = config.audioEncoders.length;
            if (i < 0) i = len - 1;
            app.putSelectIndex(App.AUDIO_ENCODER_INDEX, i);
            audioEncoderValueText.setText(config.audioEncoderName.get(config.audioEncoders[i]));

        } else if (v.getId() == R.id.iv_audio_encoder_right_key) {
            int i = app.getSelectIndex(App.AUDIO_ENCODER_INDEX);
            i++;
            int len = config.audioEncoders.length;
            if (i > len - 1) i = 0;
            app.putSelectIndex(App.AUDIO_ENCODER_INDEX, i);
            audioEncoderValueText.setText(config.audioEncoderName.get(config.audioEncoders[i]));

        } else if (v.getId() == R.id.iv_frame_rate_left_key) {

            int i = app.getSelectIndex(App.FRAME_RATE_INDEX);
            i--;
            int len = config.frameRates.length;
            if (i < 0) i = len - 1;
            app.putSelectIndex(App.FRAME_RATE_INDEX, i);
            frameRateValueText.setText(String.valueOf(config.frameRates[i]));

        } else if (v.getId() == R.id.iv_frame_rate_right_key) {
            int i = app.getSelectIndex(App.FRAME_RATE_INDEX);
            i++;
            int len = config.frameRates.length;
            if (i > len - 1) i = 0;
            app.putSelectIndex(App.FRAME_RATE_INDEX, i);
            frameRateValueText.setText(String.valueOf(config.frameRates[i]));

        } else if (v.getId() == R.id.iv_bitrate_left_key) {

            int i = app.getSelectIndex(App.BITRATE_INDEX);
            i--;
            int len = config.bitrates.length;
            if (i < 0) i = len - 1;
            app.putSelectIndex(App.BITRATE_INDEX, i);
            bitrateValueText.setText(String.valueOf(config.bitrates[i]) + "MB");

        } else if (v.getId() == R.id.iv_bitrate_right_key) {

            int i = app.getSelectIndex(App.BITRATE_INDEX);
            i++;
            int len = config.bitrates.length;
            if (i >= len) i = 0;
            app.putSelectIndex(App.BITRATE_INDEX, i);
            bitrateValueText.setText(String.valueOf(config.bitrates[i]) + "MB");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // IF USER ACCEPTS THE PERMISSION TO CAPTURE THE SCREEN
        if (resultCode == RESULT_OK && requestCode == PROJECTION_REQUEST) {
            projectionDataIntent = data;
            startRecordingService();
        } else {
            Toast.makeText(this, "you denied the capture session", Toast.LENGTH_SHORT).show();
            switchSession.setChecked(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!hasPermissions(permissions)) {
                return;
            }
            finish();
        }
    }

    private void startRecordingService() {
        if (projectionDataIntent == null) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(projectionManager.createScreenCaptureIntent(), PROJECTION_REQUEST);
            return;
        }

        // create service intent to start the recording session (foreground service)
        Intent serviceIntent = new Intent(this, CaptureService.class);
        serviceIntent.setAction(CaptureService.ACTION_START_SESSION);
        // the same key is used to obtain projection data in Capture service
        serviceIntent.putExtra(CaptureService.KEY_PROJECTION_DATA, projectionDataIntent);

        startService(serviceIntent);
    }

    private void stopRecordingService() {
        Intent serviceIntent = new Intent(this, CaptureService.class);
        serviceIntent.setAction(CaptureService.ACTION_STOP_SESSION);
        serviceIntent.putExtra("projection_data", projectionDataIntent);

        startService(serviceIntent);
    }


    private void requestPermission() {
        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasPermissions(String[] permissions) {
        boolean hasPermissions = true;
        for (String requiredPerm : permissions) {
            hasPermissions = hasPermissions && (ActivityCompat.checkSelfPermission(this, requiredPerm) == PackageManager.PERMISSION_GRANTED);
        }
        return hasPermissions;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SRef.hostActivity = null;
    }

}