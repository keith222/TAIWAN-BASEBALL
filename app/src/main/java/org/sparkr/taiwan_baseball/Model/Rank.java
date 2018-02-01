package org.sparkr.taiwan_baseball.Model;

/**
 * Created by Keith on 2018/1/31.
 */

public class Rank {
    private String team;
    private String win;
    private String lose;
    private String tie;
    private String percentage;
    private String gamebehind;

    public Rank(String team, String win, String tie, String lose, String percentage, String gamebehind) {
        this.team = team;
        this.win = win;
        this.lose = lose;
        this.tie = tie;
        this.percentage = percentage;
        this.gamebehind = gamebehind;
    }

    public String getTeam() {
        return this.team;
    }

    public String getWin() {
        return this.win;
    }

    public String getLose() {
        return this.lose;
    }

    public String getTie() {
        return this.tie;
    }

    public String getPercentage() {
        return this.percentage;
    }

    public String getGamebehind() {
        return this.gamebehind;
    }
}
