package org.sparkr.taiwan_baseball;

import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager2 mViewPager;

    private String tempTitle = "";
    private int selectedIndex = 0;

    private final int[] iconResId = {
            R.mipmap.tab_news,
            R.mipmap.tab_rank,
            R.mipmap.tab_calendar,
            R.mipmap.tab_statistics,
            R.mipmap.tab_video
    };

    private final int[] selectedIconresId = {
            R.mipmap.tab_news_fill,
            R.mipmap.tab_rank_fill,
            R.mipmap.tab_calendar_fill,
            R.mipmap.tab_statistics_fill,
            R.mipmap.tab_video_fill
    };

    private final String[] titleArray = {
            "職棒新聞",
            "聯盟排名",
            "職棒賽程",
            "個人成績",
            "職棒影音"
    };

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setTempTitle(String tempTitle) {
        this.tempTitle = tempTitle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(this);

        // Set up the ViewPager with the sections adapter.

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);


        TabLayout tabLayout = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> tab.setIcon(iconResId[position])
        ).attach();

        getSupportActionBar().setTitle(titleArray[0]);

        tabLayout.getTabAt(0).setIcon(selectedIconresId[0]);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);

                tab.setIcon(selectedIconresId[tab.getPosition()]);
                getSupportActionBar().setTitle(titleArray[tab.getPosition()]);
                selectedIndex = tab.getPosition();

                if(tab.getPosition() == 2) {
                    List<Fragment> fragments = getSupportFragmentManager().getFragments();
                    for(Fragment fragment : fragments){
                        if(fragment instanceof GameFragment) {
                            fragment.setMenuVisibility(true);
                        }

                    }

                } else if(tab.getPosition() == 3) {
                    List<Fragment> fragments = getSupportFragmentManager().getFragments();
                    for(Fragment fragment : fragments){
                        if(fragment instanceof StatsListFragment || fragment instanceof  PlayerFragment) {
                            fragment.setMenuVisibility(true);
                        }

                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
               tab.setIcon(iconResId[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        // If tapping notification which has url.
        try {
            String url = getIntent().getStringExtra("url");
            if (url != null) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(webIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    public void showProgressDialog() {
        if(progressDialog != null) {
            progressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void setPagingEnabled(boolean value) {
        if (mViewPager != null) {
            mViewPager.setUserInputEnabled(value);
        }
    }

    public boolean isShowingProgressDialog() {
        return progressDialog != null && progressDialog.isShowing();
    }

    @Override
    public void onBackPressed() {
        progressDialog.dismiss();

        FragmentManager fm = this.getSupportFragmentManager();

        if (fm.getBackStackEntryCount() == 0) {
            this.finish();

        } else {
            if(fm.getBackStackEntryCount() < 2){
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }

            if(!tempTitle.isEmpty()) {
                getSupportActionBar().setTitle(tempTitle);
                tempTitle = "";

            } else {
                getSupportActionBar().setTitle(titleArray[selectedIndex]);
            }

            fm.popBackStack();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public static class SectionsPagerAdapter extends FragmentStateAdapter {

        public SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            switch (position) {
                case 1: return RankFragment.newInstance();
                case 2: return CalendarFragment.newInstance();
                case 3: return StatisticsFragment.newInstance();
                case 4: return VideoFragment.newInstance();
                default: return NewsFragment.newInstance();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}
