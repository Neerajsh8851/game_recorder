package com.ns.gamerecorder;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class RecorderConfig {
    public VideoResolution[] videoResolutions;
    public int[] videoEncoders;
    public int[] audioEncoders;
    public int[] frameRates;
    public int[] bitrates;
    public int[] outputFormats;

    public HashMap<Integer, String> videoEncoderName;
    public HashMap<Integer, String> audioEncoderName;
    public HashMap<Integer, String> formatName;


    public static class VideoResolution {
        public int w, h;
        public VideoResolution(int a, int b) {
            w = a;
            h = b;
        }

        @NonNull
        @Override
        public String toString() {
            return w + "x" + h ;
        }
    }



    public RecorderConfig() {

        videoResolutions = new VideoResolution[] {
                getDeviceRes(), // default
                new VideoResolution(852, 720),
                new VideoResolution(852, 480),
                new VideoResolution(852, 360)
        };


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioEncoders = new int[] {
                    MediaRecorder.AudioEncoder.AMR_NB,
                    MediaRecorder.AudioEncoder.AAC,
                    MediaRecorder.AudioEncoder.AAC_ELD,
                    MediaRecorder.AudioEncoder.AMR_WB,
                    MediaRecorder.AudioEncoder.HE_AAC,
                    MediaRecorder.AudioEncoder.VORBIS,
                    MediaRecorder.AudioEncoder.OPUS
            };

        } else {
            audioEncoders = new int[]{
                    MediaRecorder.AudioEncoder.AMR_NB,
                    MediaRecorder.AudioEncoder.AAC,
                    MediaRecorder.AudioEncoder.AAC_ELD,
                    MediaRecorder.AudioEncoder.AMR_WB,
                    MediaRecorder.AudioEncoder.HE_AAC,
                    MediaRecorder.AudioEncoder.VORBIS,
            };
        }

        audioEncoderName = new HashMap<>();

        audioEncoderName.put(MediaRecorder.AudioEncoder.AMR_NB, "AMR_NB");
        audioEncoderName.put(MediaRecorder.AudioEncoder.AAC, "AAC");
        audioEncoderName.put(MediaRecorder.AudioEncoder.AAC_ELD, "AAC_ELD");
        audioEncoderName.put(MediaRecorder.AudioEncoder.AMR_WB, "AMR_WB");
        audioEncoderName.put(MediaRecorder.AudioEncoder.HE_AAC, "HE_AAC");
        audioEncoderName.put(MediaRecorder.AudioEncoder.VORBIS, "VORBIS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioEncoderName.put(MediaRecorder.AudioEncoder.OPUS, "OPUS");
        }

        videoEncoders = new int[] {
                MediaRecorder.VideoEncoder.H264,
                MediaRecorder.VideoEncoder.H263,
                MediaRecorder.VideoEncoder.HEVC,
                MediaRecorder.VideoEncoder.MPEG_4_SP,
                MediaRecorder.VideoEncoder.VP8,
        };

        videoEncoderName = new HashMap<>();

        videoEncoderName.put(MediaRecorder.VideoEncoder.H264, "H264");
        videoEncoderName.put(MediaRecorder.VideoEncoder.H263, "H263");
        videoEncoderName.put(MediaRecorder.VideoEncoder.HEVC, "HEVC");
        videoEncoderName.put(MediaRecorder.VideoEncoder.MPEG_4_SP, "MPEG4SP");
        videoEncoderName.put(MediaRecorder.VideoEncoder.VP8, "VP8");

        frameRates = new int[] {15, 24, 30, 40, 60};
        bitrates = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12};

        ArrayList<Integer> formats = new ArrayList<>();
        formats.add(MediaRecorder.OutputFormat.MPEG_4);
        formats.add(MediaRecorder.OutputFormat.THREE_GPP);
        formats.add(MediaRecorder.OutputFormat.AMR_WB);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            formats.add(MediaRecorder.OutputFormat.OGG);

        Integer[] temp = formats.toArray(new Integer[0]);
        outputFormats = new int[formats.size()];
        for (int i = 0; i < formats.size(); i++) {
            outputFormats[i] = formats.get(i);
        }

//        formatName = new HashMap<>();
//        formatName.put(MediaRecorder.OutputFormat.MPEG_4, "mp4");
//        formatName.put(MediaRecorder.OutputFormat.THREE_GPP, "3gp");
//        formatName.put(MediaRecorder.OutputFormat.AMR_WB, "AMR_WB");
    }


    private VideoResolution getDeviceRes() {
        DisplayMetrics m = Resources.getSystem().getDisplayMetrics();

        int a, b;
        a = m.widthPixels;
        b = m.heightPixels;

        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int temp = a;
            a = b;
            b = temp;
        }

        Log.d("RecorderConfig", "device width = " + a);
        Log.d("RecorderConfig", "device height = " + b);

        return new VideoResolution(a, b);
    }
}
