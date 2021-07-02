package com.raghav.audioeditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.d.lib.pulllayout.rv.PullRecyclerView;
import com.d.lib.pulllayout.rv.itemtouchhelper.OnStartDragListener;
import com.d.lib.pulllayout.rv.itemtouchhelper.SimpleItemTouchHelperCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.raghav.audioeditor.ListView.SongModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MergerActivity extends AppCompatActivity {

    String fileType="mp3";
    String processType="join";
    String curve="exp";
    int duration=1;
    boolean overlap=false,crossfade=false;


    private static final int WRITE_REQUEST_CODE = 111;
    private static final int FOLDER_REQUEST_CODE = 123;
    private long finalVideoLength=0;
    private int fps,height,width,quality;
    private ArrayList<SongModel> selectedUris;
    private ArrayList<SongModel> selectedVideos;
    private PullRecyclerView recyclerView;
    private ItemTouchHelper mItemTouchHelper;
    private Button mergebtn;
    private ImageButton addMorebtn;
    private DragAdapter listAdapter;
    //false->landscape
    private final boolean orientation=false;

    private String title="",artist="",album="",genre="",year="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merger);

        Intent intent=getIntent();
        selectedVideos=new ArrayList<>();
        selectedVideos=intent.getParcelableArrayListExtra("selectedvideos");

        for(SongModel uri: selectedVideos){
            Log.d("URI",uri.getUri());
        }

//        getAllVideoDetailsFromUri();

        recyclerView = findViewById(R.id.rv_list);
        recyclerView.setCanPullDown(false);
        recyclerView.setCanPullUp(false);
        recyclerView.setHasFixedSize(true);

        listAdapter = new DragAdapter(this,selectedVideos,R.layout.videolist_item_lv);
        recyclerView.setAdapter(listAdapter);

        final ItemTouchHelper.Callback itemTouchHelperCallback = new SimpleItemTouchHelperCallback(listAdapter);
        mItemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        listAdapter.setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                Log.d("VM LIST:","onStartDrag");
                mItemTouchHelper.startDrag(viewHolder);

            }
        });
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mergebtn= findViewById(R.id.merge);
        mergebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(listAdapter.getDatas().size()>=2)
                    mergeOptionDialog();
                else
                    Toast.makeText(MergerActivity.this, "Please choose at least 2 files to merge.", Toast.LENGTH_SHORT).show();

            }
        });
        addMorebtn= findViewById(R.id.addMore);
        addMorebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG","addMorebtn");

                Intent intent=new Intent();
                intent.putExtra("addmore",1);
                intent.putParcelableArrayListExtra("videolist",new ArrayList<>(listAdapter.getDatas()));
                setResult(AudioMergerFragment.RESULT_CODE,intent);
                finish();
            }
        });
    }


    private String timeConversion(long value) {
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

    private void mergeOptionDialog(){
        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme);
//        builder.setTitle("Merge Options");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.merge_dialog_layout,null);

        Button configure= viewInflated.findViewById(R.id.btnConfigure);
        CheckBox checkCrossfade= viewInflated.findViewById(R.id.checkCrossfade);
        RadioGridGroup fileRadioGroup= viewInflated.findViewById(R.id.typeRadioGroup);
        RadioGroup processRadioGroup= viewInflated.findViewById(R.id.processTypeRadioGroup);
        fileRadioGroup.check(R.id.radioMp3);
        configure.setEnabled(false);
        checkCrossfade.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                configure.setEnabled(!configure.isEnabled());
            }
        });
        configure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configureOptionDialog();
            }
        });
        builder.setView(viewInflated);
        builder.setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch(fileRadioGroup.getCheckedRadioButtonId()){
                    case R.id.radioMp3:fileType="mp3";
                        break;
                    case R.id.radioAac:fileType="aac";
                        break;
                    case R.id.radioM4a:fileType="m4a";
                        break;
                    case R.id.radioOgg:fileType="ogg";
                        break;
                    case R.id.radioWav:fileType="wav";
                        break;
                    case R.id.radioFlac:fileType="flac";
                        break;

                }
                switch(processRadioGroup.getCheckedRadioButtonId()){
                    case R.id.radioJoin:processType="join";
                        break;
                    case R.id.radioMix:processType="mix";
                        break;
                }
                crossfade=checkCrossfade.isChecked();

                saveOptionDialog();
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

    private void configureOptionDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        builder.setTitle("Configure");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.configure_dialog_layout, null);
        CheckBox checkOverlap= viewInflated.findViewById(R.id.checkOverlap);
        RangeSlider durationSeekbar= viewInflated.findViewById(R.id.durationSeekbar);
        Spinner curveSpinner= viewInflated.findViewById(R.id.curveSpinner);
        TextView durationTV= viewInflated.findViewById(R.id.durationSeekbarTV);

        durationSeekbar.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        durationSeekbar.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                durationTV.setText(String.format(Locale.US,"%d second",(int)value));
                duration=(int)value;
            }
        });
