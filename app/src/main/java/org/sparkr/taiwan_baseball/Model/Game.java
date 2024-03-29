package org.sparkr.taiwan_baseball.Model;

import java.io.Serializable;

/**
 * Created by Keith on 2018/2/3.
 */

public class Game implements Serializable{
    private int game;
    private String date;
    private String guest;
    private String home;
    private String place;
    private String g_score;
    private String h_score;
    private String stream;

    public int getGame() {
        return game;
    }

    public String getDate() {
        return date;
    }

    public String getGuest() {
        return guest;
    }

    public String getHome() {
        return home;
    }

    public String getPlace() {
        return place;
    }

    public String getG_score() {
        return g_score;
    }

    public String getH_score() {
        return h_score;
    }

    public String getStream() {
        return "https://hamivideo.hinet.net/main/606.do";
//        return stream;
    }
}
