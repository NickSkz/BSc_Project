package com.example.biosensordataanalyzer.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class UserDataPageAdapter extends FragmentPagerAdapter {

    private int tabsNumber;

    public UserDataPageAdapter(FragmentManager fm, int tabsNr){
        super(fm);
        tabsNumber = tabsNr;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ObjectiveUserInfoFragment();
            case 1:
                return new SubjectiveUserInfoFragment();
            case 2:
                return new ExaminatedUserInfoFragment();
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
