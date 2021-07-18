package com.raghav.audioeditor;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.IOException;


public class MusicPlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{

    private TextView now_time;
    private SeekBar audio_seekBar;
    private ImageView btn_start_audio;
    private Button btn_stop_audio;

    private MediaPlayer m;

//    private Context context = getActivity();

    private Thread thread;
    //Record playback position
    private int time;
    //Whether recording is paused

    private boolean flage = false, isChanging = false;

    String uri="";

    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_music_player, container, false);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            uri = bundle.getString("uri", "");
        }

        //Media control settings
        m = new MediaPlayer();
        m.reset();
        m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        init(view);
        return view;

    }

//    //Activity is called when it returns to the foreground from the background
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        if (m != null) {
//            if (m.isPlaying()) {
//                m.start();
//            }
//        }
//    }

    //Activity is overwritten below or called when the screen is locked
    @Override
    public void onPause() {

        Log.d("TAG","onPause");

        super.onPause();
        if (m != null) {
            if (m.isPlaying()) {
                m.pause();
                Log.d("TAG","onPause puase");
            }else{
                Log.d("TAG","onPause not playeing");
            }
        }else{
            Log.d("TAG","onPause null");
        }

    }


    //Activity is destroyed
    @Override
    public void onDestroy() {
        Log.d("TAG","ondestroy");
        if (m.isPlaying()) {
            m.stop();//Stop audio playback
            Log.d("TAG","ondestroy stop");
        }else{
            Log.d("TAG","ondestroy null");
        }

        //this produncing error
//        m.release();//Release resources
        m.reset();
        //m=null;
        super.onDestroy();
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play:
                    if (m.isPlaying()) {
                        //m.getCurrentPosition(); Get the current playback position
                        time = m.getCurrentPosition();
                        // If it is playing, pause and set the text on the button to "pause"
                        m.pause();
//                        btn_start_audio.setText("time out");
                        Log.d("TAG","Pause");
                        btn_start_audio.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play));
                        flage = true;//flage is marked as ture
                    } else if (flage) {
                        m.start();//Start playing first
                        m.seekTo(time);//Set where to start playing
                        Log.d("TAG","Play after pause");
//                        btn_start_audio.setText("Play");
                        btn_start_audio.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_pause_circle_filled_24));
                        flage = false;
                    } else {
                        m.reset();//Restore to uninitialized state
                        m = MediaPlayer.create(getActivity().getApplicationContext(), Uri.parse(uri));//Read audio
                        audio_seekBar.setMax(m.getDuration());//Set the length of SeekBar
                        m.start();
                        btn_start_audio.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_pause_circle_filled_24));

//                        if(m!=null){
//                            m.stop();
//                        }
//
//                        try {
//                            m.prepare();    //ready
//                        } catch (IllegalStateException | IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                            Log.d("TAG","play error");
//                        }
////                        m.start();  //Play

                        // Create a thread
                        Log.d("TAG","Play");
//                        btn_start_audio.setText("Play");
                    }
                    thread = new Thread(new SeekBarThread());
                    // start thread
                    thread.start();
                    Log.d("TAG","start thread");

                    break;
//                case R.id.Button02:
//                    m.stop();
//                    audio_seekBar.setProgress(0);
//                    break;
            }

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        now_time.setText( ShowTime(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Prevent conflicts with Thread updating the playback progress bar when dragging the progress bar for progress settings
        isChanging = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        now_time.setText( ShowTime(seekBar.getProgress()));
        //Set the media progress to the progress of the current seekbar
        m.seekTo(seekBar.getProgress());
        isChanging = false;
        thread = new Thread(new SeekBarThread());
        // start thread
        thread.start();
    }

    // custom thread
    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            if(m!=null) {
                while (!isChanging && m.isPlaying()) {
                    // Set the SeekBar position to the current playback position
                    audio_seekBar.setProgress(m.getCurrentPosition());
                    try {
                        // Update position every 100 milliseconds
                        Thread.sleep(100);
                        //Play progress
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("TAG", "run exception");
                    }

//                Log.d("TAG","run");
                }

            }
        }
    }

    //Time display function, we get music information in milliseconds, and convert it to the familiar 00:00 format
    public String ShowTime(int time) {
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    private void init(View view) {
        audio_seekBar = (SeekBar)view.findViewById(R.id.sBar);
        btn_start_audio = (ImageView) view.findViewById(R.id.play);
//        btn_stop_audio = (Button) view.findViewById(R.id.Button02);
        now_time = (TextView) view.findViewById(R.id.txtStartTime);

        btn_start_audio.setOnClickListener(new ClickEvent());
//        btn_stop_audio.setOnClickListener(new ClickEvent());
        audio_seekBar.setOnSeekBarChangeListener(this);


    }



}