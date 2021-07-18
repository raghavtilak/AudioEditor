package com.raghav.audioeditor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private androidx.appcompat.widget.Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private static final int PERMISSION_READ = 111;
    private static final String PREF_FILE = "VideoMergerPref";
    public static ViewPager2 viewPager;
    private String[] titles = new String[]{"Merger", "Find Others", "Cutter"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if(getSupportActionBar()!=null)
//            getSupportActionBar().setElevation(0);


        toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(0);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
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

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        Log.d("TAG","onbackpressed");
        Fragment frag=getSupportFragmentManager().findFragmentByTag("musicplayer");
        if(frag!=null && frag.isVisible()) {
            Log.d("TAG", "onbackpressed null");
            getSupportFragmentManager().beginTransaction().remove(frag).commit();
        }else{
            Log.d("TAG","onbackpressed pagger");
            if (viewPager.getCurrentItem() == 0) {
                super.onBackPressed();
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG","mainActResult");

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        removeMusicPlayer();
        switch (item.getItemId()) {
            case R.id.nav_merger:
                viewPager.setCurrentItem(0);
                break;

            case R.id.nav_cutter:
                viewPager.setCurrentItem(2);
                break;

            case R.id.nav_conv_files:
                startActivity(new Intent(this,AppFiles.class));
                break;

            case R.id.nav_share:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AudioEditor");
                    String shareMessage= "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share App"));
                } catch(Exception e) {
                    //e.toString();
                }
//                Toast.makeText(this, "Share App", Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void removeMusicPlayer(){
        Fragment frag=getSupportFragmentManager().findFragmentByTag("musicplayer");
        if(frag!=null && frag.isVisible()) {
            Log.d("TAG", "onbackpressed null");
            getSupportFragmentManager().beginTransaction().remove(frag).commit();
        }
    }
}