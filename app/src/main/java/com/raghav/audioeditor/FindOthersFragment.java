package com.raghav.audioeditor;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.raghav.audioeditor.Cutterutils.TrimAudio;

import java.util.ArrayList;

public class FindOthersFragment extends Fragment {


    //private ActivityResultLauncher<Intent> onActivityResult;
    public FindOthersFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_find_others, container, false);
        ActivityResultLauncher<Intent> onActivityResult=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode()== Activity.RESULT_OK){
                            Intent intent=result.getData();
                            if(intent!=null){
                                ClipData clipData=intent.getClipData();
                                if(clipData!=null) {
                                    ArrayList<String> contentUris=new ArrayList<>();
                                    for (int i = 0; i < clipData.getItemCount(); i++) {
                                        ClipData.Item item=clipData.getItemAt(i);
                                        getActivity().getContentResolver().takePersistableUriPermission(item.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        contentUris.add(item.getUri().toString());
                                    }
                                    startActivity(new Intent(getActivity(), MergerActivity.class)
                                            .putStringArrayListExtra("selectedvideos",contentUris));
                                }else{
                                    Uri contentUri=intent.getData();
                                    getActivity().getContentResolver().takePersistableUriPermission(contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                    TrimAudio.activity(String.valueOf(contentUri))
                                            .setFilename(getFilename(contentUri))
                                            .setHideSeekBar(false)
                                            .start(requireActivity());                                }
                            }
                        }
                    }
                }
        );

        view.findViewById(R.id.trim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("audio/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                onActivityResult.launch(intent);
            }
        });

        view.findViewById(R.id.merge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("audio/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                onActivityResult.launch(intent);
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    private String getFilename(Uri uri){

        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME};
        Cursor c = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (c != null) {
            int nameIndex = c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
            c.moveToFirst();
            String fileName = c.getString(nameIndex);
            return fileName;
        }

        return "Edit Audio";
    }
}