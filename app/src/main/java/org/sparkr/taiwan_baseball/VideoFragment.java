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

    private OkHttpClient client = new OkHttpClient();
    private List videoList;
    private VideoAdapter adapter;
    private RecyclerView recyclerView;
    private String page = "";
    private int visibleThreshold = 4;
    private Boolean isLoading;
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

        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        videoList = new ArrayList<Video.VideoItem>();
        adapter = new VideoAdapter(videoList);
        fetchVideo(page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.videoRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public Boolean isScrolled = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!isScrolled) { return; }

                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            videoList.add(null);
                            adapter.notifyItemInserted(videoList.size() - 1);
                        }
                    });

                    fetchVideo(page);
                    isLoading = true;
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void setLoaded() {
        isLoading = false;
    }

    private void fetchVideo(final String newPage) {
        Request request = new Request.Builder().url(this.getString(R.string.YoutubeAPIURL) + "search?part=snippet&channelId=UCDt9GAqyRzc2e5BNxPrwZrw&maxResults=15&order=date&pageToken="+ newPage +"&key=" + this.getString(R.string.YoutubeAPIKey)).build();
        Call mcall = client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(getContext(), "發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resStr = response.body().string();

                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    JsonObject jsonObject = gson.fromJson(resStr, JsonObject.class);
                    final Video video = gson.fromJson(jsonObject, Video.class);

                    videoList.addAll(video.getVideoItem());


                    page = jsonObject.get("nextPageToken").getAsString();
                    adapter.setOnClick(new VideoAdapter.OnItemClicked(){
                        @Override
                        public void onItemClick(int position) {
                            Video.VideoItem selectedVideo = (Video.VideoItem) videoList.get(position);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity().getString(R.string.YoutubeURL) + selectedVideo.getId().getVideoId()));
                            startActivity(intent);
                        }
                    });

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();

                            if((videoList.size() - video.getVideoItem().size() - 1) > 0) {
                                videoList.remove(videoList.size() - video.getVideoItem().size() - 1);
                                adapter.notifyItemRemoved(videoList.size());
                            }

                            setLoaded();

                            if (getActivity() != null && getActivity().findViewById(R.id.loadingPanel).getVisibility() == View.VISIBLE) {
                                getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            }
                        }
                    });

                } catch (Exception e) {
                    Log.d("error:", e.toString());
                }
            }
        });
    }

    public static class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Video.VideoItem> videos;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public VideoAdapter(List<Video.VideoItem> videos) {
            this.videos = videos;
        }

        public class VideoViewHolder extends RecyclerView.ViewHolder {

            private final TextView videoTitleTextView;
            private final TextView videoDateTextView;
            private final ImageView videoImageView;
            private String videoId;

            public VideoViewHolder(View itemView) {
                super(itemView);

                videoTitleTextView = (TextView) itemView.findViewById(R.id.videoTitleTextView);
                videoDateTextView = (TextView) itemView.findViewById(R.id.videoDateTextView);
                videoImageView = (ImageView) itemView.findViewById(R.id.videoImageView);
            }
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            if(viewType == 0) {
                View view = LayoutInflater.from(context).inflate(R.layout.video_list, parent, false);
                VideoViewHolder videoViewHolder = new VideoViewHolder(view);
                return videoViewHolder;

            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                LoadingViewHolder loadingViewHolder = new LoadingViewHolder(view);
                return loadingViewHolder;

            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof VideoViewHolder) {
                Video.VideoItem videoData = videos.get(position);
                VideoViewHolder videoViewHolder = (VideoViewHolder)holder;
                videoViewHolder.videoTitleTextView.setText(videoData.getSnippet().getVideoTitle());
                videoViewHolder.videoId = videoData.getId().getVideoId();
                videoViewHolder.videoDateTextView.setText(videoData.getSnippet().getVideoDate().substring(0, 10).replace("-", "."));
                Glide.with(videoViewHolder.videoImageView.getContext()).load(videoData.getSnippet().getThumbnails().getHigh().getVideoImageUrl()).centerCrop().into(videoViewHolder.videoImageView);


                videoViewHolder.videoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClick.onItemClick(position);
                    }
                });

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
