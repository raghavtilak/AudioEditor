package com.raghav.audioeditor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AppFiles extends AppCompatActivity implements SongAdapter.OnMusicItemClickListener,SongAdapter.OnMoreItemClickListener
{

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
        adapter = new SongAdapter(AppFiles.this, videoArrayList,this,this);
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

    @Override
    public void onMoreClick(int position, SongModel musicItem) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(musicItem.getUri()));
            inputStream.close();

            showDetailsDialog(musicItem);

        } catch (IOException e) {
            Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
            Toast.makeText(this, "File does not exist!", Toast.LENGTH_SHORT).show();
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

    private void showDetailsDialog(SongModel s){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this,R.style.DetailsAlertDialogTheme);
        builder.setTitle("Details");
        builder.setCancelable(true);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.details_dialog_layout,null);
        TextView title= viewInflated.findViewById(R.id.title);
        TextView album= viewInflated.findViewById(R.id.album);
        TextView artist= viewInflated.findViewById(R.id.artist);
        TextView duration= viewInflated.findViewById(R.id.duration);
        TextView size= viewInflated.findViewById(R.id.size);
        TextView date= viewInflated.findViewById(R.id.date);

        title.setText(s.getTitle());
        if(s.getAlbum()!=null)
            album.setText(s.getAlbum());
        else
            album.setText("<unkown>");

        if(s.getAlbum()!=null)
            album.setText(s.getArtist());
        else
            album.setText("<unkown>");

        duration.setText(s.getDuration());
        size.setText(s.getSize());
        date.setText(new SimpleDateFormat("dd/MM/yy").format(new Date(Long.parseLong(s.getDate()))));

        builder.setPositiveButton("Set as RingTone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(Settings.System.canWrite(AppFiles.this)) {
                        try {
                            RingtoneManager.setActualDefaultRingtoneUri(AppFiles.this, RingtoneManager.TYPE_RINGTONE, Uri.parse(s.getUri()));
                            Toast.makeText(AppFiles.this, "Ringtone Set!!", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(AppFiles.this, "Ringtone not set!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        showPermissionWarningDialog();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setView(viewInflated);
        builder.show();
    }

    private void showPermissionWarningDialog(){

        MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("Allow Permission");
        dialogBuilder.setCancelable(false);
        dialogBuilder.setMessage("Please grant permission to modify system settings." +
                "\nThis is required by this app in order to set the selected song as your device ringtone.");
        dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialogBuilder.setPositiveButton("GRANT", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent= new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);
            }
        });

        dialogBuilder.show();
    }

}