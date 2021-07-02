package com.raghav.audioeditor.Cutterutils;


import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.raghav.audioeditor.CutterUI.ActAudioTrimmer;


public class TrimAudio {

    public static int VIDEO_TRIMMER_REQ_CODE = 324;

    public static final String TRIM_VIDEO_OPTION = "trim_video_option",
            TRIM_VIDEO_URI = "trim_video_uri",TRIMMED_VIDEO_PATH="trimmed_video_path";

    public static ActivityBuilder activity(String uri) {
        return new ActivityBuilder(uri);
    }

    public static String getTrimmedVideoPath(Intent intent){
        return intent.getStringExtra(TRIMMED_VIDEO_PATH);
    }

    public static final class ActivityBuilder {

        @Nullable
        private final String videoUri;

        private TrimAudioOptions options;

        public ActivityBuilder(@Nullable String videoUri) {
            this.videoUri = videoUri;
            options = new TrimAudioOptions();
        }


        public ActivityBuilder setHideSeekBar(final boolean hide) {
            options.hideSeekBar = hide;
            return this;
        }

        public void start(Activity activity) {
            validate();
            activity.startActivityForResult(getIntent(activity), VIDEO_TRIMMER_REQ_CODE);
        }


        private void validate() {
            if (videoUri == null)
                throw new NullPointerException("VideoUri cannot be null.");
            if (videoUri.isEmpty())
                throw new IllegalArgumentException("VideoUri cannot be empty");
        }
//
//        public ActivityBuilder setFixedDuration(final long fixedDuration) {
//            options.fixedDuration = fixedDuration;
//            return this;
//        }
        public ActivityBuilder setFilename(final String filename) {
            options.fileName = filename;
            return this;
        }
//        public ActivityBuilder setTrimType(final int trimType) {
//            options.trimType = trimType;
//            return this;
//        }
        private Intent getIntent(Activity activity) {
            Intent intent = new Intent(activity, ActAudioTrimmer.class);
            intent.putExtra(TRIM_VIDEO_URI, videoUri);
            intent.putExtra(TRIM_VIDEO_OPTION, options);
            return intent;
        }
    }


}
