package org.sparkr.taiwan_baseball.Model;

import android.util.Log;

/**
 * Created by Keith on 2018/2/23.
 */

public class Stats {

    private String name;
    private String team;
    private String stats;
    private String category;
    private String moreUrl;

    public Stats(String team, String name, String stats, String category, String moreUrl) {
        this.name = name;
        this.team = team;
        this.stats = stats;
        this.category = category;
        this.moreUrl = moreUrl;
    }

    public String getName() {
        return this.name;
    }

    public String getTeam() {
        return this.team;
    }

    public String getStats() {
        return this.stats;
    }

    public String getCategory() {
        return this.category;
    }

    public String getMoreUrl() {
        return this.moreUrl;
    }
}
