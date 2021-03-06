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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.sparkr.taiwan_baseball.Model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();
    private List<Video.VideoItem> videoList;
    private VideoAdapter adapter;
    private RecyclerView recyclerView;
    private String page = "";
    private final int visibleThreshold = 4;
    private boolean isLoading;
    int lastVisibleItem, totalItemCount;


    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null && !((MainActivity)getContext()).isFinishing() && !((MainActivity)getActivity()).isShowingProgressDialog()) {
            ((MainActivity) getActivity()).showProgressDialog();
        }

        videoList = new ArrayList<>();
        adapter = new VideoAdapter(videoList);
        fetchVideo(page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        recyclerView = view.findViewById(R.id.videoRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public boolean isScrolled = false;

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
                        videoList.add(null);
                        adapter.notifyItemInserted(videoList.size() - 1);
                    });

                    fetchVideo(page);
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
        if(!videoList.isEmpty() && videoList.get(videoList.size()-1) == null) {
            videoList.remove(videoList.size()-1);
            adapter.notifyItemRemoved(videoList.size());
            setLoaded();
        }
    }

    public void setLoaded() {
        isLoading = false;
    }

    private void fetchVideo(final String newPage) {
        Request request = new Request.Builder().url(this.getString(R.string.YoutubeAPIURL) + "search?part=snippet&channelId=UCDt9GAqyRzc2e5BNxPrwZrw&maxResults=15&order=date&pageToken="+ newPage +"&key=" + this.getString(R.string.YoutubeAPIKey)).build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(getContext() != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                            Toast.makeText(getContext(), "影片資料發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resStr = (response.body() != null) ? response.body().string() : "";

                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    JsonObject jsonObject = gson.fromJson(resStr, JsonObject.class);
                    final Video video = gson.fromJson(jsonObject, Video.class);

                    videoList.addAll(video.getVideoItem());


                    page = jsonObject.get("nextPageToken").getAsString();
                    adapter.setOnClick(position -> {
                        Video.VideoItem selectedVideo = videoList.get(position);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity().getString(R.string.YoutubeURL) + selectedVideo.getId().getVideoId()));
                        startActivity(intent);
                    });

                    recyclerView.post(() -> {
                        adapter.notifyDataSetChanged();

                        if((videoList.size() - video.getVideoItem().size() - 1) > 0) {
                            videoList.remove(videoList.size() - video.getVideoItem().size() - 1);
                            adapter.notifyItemRemoved(videoList.size());
                        }

                        setLoaded();

                        if (getActivity() != null) {
                            ((MainActivity)getActivity()).hideProgressDialog();
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

    public static class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<Video.VideoItem> videos;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public VideoAdapter(List<Video.VideoItem> videos) {
            this.videos = videos;
        }

        public static class VideoViewHolder extends RecyclerView.ViewHolder {

            private final TextView videoTitleTextView;
            private final TextView videoDateTextView;
            private final ImageView videoImageView;
            private String videoId;

            public VideoViewHolder(View itemView) {
                super(itemView);

                videoTitleTextView = itemView.findViewById(R.id.videoTitleTextView);
                videoDateTextView = itemView.findViewById(R.id.videoDateTextView);
                videoImageView = itemView.findViewById(R.id.videoImageView);
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
                View view = LayoutInflater.from(context).inflate(R.layout.video_list, parent, false);
                return new VideoViewHolder(view);

            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(view);

            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof VideoViewHolder) {
                Video.VideoItem videoData = videos.get(position);
                VideoViewHolder videoViewHolder = (VideoViewHolder)holder;
                videoViewHolder.videoTitleTextView.setText(videoData.getSnippet().getVideoTitle());
                videoViewHolder.videoId = videoData.getId().getVideoId();
                videoViewHolder.videoDateTextView.setText(videoData.getSnippet().getVideoDate().substring(0, 10).replace("-", "."));
                Glide.with(videoViewHolder.videoImageView.getContext()).load(videoData.getSnippet().getThumbnails().getHigh().getVideoImageUrl()).centerCrop().error(R.mipmap.logo).centerCrop().into(videoViewHolder.videoImageView);


                videoViewHolder.videoImageView.setOnClickListener(v -> onClick.onItemClick(position));

            } else if(holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder)holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        @Override
        public int getItemViewType(int position) {
            return (videos.get(position) == null) ? 1 : 0;
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }
    }


}
