package org.sparkr.taiwan_baseball;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.News;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

    private OkHttpClient client;
    private List<News> newsList;
    private NewsAdapter adapter;
    private RecyclerView recyclerView;
    private int page = 1;
    private final int visibleThreshold = 4;
    private Boolean isLoading = false;
    int lastVisibleItem, totalItemCount;

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

        Utils.handleSSLHandshake();
        newsList = new ArrayList<>();
        adapter = new NewsAdapter(newsList);
        client = Utils.getUnsafeOkHttpClient().build();

        if(getActivity() != null && !((getContext() != null) && ((MainActivity)getContext()).isFinishing())) {
            ((MainActivity)getActivity()).showProgressDialog();
        }

        fetchNews(page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        recyclerView = view.findViewById(R.id.newsRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public Boolean isScrolled = false;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolled = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!isScrolled) { return; }

                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    recyclerView.post(() -> {
                        newsList.add(null);
                        adapter.notifyItemInserted(newsList.size() - 1);
                    });

                    page++;
                    fetchNews(page);
                    isLoading = true;
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        client.dispatcher().cancelAll();
        if(newsList.size() > 0 && newsList.get(newsList.size()-1) == null) {
            setLoaded();
            newsList.remove(newsList.size()-1);
            adapter.notifyItemRemoved(newsList.size());
        }

    }

    public void setLoaded() {
        isLoading = false;
    }



    private void fetchNews(final int newPage) {
        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "/xmdoc?page=" + newPage).build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(getContext() != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                            Toast.makeText(getContext(), "新聞資料發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resStr = (response.body() != null) ? response.body().string() : "";
                News news;

                try {
                    Document doc = Jsoup.parse(resStr);

                    final Elements nodes = doc.select(".NewsList > .item");
                    for(Element node: nodes) {
                        Elements newsTitleNode = node.select(".title > a");
                        String newsTitle = newsTitleNode.text().trim();
                        String newsDate = node.select(".date").text().trim();
                        String newsImageUrl = node.select(".img a").attr("style").replaceAll(".*?(h[^)]*)\\)", "$1");
                        String newsUrl = newsTitleNode.attr("href");
                        news = new News(newsTitle, newsDate, newsImageUrl, newsUrl);
                        newsList.add(news);
                    }

                    adapter.setOnClick(position -> {
                        News selectedNews = newsList.get(position);
                        if (getActivity() != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity().getString(R.string.CPBLSourceURL) + selectedNews.getNewsUrl()));
                            startActivity(intent);
                        }
                    });

                    recyclerView.post(() -> {
                        adapter.notifyDataSetChanged();
                        if((newsList.size() - nodes.size() - 1) > 0) {
                            newsList.remove(newsList.size() - nodes.size() - 1);
                            adapter.notifyItemRemoved(newsList.size());
                        }

                        setLoaded();

                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                        }
                    });

                } catch (Exception e) {
                    if (getActivity() != null) {
                        ((MainActivity)getActivity()).hideProgressDialog();
                    }

                    Log.d("error:", e.toString());
                }
            }
        });
    }

    public static class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<News> news;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public NewsAdapter(List<News> news) {
            this.news = news;
        }

        public static class NewsViewHolder extends RecyclerView.ViewHolder {

            private final TextView newsTitleTextView;
            private final TextView newsDateTextView;
            private final ImageView newsImageView;
            private String newsURL;

            public NewsViewHolder(View itemView) {
                super(itemView);

                newsTitleTextView = itemView.findViewById(R.id.newsTitleTextView);
                newsDateTextView = itemView.findViewById(R.id.newsDateTextView);
                newsImageView = itemView.findViewById(R.id.newsImageView);
            }
        }

        public static class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progressBar);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            if(viewType == 0) {
                View view = LayoutInflater.from(context).inflate(R.layout.news_list, parent, false);
                return new NewsViewHolder(view);

            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof NewsViewHolder) {
                News newsData = news.get(position);
                NewsViewHolder newsViewHolder = (NewsViewHolder)holder;
                newsViewHolder.newsTitleTextView.setText(newsData.getTitle());
                newsViewHolder.newsDateTextView.setText(newsData.getDate());
                newsViewHolder.newsURL = newsData.getNewsUrl();

                Glide.with(newsViewHolder.newsImageView.getContext())
                        .load(newsData.getImageUrl())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                Log.e("Error", e.getLocalizedMessage());
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .centerCrop().into(newsViewHolder.newsImageView);

                newsViewHolder.newsImageView.setOnClickListener(v -> onClick.onItemClick(position));

            } else if(holder instanceof LoadingViewHolder){
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder)holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return news.size();
        }

        @Override
        public int getItemViewType(int position) {
            return (news.get(position) == null) ? 1 : 0;
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }

    }

}
