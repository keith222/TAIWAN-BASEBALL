package org.sparkr.taiwan_baseball.Model;

import java.io.Serializable;

/**
 * Created by Keith on 2018/1/31.
 */

public class Rank implements Serializable {
    private String team;
    private int rank;
    private int display_rank;
    private int win;
    private int lose;
    private int tie;
    private double winning_rate;
    private double game_behind;

    public String getTeam() {
        return this.team;
    }

    public int getRank() { return this.rank; }

    public int getDisplay_rank() { return this.display_rank; }

    public int getWin() { return this.win; }

    public int getLose() {
        return this.lose;
    }

    public int getTie() {
        return this.tie;
    }

    public double getWinning_rate() { return this.winning_rate; }

    public double getGame_behind() { return this.game_behind; }
}
