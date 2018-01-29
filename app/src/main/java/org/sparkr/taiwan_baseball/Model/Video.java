package org.sparkr.taiwan_baseball.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Keith on 2018/1/29.
 */

public class Video {

    @SerializedName("items")
    private List<VideoItem> videoItem;

    public List<VideoItem> getVideoItem() {
        return videoItem;
    }

    public class VideoItem {

        private Id id;
        private Snippet snippet;

        public Id getId() {
            return id;
        }

        public Snippet getSnippet() {
            return snippet;
        }


        public class Id {

            private String videoId;

            public  String getVideoId() {
                return videoId;
            }
        }


        public class Snippet {

            @SerializedName("title")
            private String videoTitle;
            @SerializedName("publishedAt")
            private String videoDate;
            private Thumbnails thumbnails;

            public String getVideoTitle() {
                return  videoTitle;
            }

            public String getVideoDate() {
                return videoDate;
            }

            public Thumbnails getThumbnails() {
                return thumbnails;
            }
        }

        public class Thumbnails {

            private High high;

            public High getHigh() {
                return high;
            }
        }

        public class High {

            @SerializedName("url")
            private String videoImageUrl;

            public String getVideoImageUrl() {
                return videoImageUrl;
            }
        }
    }
}
