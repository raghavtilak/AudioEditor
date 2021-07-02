package com.raghav.audioeditor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.raghav.audioeditor.Cutterutils.TrimAudio;
import com.raghav.audioeditor.ListView.SongAdapter;
import com.raghav.audioeditor.ListView.SongModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;

public class AudioCutterFragment extends Fragment implements SongAdapter.OnMusicItemClickListener {

    private GridView gridView;
    private ListView listView;
    private ArrayList<SongModel> videoArrayList;

    private SongAdapter adapter;

    public AudioCutterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_video_cutter, container, false);

        videoArrayList = new ArrayList<>();

        adapter = new SongAdapter(getActivity(), videoArrayList,this);
        listView =(ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongModel v=adapter.getItem(i);
//                        startActivity(new Intent(getActivity(),CutterActivity.class)
//                                                .putExtra("uri",v.getFileUri().toString()));
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(v.getUri()));
                    inputStream.close();

                    TrimAudio.activity(String.valueOf(v.getUri()))
                            .setFilename(v.getTitle())
                            .setHideSeekBar(false)
                            .start(getActivity());
//                    startActivity(new Intent(getActivity(),Cutter.class).putExtra("uri",v.getUri()));


                } catch (Exception e) {
                    Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
                    Toast.makeText(getActivity(), "File does not exist!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongModel v=adapter.getItem(i);
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(v.getUri()));
                    inputStream.close();

                    Fragment frag=((AppCompatActivity)getActivity()).getSupportFragmentManager().findFragmentByTag("musicplayer");
                    if(frag!=null)
                        getActivity().getSupportFragmentManager().beginTransaction().remove(frag).commit();

                    MusicPlayerFragment fragment= new MusicPlayerFragment();
                    Bundle bundle=new Bundle();
                    bundle.putString("uri",v.getUri());
                    fragment.setArguments(bundle);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.relativeMerger, fragment,"musicplayer"); // fragment container id in first parameter is the  container(Main layout id) of Activity
                    transaction.addToBackStack(null);  // this will manage backstack
                    transaction.commit();

                } catch (Exception e) {
                    Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
                    Toast.makeText(getActivity(), "File does not exist!", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
        listView.setVisibility(View.VISIBLE);

        gridView =(GridView) view.findViewById(R.id.gridView);
        gridView.setNumColumns(2);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongModel v=adapter.getItem(i);
//                        startActivity(new Intent(getActivity(),CutterActivity.class)
//                                                .putExtra("uri",v.getFileUri().toString()));
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(v.getUri()));
                    inputStream.close();

                    TrimAudio.activity(v.getUri())
                            .setFilename(v.getTitle())
                            .setHideSeekBar(false)
                            .start(getActivity());
                } catch (Exception e) {
                    Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
                    Toast.makeText(getActivity(), "File does not exist!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongModel v=adapter.getItem(i);
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(v.getUri()));
                    inputStream.close();

                    Fragment frag=((AppCompatActivity)getActivity()).getSupportFragmentManager().findFragmentByTag("musicplayer");
                    if(frag!=null)
                        getActivity().getSupportFragmentManager().beginTransaction().remove(frag).commit();

                    MusicPlayerFragment fragment= new MusicPlayerFragment();
                    Bundle bundle=new Bundle();
                    bundle.putString("uri",v.getUri());
                    fragment.setArguments(bundle);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.relativeMerger, fragment,"musicplayer"); // fragment container id in first parameter is the  container(Main layout id) of Activity
                    transaction.addToBackStack(null);  // this will manage backstack
                    transaction.commit();

                } catch (Exception e) {
                    Log.w("MY_TAG", "File corresponding to the uri does not exist \n"+e);
                    Toast.makeText(getActivity(), "File does not exist!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        gridView.setVisibility(View.GONE);

        ProgressDialog progressDialog=new ProgressDialog(getActivity(),R.style.ProgressDialog);
        progressDialog.setMessage("Fetching files..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new BackgroundTask(getActivity()) {
            @Override
            public void doInBackground() {
                try {
                    getVideos();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPostExecute() {
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
                MediaStore.Audio.Media.TITLE,
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
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
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
                        ,duration,String.valueOf(date),
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
                                Comparator<SongModel> compareByDate=(SongModel v1, SongModel v2)->
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
            default: return false;
        }
    }

    private boolean chkImgUri(Context context,Uri in_imgUri) {

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
  }