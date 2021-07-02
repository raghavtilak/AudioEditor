package com.raghav.audioeditor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_READ = 111;
    private static final String PREF_FILE = "VideoMergerPref";
    public static ViewPager2 viewPager;
    private String[] titles = new String[]{"Merger", "Find Others", "Cutter"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar()!=null)
            getSupportActionBar().setElevation(0);

        if(checkPermission()) {
            viewPager = findViewById(R.id.mypager);
            FragmentStateAdapter pagerAdapter = new FragmentAdapter(this);
            viewPager.setAdapter(pagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(titles[position])).attach();
        }

        SharedPreferences settings=getSharedPreferences(PREF_FILE,MODE_PRIVATE);
        boolean dialogShown=settings.getBoolean("dialogShown",false);
        if(!dialogShown){
            SharedPreferences.Editor editor=settings.edit();
            editor.putBoolean("dialogShown",true);
            editor.apply();

            showHelpDialog();
        }

//        startActivity(new Intent(this,ScreenActivity.class));
    }

    public void showHelpDialog(){
        TextView message=new TextView(this);
        message.setText("To merge multiple videos, tap and hold on any video in the list to select more than one file.");
        message.setTextSize(20);
        message.setPadding(10,10,10,10);
        message.setTypeface(ResourcesCompat.getFont(this,R.font.visby_medium));
        message.setGravity(Gravity.START);

        new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setTitle("Multi-Select")
//                .setMessage("To merge multiple videos, tap and hold on any video in the list to select more than one file.")
                .setCancelable(false)
                .setView(message)
                .show();

    }
    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case  PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                    } else {
                        viewPager = findViewById(R.id.mypager);
                        FragmentStateAdapter pagerAdapter = new FragmentAdapter(this);
                        viewPager.setAdapter(pagerAdapter);

                        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(titles[position])).attach();

                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG","mainActResult");

    }
}