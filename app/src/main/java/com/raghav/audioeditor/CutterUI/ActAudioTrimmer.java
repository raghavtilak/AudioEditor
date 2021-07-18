package com.raghav.audioeditor.CutterUI;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.arthenica.ffmpegkit.ExecuteCallback;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.raghav.audioeditor.CustomRangeSeekbar;
import com.raghav.audioeditor.CustomSeekBar;
import com.raghav.audioeditor.Cutterutils.TrimAudio;
import com.raghav.audioeditor.Cutterutils.TrimAudioOptions;
import com.raghav.audioeditor.Cutterutils.TrimmerUtils;
import com.raghav.audioeditor.FFmpegExecutionActivity;
import com.raghav.audioeditor.MergerActivity;
import com.raghav.audioeditor.R;

import java.io.File;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ActAudioTrimmer extends AppCompatActivity {

    //1->Quick
    //2->Slow
    private int trimOption=2;
    private static final int WRITE_REQUEST_CODE = 111;
    private PlayerView playerView;

    private SimpleExoPlayer videoPlayer;

    private ImageView imagePlayPause, imageWaveform;


    private long totalDuration;

    private Uri inputUri;

    private TextView txtStartDuration, txtEndDuration, txtFinalDuration;

    private CustomRangeSeekbar seekbar;

    private long lastMinValue = 0;

    private long lastMaxValue = 0;

    private MenuItem menuDone;

    private CustomSeekBar seekbarController;

    private boolean isValidVideo = true, isVideoEnded;

    private Handler seekHandler;

    private long currentDuration, lastClickedTime;

    private boolean hidePlayerSeek;

    private String title="",artist="",album="",genre="",year="";

//    private long fixedGap;
//    private int trimType=1;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video_trimmer);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpToolBar(getSupportActionBar(), getString(R.string.txt_edt_video));
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
        //  progressView = new CustomProgressView(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        playerView = findViewById(R.id.player_view_lib);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        txtFinalDuration = findViewById(R.id.txt_final_duration);
        seekbarController = findViewById(R.id.seekbar_controller);

        imageWaveform = findViewById(R.id.image_one);


        seekHandler = new Handler(getMainLooper());
        initPlayer();

        setDataInView();
    }

    private void setUpToolBar(ActionBar actionBar, String title) {
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SettingUp exoplayer
     **/
    private void initPlayer() {
        try {
            videoPlayer = new SimpleExoPlayer.Builder(this).build();
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            playerView.setPlayer(videoPlayer);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MOVIE)
                        .build();
                videoPlayer.setAudioAttributes(audioAttributes, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataInView() {
        try {
            inputUri = Uri.parse(getIntent().getStringExtra(TrimAudio.TRIM_VIDEO_URI));
//            realUri = uri;
//            uri = Uri.parse(FileUtils.getPath(this, uri));
            Log.d("TRIMMER","VideoUri:: "+String.valueOf(inputUri));
            totalDuration = TrimmerUtils.getDurationSeconds(this, inputUri);
            imagePlayPause.setOnClickListener(v ->
                    onVideoClicked());
            Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v ->
                    onVideoClicked());
            initTrimData();
            buildMediaSource(inputUri);
            setWaveForm(inputUri);
            setUpSeekBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTrimData() {
        try {
            TrimAudioOptions trimAudioOptions = getIntent().getParcelableExtra(TrimAudio.TRIM_VIDEO_OPTION);
            assert trimAudioOptions != null;
            hidePlayerSeek = trimAudioOptions.hideSeekBar;
            toolbar.setTitle(trimAudioOptions.fileName);
//            trimType=trimVideoOptions.trimType;
//            fixedGap = trimVideoOptions.fixedDuration;
//            fixedGap = fixedGap != 0 ? fixedGap : totalDuration;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue);
                videoPlayer.setPlayWhenReady(true);
                imagePlayPause.setVisibility(View.GONE);
                return;
            }
            if ((currentDuration - lastMaxValue) > 0)
                seekTo(lastMinValue);
            videoPlayer.setPlayWhenReady(!videoPlayer.getPlayWhenReady());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekTo(long sec) {
        if (videoPlayer != null)
            videoPlayer.seekTo(sec * 1000);
    }

    private void buildMediaSource(Uri mUri) {
        try {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mUri));
            videoPlayer.addMediaSource(mediaSource);
            videoPlayer.prepare();
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    imagePlayPause.setVisibility(playWhenReady ? View.GONE :
                            View.VISIBLE);
                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_ENDED:
                            Log.d("TRIMMER","onPlayerStateChanged: Video ended.");
                            imagePlayPause.setVisibility(View.VISIBLE);
                            isVideoEnded = true;
                            break;
                        case Player.STATE_READY:
                            isVideoEnded = false;
                            startProgress();
                            Log.d("TRIMMER","onPlayerStateChanged: Ready to play.");
                            break;
                        default:
                            break;
                        case Player.STATE_BUFFERING:
                            Log.d("TRIMMER","onPlayerStateChanged: STATE_BUFFERING.");
                            break;
                        case Player.STATE_IDLE:
                            Log.d("TRIMMER","onPlayerStateChanged: STATE_IDLE.");
                            break;
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpSeekBar() {
        seekbar.setVisibility(View.VISIBLE);
        txtStartDuration.setVisibility(View.VISIBLE);
        txtEndDuration.setVisibility(View.VISIBLE);
        txtFinalDuration.setVisibility(View.VISIBLE);

        seekbarController.setMaxValue(totalDuration).apply();
        seekbar.setMaxValue(totalDuration).apply();
        seekbar.setMaxStartValue((float) totalDuration).apply();

//        if(trimType == 1)
//        {
//            seekbar.setGap(2).apply();
//        }else{
////            seekbar.setMaxStartValue((float) minGap);
////            seekbar.setGap(minGap).apply();
//
//            seekbar.setFixGap(fixedGap).apply();
//        }
        seekbar.setGap(2).apply();
        lastMaxValue = totalDuration;

        if (hidePlayerSeek)
            seekbarController.setVisibility(View.GONE);

        seekbar.setOnRangeSeekbarFinalValueListener((minValue, maxValue) -> {
            if (!hidePlayerSeek)
                seekbarController.setVisibility(View.VISIBLE);
        });

        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            long minVal = minValue.longValue();
            long maxVal = maxValue.longValue();
            if (lastMinValue != minVal) {
                seekTo((long) minValue);
                if (!hidePlayerSeek)
                    seekbarController.setVisibility(View.INVISIBLE);
            }
            lastMinValue = minVal;
            lastMaxValue = maxVal;
            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal));
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal));
            txtFinalDuration.setText(String.format("~%s", TrimmerUtils.formatSeconds(lastMaxValue - lastMinValue)));
        });

        seekbarController.setOnSeekbarFinalValueListener(value -> {
            long value1 = (long) value;
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1);
                return;
            }
            if (value1 > lastMaxValue)
                seekbarController.setMinStartValue((int) lastMaxValue).apply();
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue((int) lastMinValue).apply();
                if (videoPlayer.getPlayWhenReady())
                    seekTo(lastMinValue);
            }
        });

    }

    /**
     * will be called whenever seekBar range changes
     * it checks max duration is exceed or not.
     * and disabling and enabling done menuItem
     */
    // @param minVal left thumb value of seekBar
    // @param maxVal right thumb value of _

