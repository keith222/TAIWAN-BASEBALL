package org.sparkr.taiwan_baseball;

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
import android.widget.Toast;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.Rank;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    private OkHttpClient client = new OkHttpClient();
    private SectionedRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

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

        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        adapter = new SectionedRecyclerViewAdapter();

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        if(month < 3) {
            year--;
        }
        fetchRank(Integer.toString(year));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rankRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;
    }

    public void fetchRank(final String year) {
        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "standing/year/"+ year +".html").build();
        Call mcall = client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resStr = response.body().string();
                List<Rank> rank = new ArrayList<>();

                try {
                    Document doc = Jsoup.parse(resStr);
                    Elements seasonNodes = doc.select(".std_tb");

                    for(Element seasonNode: seasonNodes) {
                        Elements teamNodes = seasonNode.select("tr");

                        for(Element teamNode: teamNodes) {
                            if(teamNodes.indexOf(teamNode) == 0){ continue; }

                            Elements nodes = teamNode.select("td");
                            List<String> rankElement = new ArrayList<>();

                            for(Element node: nodes) {
                                if(nodes.indexOf(node) >= 6) { break; }
                                if(nodes.indexOf(node) <= 0 || nodes.indexOf(node) == 2) { continue; }

                                rankElement.add(node.text());
                            }
                            String[] winLose = rankElement.get(1).split("-");
                            rank.add(new Rank(rankElement.get(0), winLose[0], winLose[1], winLose[2], rankElement.get(2), rankElement.get(3)));
                        }

                    }

                    if(rank.size() >= 4) {
                        adapter.addSection(new RankSection("上半季", rank.subList(0,4)));
                    }
                    if(rank.size() >= 8) {
                        adapter.addSection(new RankSection("下半季", rank.subList(4,8)));
                    }
                    if(rank.size() >= 12) {
                        adapter.addSection(new RankSection("全年度", rank.subList(8,12)));
                    }

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                } catch (Exception e) {
                    Log.d("error:", e.toString());
                }
            }
        });
    }

    public class RankSection extends StatelessSection {
        private String title;
        private List<Rank> rankList;

        public RankSection(String title, List rankList) {
            super(new SectionParameters.Builder(R.layout.rank_list).headerResourceId(R.layout.rank_head).build());
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
            switch (team) {
                case "中信兄弟": return R.mipmap.t1;
                case "統一7-ELEVEn": return R.mipmap.t2;
                case "Lamigo": return R.mipmap.t3;
                case "富邦": return R.mipmap.t4;
                case "義大": return R.mipmap.t4_1;
                default: return R.mipmap.t1;
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView seasonTextView;

        HeaderViewHolder(View view) {
            super(view);

            seasonTextView = (TextView) view.findViewById(R.id.rankSeasonTextView);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView teamImageView;
        private final TextView winTextView;
        private final TextView loseTextView;
        private final TextView tieTextView;
        private final TextView percentageTextView;
        private final TextView gamebehindTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            teamImageView = (ImageView) itemView.findViewById(R.id.rankTeamImageView);
            winTextView = (TextView) itemView.findViewById(R.id.winTextView);
            loseTextView = (TextView) itemView.findViewById(R.id.loseTextView);
            tieTextView = (TextView) itemView.findViewById(R.id.tieTextView);
            percentageTextView = (TextView) itemView.findViewById(R.id.rateTextView);
            gamebehindTextView = (TextView) itemView.findViewById(R.id.gbTextView);
        }
    }
}
