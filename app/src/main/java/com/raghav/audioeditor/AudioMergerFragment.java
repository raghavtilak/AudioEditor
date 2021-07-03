package com.raghav.audioeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.raghav.audioeditor.ListView.SongAdapter;
import com.raghav.audioeditor.ListView.SongModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.Looper.getMainLooper;

public class AudioMergerFragment extends Fragment implements SongAdapter.OnMusicItemClickListener {


    private ArrayList<SongModel> addMoreList;

    public static final int REQUEST_CODE=11;
    public static final int RESULT_CODE=12;

    private ListView listView;
    private GridView gridView;
    private ArrayList<SongModel> videoArrayList;
    private ArrayList<SongModel> selectedvideoArrayList;
    private SongAdapter adapter;
    private EditText searchEditText;
    private int isAddMore=0;

    public AudioMergerFragment() {
        // Required empty public constructor
        Log.d("TAG","fragctor");

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.d("TAG","oncreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("TAG","oncreateview");

        View view=inflater.inflate(R.layout.fragment_video_select, container, false);

       // MaterialToolbar toolbar=view.findViewById(R.id.toolbar);
        //getActivity().getActionBar().setSupportActionBar(toolbar);
      //  actionBar =((AppCompatActivity)getActivity()).getSupportActionBar();

        selectedvideoArrayList=new ArrayList<>();
        videoArrayList = new ArrayList<>();
        addMoreList=new ArrayList<>();

        ProgressDialog progressDialog=new ProgressDialog(getActivity(),R.style.ProgressDialog);
        progressDialog.setMessage("Fetching files..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        adapter = new SongAdapter (getActivity(), videoArrayList,this);
        listView =(ListView) view.findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(selectedvideoArrayList.contains(videoArrayList.get(i))){
                    selectedvideoArrayList.remove(videoArrayList.get(i));
                }else{
                    selectedvideoArrayList.add(videoArrayList.get(i));
                }
               // toolbar.setVisibility(View.GONE);
                actionMode.setTitle(String.valueOf(selectedvideoArrayList.size()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.menu_items, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.submit:
                        boolean exists=true;
//                        ArrayList<String> videoUris=new ArrayList<>();
                        for(SongModel u:selectedvideoArrayList){

                          if(!validateUri(Uri.parse(u.getUri()))) {
                              exists = false;
                          }
                        }
                        if(exists) {
                            if(isAddMore==1) {
                                    selectedvideoArrayList.addAll(addMoreList);
                                    Intent i = new Intent(getActivity(), MergerActivity.class);
                                    i.putParcelableArrayListExtra("selectedvideos", selectedvideoArrayList);
                                    startActivityForResult(i, REQUEST_CODE);

                                    listView.clearChoices();
                                    adapter.notifyDataSetChanged();
                                    actionMode.finish();

                                    addMoreList.clear();
                                    isAddMore=0;

                                    return true;
                            }else{
                                if (selectedvideoArrayList.size() != 1) {

//                                    videoUris.addAll(addMoreList);
                                    Intent i = new Intent(getActivity(), MergerActivity.class);
                                    i.putParcelableArrayListExtra("selectedvideos", selectedvideoArrayList);
                                    startActivityForResult(i, REQUEST_CODE);

                                    listView.clearChoices();
                                    adapter.notifyDataSetChanged();
                                    actionMode.finish();


//                                    addMoreList.clear();
                                    return true;
                                } else {
                                    Toast.makeText(getActivity(), "Please select at least 2 files to merge.", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            }
                        }else{
                            Toast.makeText(getActivity(), "One or more selected file don't exist!", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    default:return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selectedvideoArrayList.clear();
               // toolbar.setVisibility(View.VISIBLE);
            }


        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAddMore==1) {
                    boolean exists=true;
//                    ArrayList<String> videoUris=new ArrayList<>();
                    Uri uri=Uri.parse(videoArrayList.get(i).getUri());
                    if(!validateUri(uri)) {
                        exists=false;
                    }
                    if(exists){
                        addMoreList.add(videoArrayList.get(i));
                        Intent intent = new Intent(getActivity(), MergerActivity.class);
                        intent.putParcelableArrayListExtra("selectedvideos", addMoreList);
                        startActivityForResult(intent, REQUEST_CODE);
                        addMoreList.clear();
                        isAddMore=0;
                    }
                }else{
                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(videoArrayList.get(i).getUri()));
                        inputStream.close();

                        showDetailsDialog(videoArrayList.get(i));

                    } catch (Exception e) {
                        Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
                        Toast.makeText(getActivity(), "File does not exist!", Toast.LENGTH_SHORT).show();
                    }                }
            }
        });
        listView.setVisibility(View.VISIBLE);

        gridView =(GridView) view.findViewById(R.id.gridView);
        gridView.setNumColumns(2);
        
        gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(selectedvideoArrayList.contains(videoArrayList.get(i))){
                    selectedvideoArrayList.remove(videoArrayList.get(i));
                }else{
                    selectedvideoArrayList.add(videoArrayList.get(i));
                }
                // toolbar.setVisibility(View.GONE);
                actionMode.setTitle(String.valueOf(selectedvideoArrayList.size()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.menu_items, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.submit:
                        boolean exists=true;
//                        ArrayList<String> videoUris=new ArrayList<>();
                        for(SongModel u:selectedvideoArrayList){

                            if(!validateUri(Uri.parse(u.getUri()))) {
                                exists = false;
                            }
                        }
                        if(exists) {
                            if(isAddMore==1) {
                                selectedvideoArrayList.addAll(addMoreList);
                                Intent i = new Intent(getActivity(), MergerActivity.class);
                                i.putParcelableArrayListExtra("selectedvideos", selectedvideoArrayList);
                                startActivityForResult(i, REQUEST_CODE);

                                gridView.clearChoices();
                                adapter.notifyDataSetChanged();
                                actionMode.finish();

                                addMoreList.clear();
                                isAddMore=0;

                                return true;
                            }else{
                                if (selectedvideoArrayList.size() != 1) {

//                                    videoUris.addAll(addMoreList);
                                    Intent i = new Intent(getActivity(), MergerActivity.class);
                                    i.putParcelableArrayListExtra("selectedvideos", selectedvideoArrayList);
                                    startActivityForResult(i, REQUEST_CODE);

                                    gridView.clearChoices();
                                    adapter.notifyDataSetChanged();
                                    actionMode.finish();


//                                    addMoreList.clear();
                                    return true;
                                } else {
                                    Toast.makeText(getActivity(), "Please select at least 2 files to merge.", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            }
                        }else{
                            Toast.makeText(getActivity(), "One or more selected file don't exist!", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    default:return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selectedvideoArrayList.clear();
                // toolbar.setVisibility(View.VISIBLE);
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAddMore==1) {
                    boolean exists=true;
//                    ArrayList<String> videoUris=new ArrayList<>();
                    Uri uri=Uri.parse(videoArrayList.get(i).getUri());
                    if(!validateUri(uri)) {
                        exists=false;
                    }
                    if(exists){
                        addMoreList.add(videoArrayList.get(i));
                        Intent intent = new Intent(getActivity(), MergerActivity.class);
                        intent.putParcelableArrayListExtra("selectedvideos", addMoreList);
                        startActivityForResult(intent, REQUEST_CODE);
                        addMoreList.clear();
                        isAddMore=0;
                    }
                }else{
                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(videoArrayList.get(i).getUri()));
                        inputStream.close();

                        showDetailsDialog(videoArrayList.get(i));

                    } catch (Exception e) {
                        Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
                        Toast.makeText(getActivity(), "File does not exist!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        gridView.setVisibility(View.GONE);

        new BackgroundTask(getActivity()) {
            @Override
            public void doInBackground() {
                Log.d("ASYNC","doInBackground");
                try {
                    getVideos();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPostExecute() {
                Log.d("ASYNC","doInBackground");
                listView.setAdapter(adapter);
                progressDialog.dismiss();
            }
        }.execute();

        return view;
    }

    public void getVideos() throws FileNotFoundException {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST
        };

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        try (Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(
                collection,
                projection,
                null,
                null,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int dateColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int artistColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);

            while (cursor.moveToNext()) {
                // Get values of columns for a given Audio.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                long duration = cursor.getInt(durationColumn);
                long date = cursor.getInt(dateColumn);
                int size = cursor.getInt(sizeColumn);
                String artist = cursor.getString(artistColumn);
                String album = cursor.getString(albumColumn);
                float sizeTomb=size/(1024f*1024f);

                Uri contentUri=null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), id);
                }else{
                    contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                }

                //this if check takes a lot of time
                //removing this makes loading incredibly fast
//                if(chkImgUri(getActivity(),contentUri)){
                    videoArrayList.add(new SongModel(album,artist,name, timeConversion(duration)
                            ,duration,String.valueOf(date*1000),
                            ((double)Math.round(sizeTomb*100)/100)+" mb",String.valueOf(contentUri)));
//                }
            }
    }

    }


    @Override
    public void onMusicClick(int position, SongModel musicItem) {
//        showMediaPlayerDialog();
//        audioPlayer(musicItem);

        Fragment frag=((AppCompatActivity)getActivity()).getSupportFragmentManager().findFragmentByTag("musicplayer");
        if(frag!=null)
            getActivity().getSupportFragmentManager().beginTransaction().remove(frag).commit();

        MusicPlayerFragment fragment= new MusicPlayerFragment();
        Bundle bundle=new Bundle();
        bundle.putString("uri",musicItem.getUri());
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.relativeMerger, fragment,"musicplayer"); // fragment container id in first parameter is the  container(Main layout id) of Activity
        transaction.addToBackStack(null);  // this will manage backstack
        transaction.commit();


    }
//
//    private void audioPlayer(SongModel musicItem) {
//        mPlayer = new MediaPlayer();
//        try {
//            mPlayer.setDataSource(getActivity(), Uri.parse(musicItem.getUri()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            mPlayer.setAudioAttributes(new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .build());
//        } else {
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        }
//
//        new Handler(getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mPlayer.prepareAsync();
//            }
//        }, 200);
//
//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                mPlayer.stop();
//                mPlayer.reset();
//                playbtn.setSelected(true);
//            }
//        });
//        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                playbtn.setVisibility(View.VISIBLE);
//                playbtn.setSelected(true);
//                rlPlayer.setVisibility(View.VISIBLE);
//            }
//        });
//
////        songPrgs = findViewById(R.id.sBar);
//        songPrgs.setClickable(false);
//
//
//        playbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (playbtn.isSelected()) {
//                    playbtn.setSelected(false);
//                    if (mPlayer.isPlaying()) {
//                        mPlayer.pause();
//                    } else
//                        mPlayer.start();
//
//                    eTime = mPlayer.getDuration();
//                    sTime = mPlayer.getCurrentPosition();
//                    if (oTime == 0) {
//                        songPrgs.setMax(eTime);
//                        oTime = 1;
//                    }
//                    songTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
//                            TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
//
//                    startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
//                            TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
//                    songPrgs.setProgress(sTime);
//                    hdlr.postDelayed(UpdateSongTime, 100);
//                    Log.d("time", eTime + "ok ok" + sTime);
//                } else {
//                    playbtn.setSelected(true);
//                    mPlayer.pause();
//                }
//            }
//        });
//    }
//
//    private void showMediaPlayerDialog(){
//        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
//        builder.setCancelable(true);
//
//        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.musicplayer_layout, null);
//        rlPlayer = viewInflated.findViewById(R.id.mediaRLayout);
//        rlPlayer.setVisibility(View.VISIBLE);
//        startTime = viewInflated.findViewById(R.id.txtStartTime);
//        songTime = viewInflated.findViewById(R.id.txtSongTime);
//        playbtn = viewInflated.findViewById(R.id.play);
//        songPrgs = viewInflated.findViewById(R.id.sBar);
//        songPrgs.setClickable(false);
//
//        builder.setView(viewInflated);
//        builder.show();
//    }

    public String timeConversion(long value) {
        String videoTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            videoTime = String.format("%02d:%02d", mns, scs);
        }
        return videoTime;
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_mainact,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort:
                ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.sort,android.R.layout.select_dialog_singlechoice);
                MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(getActivity(),R.style.AlertDialogTheme);
                dialogBuilder.setTitle("Sort");
                dialogBuilder.setSingleChoiceItems(sortAdapter, 4, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                Comparator<SongModel> compareByDate=(SongModel v1,SongModel v2)->
                                        v1.getDate().compareTo(v2.getDate());
                                adapter.sort(compareByDate);
                                break;
                            case 1:
                                Comparator<SongModel> compareByFilename=(SongModel v1,SongModel v2)->
                                        v1.getTitle().compareTo(v2.getTitle());
//                                Collections.sort(videoArrayList,compareByFilename);
                                adapter.sort(compareByFilename);
//                                adapter.setList(videoArrayList);
//                                adapter.notifyDataSetChanged();
                                break;
                            case 2:
                                Comparator<SongModel> compareBySize=(SongModel v1,SongModel v2)->
                                        v1.getSize().compareTo(v2.getSize());
//                                Collections.sort(videoArrayList,compareBySize);
                                adapter.sort(compareBySize);
                                break;
                            case 3:
                                Comparator<SongModel> compareByDuration=(SongModel v1,SongModel v2)->
                                        v1.getDuration().compareTo(v2.getDuration());
//                                Collections.sort(videoArrayList,compareByDuration);
                                adapter.sort(compareByDuration);
                                break;
                            case 4:
                                compareByDate=(SongModel v1,SongModel v2)->
                                        v1.getDate().compareTo(v2.getDate());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    adapter.sort(compareByDate.reversed());
                                }
                                break;
                            case 5:
                                compareByFilename = (SongModel v1, SongModel v2) ->
                                        v1.getTitle().compareTo(v2.getTitle());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                    Collections.sort(videoArrayList,compareByFilename.reversed());
                                    adapter.sort(compareByFilename.reversed());
                                }
                                break;
                            case 6:
                                compareBySize = (SongModel v1, SongModel v2) ->
                                        v1.getSize().compareTo(v2.getSize());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                    Collections.sort(videoArrayList,compareBySize.reversed());
                                    adapter.sort(compareBySize.reversed());
                                }
                                break;
                            case 7:
                                compareByDuration = (SongModel v1, SongModel v2) ->
                                        v1.getDuration().compareTo(v2.getDuration());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                    Collections.sort(videoArrayList,compareByDuration.reversed());
                                    adapter.sort(compareByDuration.reversed());
                                }
                                break;
                        }
                    }
                });
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dialogBuilder.setCancelable(false);
                dialogBuilder.show();
                return true;
            case R.id.changeLayout:

                if(gridView.getVisibility()==View.VISIBLE){
                    gridView.setVisibility(View.GONE);
                    adapter.setItemView(R.layout.videolist_item_lv);
                    listView.setAdapter(adapter);
                    listView.setVisibility(View.VISIBLE);
                    item.setIcon((R.drawable.ic_action_grid));
                }else{
                    listView.setVisibility(View.GONE);
                    adapter.setItemView(R.layout.videolist_item_gv);
                    gridView.setAdapter(adapter);
                    gridView.setVisibility(View.VISIBLE);
                    item.setIcon((R.drawable.ic_action_list));
                }

                return true;
            case R.id.files:

                getActivity().startActivity(new Intent(getActivity(),AppFiles.class));

                return true;
            default: return false;
        }
    }

    public boolean chkImgUri(Context context,Uri in_imgUri) {

        boolean res;

        ContentResolver cr = context.getContentResolver();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = cr.query(Uri.parse(in_imgUri.toString()), projection, null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                String filePath = cur.getString(0);
                // true= if it exists
                // false= File was not found
                res = new File(filePath).exists();

            } else {
                // Uri was ok but no entry found.
                res = false;
            }
            cur.close();
        } else {
            // content Uri was invalid or some other error occurred
            res = false;
        }

        return res;
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d("TAG","inresume");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(data!=null) {
            isAddMore = data.getIntExtra("addmore", 0);
            addMoreList=data.getParcelableArrayListExtra("videolist");
            Log.d("TAG", String.valueOf(isAddMore));

            for(SongModel s:addMoreList)
                Log.d("TAG",s.getUri());

        }
    }

    private boolean validateUri(Uri uri){
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            inputStream.close();
            return true;
        } catch (Exception e) {
            Log.d("MY_TAG", "File corresponding to the uri does not exist \n"+e);
            Toast.makeText(getActivity(), "Selected file doesn't exists.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void showDetailsDialog(SongModel s){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(),R.style.AlertDialogTheme);
        builder.setTitle("Details");
        builder.setCancelable(false);

        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.details_dialog_layout,null);
        TextView title= viewInflated.findViewById(R.id.title);
        TextView album= viewInflated.findViewById(R.id.album);
        TextView artist= viewInflated.findViewById(R.id.artist);
        TextView duration= viewInflated.findViewById(R.id.duration);
        TextView size= viewInflated.findViewById(R.id.size);
        TextView date= viewInflated.findViewById(R.id.date);

        title.setText(s.getTitle());
        album.setText(s.getAlbum());
        artist.setText(s.getArtist());
        duration.setText(s.getDuration());
        size.setText(s.getSize());
        date.setText(new SimpleDateFormat("dd/MM/yy").format(new Date(Long.parseLong(s.getDate()))));

        Log.d("TAG",s.getDate());
        builder.setView(viewInflated);





        builder.setPositiveButton("Set as RingTone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(Settings.System.canWrite(getActivity())) {
                        try {
                            RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_RINGTONE, Uri.parse(s.getUri()));
                            Toast.makeText(getActivity(), "Ringtone Set!!", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(getActivity(), "Ringtone not set!", Toast.LENGTH_SHORT).show();
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
        builder.show();
    }

    private void showPermissionWarningDialog(){

        MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(getActivity());
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