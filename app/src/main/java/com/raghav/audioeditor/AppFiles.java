package com.raghav.audioeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.raghav.audioeditor.Cutterutils.TrimAudio;
import com.raghav.audioeditor.ListView.SongAdapter;
import com.raghav.audioeditor.ListView.SongModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppFiles extends AppCompatActivity implements SongAdapter.OnMusicItemClickListener {

//    private GridView gridView;
    private ListView listView;
    private ArrayList<SongModel> videoArrayList;
    private TextView textViewInfo;
    private SongAdapter adapter;
    private List<SongModel> songs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_files);
//        getSupportActionBar().setTitle("Converted Files");

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(0);
        videoArrayList = new ArrayList<>();


        textViewInfo =(TextView) findViewById(R.id.textViewInfo);
        adapter = new SongAdapter(AppFiles.this, videoArrayList,this);
        listView =(ListView) findViewById(R.id.listView);

        listView.setVisibility(View.VISIBLE);

//        gridView =(GridView) findViewById(R.id.gridView);
//        gridView.setNumColumns(2);


        new BackgroundTask(this) {
            @Override
            public void doInBackground() {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "audioeditor").build();

                SongDao userDao = db.songDao();
                songs = userDao.getAll();
                Log.d("TAG", "songs "+String.valueOf(songs.size()));
            }

            @Override
            public void onPostExecute() {
                videoArrayList.addAll(songs);
                if(videoArrayList.size()==0){
                    textViewInfo.setText("No files created yet!");
                }
                Log.d("TAG","postexe videos"+ String.valueOf(videoArrayList.size()));
                listView.setAdapter(adapter);

            }
        }.execute();

//        gridView.setVisibility(View.GONE);

    }


    @Override
    public void onMusicClick(int position, SongModel v) {

        try {
            InputStream inputStream = AppFiles.this.getContentResolver().openInputStream(Uri.parse(v.getUri()));
            inputStream.close();

            Fragment frag=getSupportFragmentManager().findFragmentByTag("musicplayer");
            if(frag!=null)
                AppFiles.this.getSupportFragmentManager().beginTransaction().remove(frag).commit();

            MusicPlayerFragment fragment= new MusicPlayerFragment();
            Bundle bundle=new Bundle();
            bundle.putString("uri",v.getUri());
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.relativeMerger, fragment,"musicplayer"); // fragment container id in first parameter is the  container(Main layout id) of Activity
            transaction.addToBackStack(null);  // this will manage backstack
            transaction.commit();

        } catch (Exception e) {
            Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
            Toast.makeText(AppFiles.this, "File does not exist!", Toast.LENGTH_SHORT).show();

            new BackgroundTask(this) {
                @Override
                public void doInBackground() {
                    AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "audioeditor").build();

                    SongDao userDao = db.songDao();
                    userDao.delete(v);
                    Log.d("TAG", "songs "+String.valueOf(songs.size()));
                }

                @Override
                public void onPostExecute() {
                    adapter.notifyDataSetChanged();
                }
            }.execute();


        }
    }


    public abstract class BackgroundTask {

        private Activity activity;
        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {

                    doInBackground();
                    activity.runOnUiThread(new Runnable() {
                        public void run() {

                            onPostExecute();
                        }
                    });
                }
            }).start();
        }
        public void execute(){
            startBackground();
        }

        public abstract void doInBackground();
        public abstract void onPostExecute();

    }
}