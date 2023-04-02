package org.sparkr.taiwan_baseball;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SectionsPagerAdapter extends FragmentStateAdapter {

    public SectionsPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 1:
                return RankFragment.newInstance();
            case 2:
                return CalendarFragment.newInstance();
            case 3:
                return StatisticsFragment.newInstance();
            case 4:
                return VideoFragment.newInstance();
            default:
                return NewsFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
