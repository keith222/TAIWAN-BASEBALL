package org.sparkr.taiwan_baseball;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.Rank;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RankFragment extends Fragment {

    private OkHttpClient client;
    private SectionedRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private int completedCount = 0;
    private final List<Section> sectionList = new ArrayList<>(Arrays.asList(new Section[3]));

    public RankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RankFragment.
     */
    public static RankFragment newInstance() {
        RankFragment fragment = new RankFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null && !((MainActivity)getContext()).isFinishing() && !((MainActivity)getActivity()).isShowingProgressDialog()) {
            ((MainActivity)getActivity()).showProgressDialog();
        }

        client = Utils.getUnsafeOkHttpClient().build();
        adapter = new SectionedRecyclerViewAdapter();

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        if(month < 3) {
            year--;
            fetchLastYearRank(year);

        } else {
            fetchCurrentRank();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        recyclerView = view.findViewById(R.id.rankRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;
    }

    public void fetchCurrentRank() {
        client.dispatcher().setMaxRequestsPerHost(3);

        for (int i = 0; i < 3; i++) {
            int finalI = i;
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("SeasonCode", String.valueOf(finalI));
            RequestBody body = builder.build();
            Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "/standings/season").post(body).build();

            Call mCall = client.newCall(request);
            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if(getContext() != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if(getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                                ((MainActivity) getActivity()).hideProgressDialog();
                                Toast.makeText(getContext(), "排行資料發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String resStr = (response.body() != null) ? response.body().string() : "";
                    List<Rank> rank = new ArrayList<>();

                    try {
                        Document doc = Jsoup.parse(resStr);
                        Element seasonNode = doc.select(".RecordTable:first-child").first();
                        Elements teamNodes = seasonNode.select("tr");

                        for (Element teamNode: teamNodes) {
                            if(teamNodes.indexOf(teamNode) == 0){ continue; }

                            List<String> rankElement = new ArrayList<>();
                            Elements nodes = teamNode.select("td");
                            for(Element node: nodes) {
                                if(nodes.indexOf(node) > 4) { break; }
                                if(nodes.indexOf(node) == 1) { continue; }
                                if (nodes.indexOf(node) == 0) {
                                    String team = node.select(".team-w-trophy").text().trim();
                                    rankElement.add(team);
                                    continue;
                                }
                                rankElement.add(node.text());
                            }
                            String[] winLose = rankElement.get(1).split("-");
                            rank.add(new Rank(rankElement.get(0), winLose[0], winLose[1], winLose[2], rankElement.get(2), rankElement.get(3)));
                        }

                        // season => 0: All, 1: 1st half, 2: 2nd half
                        String season = "上半季";
                        int index = 0;
                        switch (finalI) {
                            case 0:
                                season = "全年度";
                                index = 2;
                                break;
                            case 1:
                                season = "上半季";
                                index = 0;
                                break;
                            case 2:
                                season = "下半季";
                                index = 1;
                                break;
                        }

                        if (!rank.isEmpty()) {
                            sectionList.set(index, new RankSection(season, rank));
                        }
                        completedCount ++;
                        onDataFetched();

                    } catch (Exception e) {
                        if (getActivity() != null) {
                            ((MainActivity)getActivity()).hideProgressDialog();
                        }
                        e.printStackTrace();
                        Log.d("error-rank:", e.getMessage());
                    }
                }
            });
        }
    }

    public void onDataFetched() {
        if (completedCount == 3) {
            sectionList.remove(null);
            for (int i = 0; i <= 2; i++) {
                if (i >= sectionList.size()) { continue; }
                adapter.addSection(sectionList.get(i));
            }
            recyclerView.post(() -> {
                adapter.notifyDataSetChanged();
                if(getActivity() != null && ((MainActivity)getActivity()).getSelectedIndex() != 0) {
                    ((MainActivity) getActivity()).hideProgressDialog();
                }
            });
        }
    }

    public void fetchLastYearRank(final int year) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("Year", String.valueOf(year));
        RequestBody body = builder.build();

        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "/standings/history").post(body).build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(getContext() != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                            Toast.makeText(getContext(), "排行資料發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resStr = (response.body() != null) ? response.body().string() : "";
                List<Rank> rank = new ArrayList<>();

                try {
                    Document doc = Jsoup.parse(resStr);
                    Elements seasonNodes = doc.select(".RecordTable");

                    for(Element seasonNode: seasonNodes) {
                        Elements teamNodes = seasonNode.select("tr");

                        for(Element teamNode: teamNodes) {
                            if(teamNodes.indexOf(teamNode) == 0){ continue; }

                            Elements nodes = teamNode.select("td");
                            List<String> rankElement = new ArrayList<>();

                            for(Element node: nodes) {
                                if(nodes.indexOf(node) <= 4) { break; }
                                if(nodes.indexOf(node) == 1) { continue; }

                                if (nodes.indexOf(node) == 0) {
                                    String team = node.select(".team-w-trophy").text().trim();
                                    rankElement.add(team);
                                    continue;
                                }
                                rankElement.add(node.text());
                            }
                            String[] winLose = rankElement.get(1).split("-");
                            rank.add(new Rank(rankElement.get(0), winLose[0], winLose[1], winLose[2], rankElement.get(2), rankElement.get(3)));
                        }
                    }

                    adapter.addSection(new RankSection("上半季", rank.subList(0, 5)));
                    adapter.addSection(new RankSection("下半季", rank.subList(5, 10)));
                    adapter.addSection(new RankSection("全年度", rank.subList(10, 15)));

                    recyclerView.post(() -> {
                        adapter.notifyDataSetChanged();
                        if(getActivity() != null && ((MainActivity)getActivity()).getSelectedIndex() != 0) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                        }
                    });

                } catch (Exception e) {
                    if (getActivity() != null) {
                        ((MainActivity)getActivity()).hideProgressDialog();
                    }

                    Log.d("error:", e.getMessage());
                }
            }
        });
    }

    public static class RankSection extends StatelessSection {
        private final String title;
        private final List<Rank> rankList;

        public RankSection(String title, List<Rank> rankList) {
            super(SectionParameters.builder().itemResourceId(R.layout.rank_list).headerResourceId(R.layout.rank_head).build());
            this.title = title;
            this.rankList = rankList;
        }

        @Override
        public int getContentItemsTotal() {
            return rankList.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            Rank rankData = rankList.get(position);

            itemHolder.teamImageView.setImageResource(getTeamImage(rankData.getTeam()));
            itemHolder.winTextView.setText(rankData.getWin());
            itemHolder.loseTextView.setText(rankData.getLose());
            itemHolder.tieTextView.setText(rankData.getTie());
            itemHolder.percentageTextView.setText(rankData.getPercentage());
            itemHolder.gamebehindTextView.setText(rankData.getGamebehind());
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.seasonTextView.setText(title);
        }

        public int getTeamImage(String team) {
            if (team.contains("味全")) {
                return R.mipmap.t__1;
            } else if (team.contains("中信")) {
                return R.mipmap.t1;
            } else if (team.contains("統一")) {
                return R.mipmap.t2;
            } else if (team.contains("樂天")) {
                return R.mipmap.t3_0;
            } else if (team.contains("富邦")) {
                return R.mipmap.t4;
            }

            return R.mipmap.logo;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView seasonTextView;

        HeaderViewHolder(View view) {
            super(view);

            seasonTextView = view.findViewById(R.id.rankSeasonTextView);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView teamImageView;
        private final TextView winTextView;
        private final TextView loseTextView;
        private final TextView tieTextView;
        private final TextView percentageTextView;
        private final TextView gamebehindTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            teamImageView = itemView.findViewById(R.id.rankTeamImageView);
            winTextView = itemView.findViewById(R.id.winTextView);
            loseTextView = itemView.findViewById(R.id.loseTextView);
            tieTextView = itemView.findViewById(R.id.tieTextView);
            percentageTextView = itemView.findViewById(R.id.rateTextView);
            gamebehindTextView = itemView.findViewById(R.id.gbTextView);
        }
    }
}
