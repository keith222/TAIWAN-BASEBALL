package org.sparkr.taiwan_baseball;

import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


public class MainActivity extends AppCompatActivity {

    /**
     * The  androidx.core.view.PagerAdapter that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.core.app.FragmentStatePagerAdapter.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ProgressDialog progressDialog;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private CustomViewPager mViewPager;

    private String tempTitle = "";
    private int selectedIndex = 0;
    private final int[] yearMonth = new int[2];

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

    public int[] getSelectedYearMonth() { return yearMonth; }

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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
//        mViewPager.setOffscreenPageLimit(4);


        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(iconResId[0]);
        getSupportActionBar().setTitle(titleArray[0]);
        for(int i = 1; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(iconResId[i]);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);

                tab.setIcon(selectedIconresId[tab.getPosition()]);
                getSupportActionBar().setTitle(titleArray[tab.getPosition()]);
                selectedIndex = tab.getPosition();

                if(tab.getPosition() == 2) {
                    Fragment gameFragment = getSupportFragmentManager().findFragmentByTag("GameFragment");
                    if(gameFragment != null && gameFragment.isVisible()) {
                        gameFragment.setMenuVisibility(true);
                    }

                } else if(tab.getPosition() == 3) {
                    Fragment statsListFragment = getSupportFragmentManager().findFragmentByTag("StatsListFragment");
                    Fragment playerFragment = getSupportFragmentManager().findFragmentByTag("PlayerFragment");

                    if(statsListFragment != null && statsListFragment.isVisible()) {
                        statsListFragment.setMenuVisibility(true);
                    }

                    if(playerFragment != null && playerFragment.isVisible()) {
                        playerFragment.setMenuVisibility(true);
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
        mViewPager.setPagingEnabled(value);
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



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 1: return RankFragment.newInstance();
                case 2: return CalendarFragment.newInstance();
                case 3: return StatisticsFragment.newInstance();
                case 4: return VideoFragment.newInstance();
                default: return NewsFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }
    }
}
