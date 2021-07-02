package com.raghav.audioeditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.arthenica.ffmpegkit.ExecuteCallback;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.Log;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.math.BigDecimal;

public class FFmpegExecutionActivity extends AppCompatActivity {

    private boolean executionStatus=false;
    private TextView videoNametop,percentage,cancel,videoNameBot,doneTextView;
    private ImageView actionPlay,actionDelete,share;
    private LinearProgressIndicator progressBar;
    private RelativeLayout relativeInfo,relativeDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_fmpeg_execution);

        videoNameBot=(TextView)findViewById(R.id.videoName);
        videoNametop=(TextView)findViewById(R.id.videoName2);
        percentage=(TextView)findViewById(R.id.progressText);
        cancel=(TextView)findViewById(R.id.cancel);
        doneTextView=(TextView)findViewById(R.id.doneTextView);
        actionPlay=(ImageButton)findViewById(R.id.actionPlay);
        actionDelete=(ImageButton)findViewById(R.id.actionDelete);
        share=(ImageButton)findViewById(R.id.actionShare);
        relativeDone=(RelativeLayout) findViewById(R.id.relativeDone);
        relativeInfo=(RelativeLayout) findViewById(R.id.relativeInfo);
        progressBar=(LinearProgressIndicator) findViewById(R.id.progressBar);

        String exe=getIntent().getStringExtra("exe");
        String filename=getIntent().getStringExtra("filename");
        String safuri=getIntent().getStringExtra("safuri");
        long videoLength=getIntent().getLongExtra("videolength",0);

        android.util.Log.d("LENGTH", String.valueOf(videoLength));
        videoNameBot.setText(filename);
        videoNametop.setText(filename);

        FFmpegSession session = FFmpegKit.executeAsync(exe, new ExecuteCallback() {
            @Override
            public void apply(Session session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();

                FFmpegExecutionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (returnCode.isSuccess()) {
                            executionStatus=true;
                            share.setVisibility(View.VISIBLE);
                            relativeDone.setVisibility(View.VISIBLE);
                            relativeInfo.setVisibility(View.GONE);

                            actionPlay.setVisibility(View.VISIBLE);
                            actionDelete.setVisibility(View.VISIBLE);
                            share.setVisibility(View.VISIBLE);
                            Toast.makeText(FFmpegExecutionActivity.this, "Audio Saved!", Toast.LENGTH_SHORT).show();
                        } else if (returnCode.isCancel()) {
                            //Toast.makeText(FFmpegExecutionActivity.this, "Merging cancelled!", Toast.LENGTH_SHORT).show();
                            relativeInfo.setVisibility(View.GONE);
                            doneTextView.setText("Merging Cancelled!");
                            doneTextView.setCompoundDrawables(null,getResources().getDrawable(R.drawable.ic_baseline_error_outline_24),null,null);
                            relativeDone.setVisibility(View.VISIBLE);

                            actionDelete.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(FFmpegExecutionActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }, new LogCallback() {
            @Override
            public void apply(Log log) {
                FFmpegExecutionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        android.util.Log.d("STATS", log.toString());
                    }});
            }
        }, new StatisticsCallback() {
            @Override
            public void apply(Statistics statistics) {
                float progress = Float.parseFloat(String.valueOf(statistics.getTime())) / videoLength;
                float progressFinal = progress * 100;


                FFmpegExecutionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        android.util.Log.d("STATS",statistics.toString());
                        int timeInMilliseconds = statistics.getTime();
                        if (timeInMilliseconds > 0) {
                            String completePercentage="1";
                            try {
                                completePercentage = new BigDecimal(timeInMilliseconds).multiply(new BigDecimal(100)).divide(new BigDecimal(videoLength), 0, BigDecimal.ROUND_HALF_UP).toString();
                            }catch(ArithmeticException e){
                                e.printStackTrace();
                            }
                            if (percentage != null) {
                                percentage.setText(String.format("Encoding audio: %% %s.", completePercentage));
                            }
                            progressBar.setProgress(Integer.parseInt(completePercentage));
                        }
                    }
                });

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FFmpegKit.cancel(session.getSessionId());
            }
        });

        actionPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag=getSupportFragmentManager().findFragmentByTag("musicplayer");
                if(frag!=null)
                    getSupportFragmentManager().beginTransaction().remove(frag).commit();

                MusicPlayerFragment fragment= new MusicPlayerFragment();
                Bundle bundle=new Bundle();
                bundle.putString("uri",safuri);
                fragment.setArguments(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relativeMerger, fragment,"musicplayer"); // fragment container id in first parameter is the  container(Main layout id) of Activity
                transaction.addToBackStack(null);  // this will manage backstack
                transaction.commit();
            }
        });

        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FFmpegKit.cancel(session.getSessionId());
                if(deleteFile(Uri.parse(safuri))){
                    Toast.makeText(FFmpegExecutionActivity.this, "File deleted.", Toast.LENGTH_SHORT).show();
                    actionPlay.setVisibility(View.GONE);
                    actionDelete.setVisibility(View.GONE);
                    share.setVisibility(View.GONE);

                    videoNametop.setText("File deleted successfully!!");
                }else{
                    Toast.makeText(FFmpegExecutionActivity.this, "Error deleting file.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Share audio..");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(safuri));
                startActivity(intent);
            }
        });
    }
    private boolean deleteFile(Uri uri){
//     ContentResolver resolver=getApplicationContext().getContentResolver();
//     int numVideoRemoved=resolver.delete(uri,null,null);
//     return numVideoRemoved;

        try {
            return DocumentFile.fromSingleUri(getApplicationContext(),uri ).delete();

        }catch (NullPointerException e){
            return false;
        }
    }
}