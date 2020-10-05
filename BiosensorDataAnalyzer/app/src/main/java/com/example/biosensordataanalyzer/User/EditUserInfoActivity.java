package com.example.biosensordataanalyzer.User;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.biosensordataanalyzer.Main.MainActivity;
import com.example.biosensordataanalyzer.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;


public class EditUserInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        final ViewPager viewPager = findViewById(R.id.view_pager);
        UserDataPageAdapter pagerAdapter = new UserDataPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.bringToFront();
    }

    @Override
    public void onStop(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("username", MainActivity.currentUser.name);
        setResult(Activity.RESULT_OK,returnIntent);
        super.onStop();
    }
}