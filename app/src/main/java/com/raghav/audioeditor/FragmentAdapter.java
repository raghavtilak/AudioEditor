package com.raghav.audioeditor;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(FragmentActivity fa) {
        super(fa);
    }


    @Override
    public Fragment createFragment(int pos) {
        switch (pos) {
            case 0: {
                return new AudioMergerFragment();
            }
            case 1: {
                return new FindOthersFragment();
            }
            case 2: {
                return new AudioCutterFragment();
            }
            default:
                return new AudioMergerFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}