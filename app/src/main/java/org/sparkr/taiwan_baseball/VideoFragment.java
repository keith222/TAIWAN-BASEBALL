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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

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
    private String page = "";
    private int previousTotal = 0;
    private int visibleThreshold = 4;
    private Boolean loading = true;
    int firstVisibleItem, visibleItemCount, totalItemCount;


    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.videoRecyclerView);
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

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if(loading) {
                    if(totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }

                if (!loading && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold))) {
                    fetchVideo(page);
                    Log.d("loading","loading");
                    loading = true;
                }

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void fetchVideo(final String newPage) {
        Request request = new Request.Builder().url(this.getString(R.string.YoutubeAPIURL) + "search?part=snippet&channelId=UCDt9GAqyRzc2e5BNxPrwZrw&maxResults=15&order=date&pageToken="+ newPage +"&key=" + this.getString(R.string.YoutubeAPIKey)).build();
        Call mcall = client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {//这是Activity的方法，会在主线程执行任务
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resStr = response.body().string();

                try {

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    JsonObject jsonObject = gson.fromJson(resStr, JsonObject.class);
                    Video video = gson.fromJson(jsonObject, Video.class);

                    videoList.addAll(video.getVideoItem());


                    page = jsonObject.get("nextPageToken").toString();

                    adapter.setOnClick(new VideoAdapter.OnItemClicked(){
                        @Override
                        public void onItemClick(int position) {
                            Video.VideoItem selectedVideo = (Video.VideoItem) videoList.get(position);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity().getString(R.string.YoutubeURL) + selectedVideo.getId().getVideoId()));
                            startActivity(intent);
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();

                            if(getActivity().findViewById(R.id.loadingPanel).getVisibility() == View.VISIBLE) {
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

    public static class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

        private List<Video.VideoItem> videos;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public VideoAdapter(List<Video.VideoItem> videos) {
            this.videos = videos;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView videoTitleTextView;
            private final TextView videoDateTextView;
            private final ImageView videoImageView;
            private String videoId;

            public ViewHolder(View itemView) {
                super(itemView);

                videoTitleTextView = (TextView) itemView.findViewById(R.id.videoTitleTextView);
                videoDateTextView = (TextView) itemView.findViewById(R.id.videoDateTextView);
                videoImageView = (ImageView) itemView.findViewById(R.id.videoImageView);
            }
        }

        @Override
        public VideoFragment.VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.video_list, parent, false);
            VideoFragment.VideoAdapter.ViewHolder viewHolder = new VideoFragment.VideoAdapter.ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Video.VideoItem videoData = videos.get(position);

            holder.videoTitleTextView.setText(videoData.getSnippet().getVideoTitle());
            holder.videoId = videoData.getId().getVideoId();
            holder.videoDateTextView.setText(videoData.getSnippet().getVideoDate().substring(0, 10).replace("-","."));
            Glide.with(holder.videoImageView.getContext()).load(videoData.getSnippet().getThumbnails().getHigh().getVideoImageUrl()).centerCrop().into(holder.videoImageView);


            holder.videoImageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClick.onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }
    }


}
