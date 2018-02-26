package org.sparkr.taiwan_baseball.Model;

/**
 * Created by Keith on 2018/2/24.
 */

public class StatsList {
    private String num;
    private String name;
    private String team;
    private String stats;
    private String playerUrl;

    public StatsList(String num, String name, String team, String stats, String playerUrl) {
        this.num = num;
        this.name = name;
        this.team = team;
        this.stats = stats;
        this.playerUrl = playerUrl;
    }

    public String getNum() {
        return this.num;
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

    public String getPlayerUrl() {
        return this.playerUrl;
    }
}