//    private void setDoneColor(long minVal, long maxVal) {
//        try {
//            if (menuDone == null)
//                return;
//            //changed value is less than maxDuration
//            if ((maxVal - minVal) <= maxToGap) {
//                menuDone.getIcon().setColorFilter(
//                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white)
//                                , PorterDuff.Mode.SRC_IN)
//                );
//                isValidVideo = true;
//            } else {
//                menuDone.getIcon().setColorFilter(
//                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white)
//                                , PorterDuff.Mode.SRC_IN)
//                );
//                isValidVideo = false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null)
            videoPlayer.release();

        stopRepeatingTask();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            //prevent multiple clicks
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                return true;
            lastClickedTime = SystemClock.elapsedRealtime();

            saveOptionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }

    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = videoPlayer.getCurrentPosition() / 1000;
                if (!videoPlayer.getPlayWhenReady())
                    return;
                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else
                    videoPlayer.setPlayWhenReady(false);
            } finally {
                seekHandler.postDelayed(updateSeekbar, 1000);
            }
        }
    };

    public String getTime(long miliSeconds)
    {
        int mm=(int)TimeUnit.MILLISECONDS.toMillis(miliSeconds%1000)/100;
        int hrs = (int) TimeUnit.MILLISECONDS.toHours(miliSeconds) % 24;
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(miliSeconds) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds) % 60;
        return String.format("%02d:%02d:%02d.%02d", hrs, min, sec,mm);
    }

    private void saveOptionDialog(){

        Log.d("TAG","SAVE DIALOG");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme);
        builder.setTitle("Save File");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.save_dialog_layout,null);

        TextInputEditText filenameEditText = (TextInputEditText) viewInflated.findViewById(R.id.filename);
        TextInputEditText titleEditText = (TextInputEditText) viewInflated.findViewById(R.id.title);
        TextInputEditText albumEditText = (TextInputEditText) viewInflated.findViewById(R.id.album);
        TextInputEditText artistEditText = (TextInputEditText) viewInflated.findViewById(R.id.artist);
        TextInputEditText genreEditText = (TextInputEditText) viewInflated.findViewById(R.id.genre);
        TextInputEditText yearEditText = (TextInputEditText) viewInflated.findViewById(R.id.year);
        TextView moreOptions=(TextView)viewInflated.findViewById(R.id.moreOptions);

        TextInputLayout filenameTIL = (TextInputLayout) viewInflated.findViewById(R.id.filenameTIL);
        TextInputLayout titleTIL = (TextInputLayout) viewInflated.findViewById(R.id.titleTIL);
        TextInputLayout albumTIL = (TextInputLayout) viewInflated.findViewById(R.id.albumTIL);
        TextInputLayout artistTIL = (TextInputLayout) viewInflated.findViewById(R.id.artistTIL);
        TextInputLayout genreTIL = (TextInputLayout) viewInflated.findViewById(R.id.genreTIL);
        TextInputLayout yearTIL = (TextInputLayout) viewInflated.findViewById(R.id.yearTIL);

        filenameEditText.setText("AUD-"+ System.currentTimeMillis());
        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(titleTIL.getVisibility()==View.GONE) {
                    titleTIL.setVisibility(View.VISIBLE);
                    albumTIL.setVisibility(View.VISIBLE);
                    artistTIL.setVisibility(View.VISIBLE);
                    genreTIL.setVisibility(View.VISIBLE);
                    yearTIL.setVisibility(View.VISIBLE);
                }else{
                    titleTIL.setVisibility(View.GONE);
                    albumTIL.setVisibility(View.GONE);
                    artistTIL.setVisibility(View.GONE);
                    genreTIL.setVisibility(View.GONE);
                    yearTIL.setVisibility(View.GONE);

                    titleEditText.setText("");
                    albumEditText.setText("");
                    artistEditText.setText("");
                    genreEditText.setText("");
                    yearEditText.setText("");
                }
            }
        });

        builder.setView(viewInflated);

        builder.setPositiveButton("SAVE AS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String fileName=filenameEditText.getText().toString();
                if(Objects.requireNonNull(filenameEditText.getText()).length()==0){
                    filenameTIL.setErrorEnabled(true);
                    filenameTIL.setError("This can't be empty!");
                }else {
                    fileName = filenameEditText.getText().toString();
                }
                if (Objects.requireNonNull(titleEditText.getText()).length()!=0){
                    title=titleEditText.getText().toString();
                }
                if (Objects.requireNonNull(albumEditText.getText()).length()!=0){
                    album=albumEditText.getText().toString();
                }
                if (Objects.requireNonNull(artistEditText.getText()).length()!=0){
                    artist=artistEditText.getText().toString();
                }
                if (Objects.requireNonNull(genreEditText.getText()).length()!=0){
                    genre=genreEditText.getText().toString();
                }
                if (Objects.requireNonNull(yearEditText.getText()).length()!=0){
                    year=yearEditText.getText().toString();
                }
//                createOutputFile(fileName,title,artist,album);
                createFile(fileName);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void createFile(String fileName) {
        Log.d("TAG","CREATE FILE");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String fileType=getMime(inputUri);
        // file type
        if(fileType.equalsIgnoreCase("mp3")){
            intent.setType("audio/mpeg");
            // file name
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
        }else if(fileType.equalsIgnoreCase("wav")){
            intent.setType("audio/*");
            // file name
            intent.putExtra(Intent.EXTRA_TITLE, fileName+".wav");
        }else if(fileType.equalsIgnoreCase("m4a")){
            intent.setType("audio/*");
            // file name
            intent.putExtra(Intent.EXTRA_TITLE, fileName+".m4a");
        }else{
            intent.setType("audio/"+fileType);
            // file name
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
        }

        // file name
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(intent,WRITE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK ) {

            switch (requestCode) {
                case WRITE_REQUEST_CODE:
                    if (data != null) {
                        Uri uri = data.getData();
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        String videoPath = FFmpegKitConfig.getSafParameterForWrite(this, uri);
                        Log.d("SAF URI", String.valueOf(uri));

                        executeFFmpeg(videoPath,getSAFUriInfo(uri),uri);
                    }
                    break;
            }
        }
    }

    private void executeFFmpeg(String outputFile,String fileName,Uri safuri){


//        ffmpeg -ss 00:02:43.00 -t 00:00:10 -i input.mp3 -codec:a libmp3lame out.mp3

        String input=FFmpegKitConfig.getSafParameterForRead(this,inputUri);
        long finalVideoLength=(lastMaxValue*1000-lastMinValue*1000);

        StringBuilder metadata=new StringBuilder(" ");
        metadata.append(" -metadata title=\"").append(title).append("\" -metadata artist=\"").append(artist)
                .append("\" -metadata album=\"").append(album).append("\" -metadata genre=\"").append(genre)
                .append("\" -metadata date=\"").append(year).append("\"");

        String exe="-ss "+TrimmerUtils.formatCSeconds(lastMinValue)+" -to "+
                TrimmerUtils.formatCSeconds(lastMaxValue)+
                " -i "+input+metadata.toString()+
                " -codec:a libmp3lame "+outputFile;

        Log.d("QUERY",exe);
        startActivity(new Intent(this,FFmpegExecutionActivity.class)
                .putExtra("exe",exe)
                .putExtra("type","Trimming")
                .putExtra("filename",fileName)
                .putExtra("videolength",finalVideoLength)
                .putExtra("safuri",String.valueOf(safuri)));
//        finishAffinity();
        finish();
    }


    private String getSAFUriInfo(Uri safuri){
        String fileName = null;
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE
        };

        Cursor c = getContentResolver().query(safuri, projection, null, null, null);
        if (c != null) {
            int nameIndex = c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
            int durationIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeIndex = c.getColumnIndexOrThrow(OpenableColumns.SIZE);

            c.moveToFirst();
            fileName = c.getString(nameIndex);
            long duration = c.getInt(durationIndex);
            int size = c.getInt(sizeIndex);
            float sizeTomb = size / (1024f * 1024f);


        }
        return fileName;
    }

    private void setWaveForm(Uri uri){
        ProgressDialog progressDialog=new ProgressDialog(this,R.style.ProgressDialog);
        progressDialog.setMessage("Generating Waveform..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        File file=getCacheDir();
        File image=new File(file,System.currentTimeMillis()+".png");

        String path = FFmpegKitConfig.getSafParameterForRead(ActAudioTrimmer.this, uri);
        String exe="-i "+path+" -f lavfi -i color=c=#022E57:s=640x320 -filter_complex \"[0:a]showwavespic=s=640x320:colors=#DF4769[fg];[1:v][fg]overlay=format=auto\" -frames:v 1 "+image.getAbsolutePath();

        Log.d("QUERY",exe);

        FFmpegSession session = FFmpegKit.executeAsync(exe, new ExecuteCallback() {
            @Override
            public void apply(Session session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();

                ActAudioTrimmer.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (returnCode.isSuccess()) {
//                            Toast.makeText(ActAudioTrimmer.this, "Waveform created", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Bitmap bitmap=BitmapFactory.decodeFile(image.getAbsolutePath());
//                            seekbar.setBackground(Drawable.createFromPath(image.getAbsolutePath()));
                            imageWaveform.setImageBitmap(bitmap);
                            Log.d("BITMAP", String.valueOf(bitmap.getByteCount()));
                        } else if (returnCode.isCancel()) {

                        } else {
                            progressDialog.dismiss();

                            Toast.makeText(ActAudioTrimmer.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });
    }


    private String getMime(Uri uri){
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

}
