package org.sparkr.taiwan_baseball;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.sparkr.taiwan_baseball.Model.Game;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Game sendedGame;

    private int[] iconResId = {
            R.mipmap.tab_news,
            R.mipmap.tab_rank,
            R.mipmap.tab_calendar,
            R.mipmap.tab_statistics,
            R.mipmap.tab_video
    };

    private int[] selectedIconresId = {
            R.mipmap.tab_news_fill,
            R.mipmap.tab_rank_fill,
            R.mipmap.tab_calendar_fill,
            R.mipmap.tab_statistics_fill,
            R.mipmap.tab_video_fill
    };

    public void setSendedGame(Game game) {
        this.sendedGame = game;
    }

    public Game getSendedGame() {
        return this.sendedGame;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        View loadingView = findViewById(R.id.loadingPanel);
        loadingView.setVisibility(View.GONE);
        loadingView.setClickable(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(iconResId[0]);
        for(int i = 1; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(iconResId[i]);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setIcon(selectedIconresId[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
               tab.setIcon(iconResId[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

    }

    
  

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 3: return NewsFragment.newInstance();
                case 2: return RankFragment.newInstance();
                case 0: return CalendarFragment.newInstance();
                case 1: return StatisticsFragment.newInstance();
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
