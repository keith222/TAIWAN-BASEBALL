package org.sparkr.taiwan_baseball;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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
    private NewsAdapter adapter;
    private RecyclerView recyclerView;
    private int page = 0;

    public NewsFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewsFragment.
     */
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

        newsList = new ArrayList<>();

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.newsRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);
        fetchNews(page);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Log.d("dy", ""+dy);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void fetchNews(final int newPage) {
        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "news/lists/news_lits.html?per_page=" + newPage).build();
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
                        if (node.select(".news_row_date").text().isEmpty()) {continue;}

                        String newstitle = node.select(".news_row_cont > div > a.news_row_title").text().trim();
                        String tmpeDate = node.select(".news_row_date").text().trim();
                        String newsDate = tmpeDate;
                        String newsImageUrl = node.select(".news_row_pic > img").attr("src").toString();
                        String newsUrl = node.select(".news_row_cont > div > a").attr("href").toString();

                        news = new News(newstitle, newsDate, newsImageUrl, newsUrl);
                        newsList.add(news);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();

                            adapter.setOnClick(new NewsAdapter.OnItemClicked(){
                                @Override
                                public void onItemClick(int position) {
                                    News selectedNews = (News) newsList.get(position);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity().getString(R.string.CPBLSourceURL) + selectedNews.getNewsUrl()));
                                    startActivity(intent);
                                }
                            });
                        }
                    });

                    page += 1;

                } catch (Exception e) {
                    Log.d("error:", e.toString());
                }
            }
        });
    }

    public static class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

        private List<News> news;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public NewsAdapter(List<News> news) {
            this.news = news;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView titleTextView;
            private final TextView dateTextView;
            private final ImageView newsImageView;
            private String newsURL;

            public ViewHolder(View itemView) {
                super(itemView);

                titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
                dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
                newsImageView = (ImageView) itemView.findViewById(R.id.newsImageView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.news_list, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Log.d("News", position+"");

            News newsData = news.get(position);
            holder.titleTextView.setText(newsData.getTitle());
            holder.dateTextView.setText(newsData.getDate());
            holder.newsURL = newsData.getNewsUrl();
            Glide.with(holder.newsImageView.getContext()).load(newsData.getImageUrl()).centerCrop().into(holder.newsImageView);

            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClick.onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return news.size();
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }
    }

}
