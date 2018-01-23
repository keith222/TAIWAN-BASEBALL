package org.sparkr.taiwan_baseball.Model;

/**
 * Created by Keith on 2018/1/24.
 */

public class News {
    private String title;
    private String date;
    private String imageUrl;
    private String newsUrl;

    public News(String title, String date, String imageurl, String newsUrl) {
        this.title = title;
        this.date = date;
        this.imageUrl = imageurl;
        this.newsUrl = newsUrl;
    }
}
