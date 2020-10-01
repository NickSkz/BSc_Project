package com.example.biosensordataanalyzer.StaticData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.biosensordataanalyzer.User.ExaminatedUserInfoFragment;
import com.example.biosensordataanalyzer.User.ObjectiveUserInfoFragment;
import com.example.biosensordataanalyzer.User.SubjectiveUserInfoFragment;

public class TrainingPageAdapter  extends FragmentPagerAdapter {
    private int tabsNumber;

    public TrainingPageAdapter(FragmentManager fm, int tabsNr){
        super(fm);
        tabsNumber = tabsNr;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new StepsFragment();
            case 1:
                return new DistanceFragment();
            case 2:
                return new CaloriesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsNumber;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }
}