//        durationSeekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
//            @Override
//            public void valueChanged(Number value) {
//                durationTV.setText(String.format(Locale.US,"%d second",value.intValue()));
//            }
//        });


        builder.setView(viewInflated);
        builder.setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch(curveSpinner.getSelectedItemPosition()){
                    case 0:curve="exp";
                        break;
                    case 1:curve="log";
                        break;
                    case 2:curve="tri";
                        break;
                }
                overlap=checkOverlap.isChecked();
//                int duration=durationSeekbar.getPosition();

                dialog.dismiss();
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

    private void saveOptionDialog(){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme);
        builder.setTitle("Save File");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.save_dialog_layout,null);

        TextInputEditText filenameEditText = viewInflated.findViewById(R.id.filename);
        TextInputEditText titleEditText = viewInflated.findViewById(R.id.title);
        TextInputEditText albumEditText = viewInflated.findViewById(R.id.album);
        TextInputEditText artistEditText = viewInflated.findViewById(R.id.artist);
        TextInputEditText genreEditText = viewInflated.findViewById(R.id.genre);
        TextInputEditText yearEditText = viewInflated.findViewById(R.id.year);
        TextView moreOptions= viewInflated.findViewById(R.id.moreOptions);

        TextInputLayout filenameTIL = viewInflated.findViewById(R.id.filenameTIL);
        TextInputLayout titleTIL = viewInflated.findViewById(R.id.titleTIL);
        TextInputLayout albumTIL = viewInflated.findViewById(R.id.albumTIL);
        TextInputLayout artistTIL = viewInflated.findViewById(R.id.artistTIL);
        TextInputLayout genreTIL = viewInflated.findViewById(R.id.genreTIL);
        TextInputLayout yearTIL = viewInflated.findViewById(R.id.yearTIL);

        filenameTIL.setSuffixText("."+fileType);
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

    private void executeFfmpeg(String outputFile,String fileName,Uri safuri){

        /*
          //audio ffmpeg
        * ffmpeg -i 2cwav.wav -i 1cmp3.mp3 -i 2caac.aac -filter_complex
        * "concat=n=3:v=0:a=1[a]" -map "[a]" -codec:a libmp3lame -b:a 256k output.mp3

        * //working with crossfade->wiyh overlapping
            * ffmpeg -i 2cmp3.mp3 -i 2cwav.wav -i 1cwav.wav -i 1cmp3.mp3 -vn
            * -filter_complex
              "[0][1]acrossfade=d=5:c1=tri:c2=tri[a01];
               [a01][2]acrossfade=d=5:c1=tri:c2=tri[a02];
               [a02][3]acrossfade=d=5:c1=tri:c2=tri" out.mp3

          //working Cross fade from one input to another but without overlapping:
            * ffmpeg -i 2cmp3.mp3 -i 2cwav.wav -i 1cwav.wav -i 1cmp3.mp3 -vn
            *  -filter_complex "[0][1]acrossfade=d=5:o=0:c1=tri:c2=tri[a01];
            * [a01][2]acrossfade=d=5:o=0:c1=tri:c2=tri[a02];[a02][3]
            * acrossfade=d=5:o=0:c1=tri:c2=tri" out.mp3

        *
            //working mix audio
            * ffmpeg -i 2cmp3.mp3 -i diffmp3.mp3 -filter_complex
            * amix=inputs=2:duration=longest output.mp3

         */

        StringBuilder metadata=new StringBuilder(" ");
        metadata.append("-metadata title=\"").append(title).append("\" -metadata artist=\"").append(artist)
                .append("\" -metadata album=\"").append(album).append("\" -metadata genre=\"").append(genre)
                .append("\" -metadata date=\"").append(year).append("\"");

        StringBuilder i_paths=new StringBuilder();
        StringBuilder filterComplex=new StringBuilder();
        StringBuilder concate=new StringBuilder();

        int i=0;
        List<SongModel> finalList=listAdapter.getDatas();

        for (i = 0; i < finalList.size(); i++) {
            SongModel s = finalList.get(i);
            finalVideoLength += s.getDurationLong();
            String path = FFmpegKitConfig.getSafParameterForRead(MergerActivity.this, Uri.parse(s.getUri()));
            i_paths.append("-i ").append(path).append(" ");
        }

        if(processType.equalsIgnoreCase("join")){
            if(crossfade){
                filterComplex.append("-vn -filter_complex \"");

                if(overlap){
                    if(finalList.size()==2){
                        filterComplex.append("[0][1]acrossfade=d=")
                                .append(duration).append(":o=0").append(":c1=").append(curve)
                                .append(":c2=tri\" ");
                    }else {
                        filterComplex.append("[0][1]acrossfade=d=")
                                .append(duration).append(":o=0").append(":c1=").append(curve)
                                .append(":c2=tri;");
                        for (i = 2; i < finalList.size(); i++) {
                            if((i+1)==finalList.size()){
                                filterComplex.append("[a0").append(i-1).append("][").append(i)
                                        .append("]acrossfade=d=").append(duration).append(":c1=")
                                        .append(curve).append(":c2=").append(curve).append("\"");
                            }else{
                                filterComplex.append("[a0").append(i-1).append("][").append(i)
                                        .append("]acrossfade=d=").append(duration).append(":c1=")
                                        .append(curve).append(":c2=").append(curve).append("[a0")
                                        .append(i).append("];");
                            }
                        }
                    }
                }else{
                    if(finalList.size()==2){
                        filterComplex.append("[0][1]acrossfade=d=")
                                .append(duration).append(":c1=").append(curve)
                                .append(":c2=").append(curve).append("\" ");
                    }else {
                        filterComplex.append("[0][1]acrossfade=d=")
                                .append(duration).append(":c1=").append(curve)
                                .append(":c2=").append(curve).append("[a01];");
                        for (i = 2; i < finalList.size(); i++) {
                            if((i+1)==finalList.size()){
                                filterComplex.append("[a0").append(i-1).append("][").append(i)
                                        .append("]acrossfade=d=").append(duration).append(":c1=")
                                        .append(curve).append(":c2=").append(curve).append("\" ");
                            }else{
                                filterComplex.append("[a0").append(i-1).append("][").append(i)
                                        .append("]acrossfade=d=").append(duration).append(":c1=")
                                        .append(curve).append(":c2=").append(curve).append("[a0")
                                        .append(i).append("];");
                            }
                        }
                    }
                }

                filterComplex.append(outputFile);


            }else{
                filterComplex.append("-filter_complex \"concat=n=").append(i)
                        .append(":v=0:a=1[a]\" -map \"[a]\" ")
                        .append(outputFile);
            }
        }else{
            filterComplex.append("-filter_complex ").append("amix=inputs=").append(i).append(":duration=longest ").append(outputFile);
        }

        String exe=i_paths.toString()+filterComplex.toString()+concate.toString();

        Log.d("QUERY",exe);
        startActivity(new Intent(this,FFmpegExecutionActivity.class)
                .putExtra("exe",exe)
                .putExtra("filename",fileName)
                .putExtra("videolength",finalVideoLength)
                .putExtra("safuri",String.valueOf(safuri)));
//        finishAffinity();
        finish();
//                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP)    );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK ){

            switch (requestCode){
                case WRITE_REQUEST_CODE:
                    if(data!=null){
                        Uri uri=data.getData();
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        String videoPath = FFmpegKitConfig.getSafParameterForWrite(MergerActivity.this, uri);

                        executeFfmpeg(videoPath,getSAFUriInfo(uri),uri);
                        Log.d("SAFURI", String.valueOf(uri));
                    }
                    break;
                case FOLDER_REQUEST_CODE:
                    if (data != null) {
                        getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        DocumentFile documentFile=DocumentFile.fromTreeUri(this,data.getData());
                        DocumentFile file=documentFile.createFile("audio/mp3","VID-"+System.currentTimeMillis());

                        String videoPath = FFmpegKitConfig.getSafParameterForWrite(MergerActivity.this, file.getUri());
                        executeFfmpeg(videoPath,file.getName(),file.getUri());

                        Log.d("URI", String.valueOf(file.getUri()));
                    }
                    break;
            }
        }
    }

    public void createFile(String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
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


        //working perfectly mpeg(mp3), flac, aac
        //working but recognised by android->wav,m4a
        //obb not working,ffmpeg error

        //"audio/mp4a-latm"

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(intent,WRITE_REQUEST_CODE);
    }

    private void chooseFolder(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(intent,FOLDER_REQUEST_CODE);
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
}