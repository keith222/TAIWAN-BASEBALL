package org.sparkr.taiwan_baseball;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.News;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private List newsList;
    private RecyclerView recyclerView;

    public NewsFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        final TextView textView = (TextView) view.findViewById(R.id.textView);

        newsList = new ArrayList<>();

        Request request = new Request.Builder().url("http://www.cpbl.com.tw/news/lists.html").build();
        Call mcall = client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resStr = response.body().string();
                News news;

                try {
                    Document doc = Jsoup.parse(resStr);

                    String topNewsTitle = doc.select(".news_head_title > a").text();
                    String topNewsDate = doc.select(".news_head_date").text();
                    String topNewsUrl = doc.select(".games_news_pic > a").attr("href").toString();
                    String topNewsImageUrl = doc.select(".games_news_pic > a > img").attr("src").toString();

                    if(!topNewsTitle.isEmpty()) {
                        news = new News(topNewsTitle, topNewsDate, topNewsImageUrl, topNewsUrl);
                        newsList.add(news);
                    }

                    Elements nodes = doc.select(".news_row");
                    for(Element node: nodes) {
                        String newstitle = node.select(".news_row_cont > div > a.news_row_title").text().trim();
                        String tmpeDate = node.select(".news_row_date").text().trim();
                        String newsDate = tmpeDate;
                        String newsImageUrl = node.select(".news_row_pic > img").attr("src").toString();
                        String newsUrl = node.select(".news_row_cont > div > a").attr("href").toString();

                        news = new News(newstitle, newsDate, newsImageUrl, newsUrl);
                        newsList.add(news);
                    }
                } catch (Exception e) {
                    Log.d("error:", e.toString());
                }




//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textView.setText(resStr);
//                    }
//                });
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
