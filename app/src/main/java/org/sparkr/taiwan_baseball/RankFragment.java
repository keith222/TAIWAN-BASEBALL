package org.sparkr.taiwan_baseball;

import android.content.Context;
import android.media.Image;
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
import org.w3c.dom.Text;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RankFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private List rankList;
    private RankAdapter adapter;

    public RankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RankFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        rankList = new ArrayList<>();
        adapter = new RankAdapter(rankList);

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

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rankRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;
    }


    public void fetchRank(final String year) {
        Log.d("go","gogogogogo");
        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "standing/year/"+ year +".html").build();
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
                List<Rank> rank = new ArrayList<>();

                try {
                    Document doc = Jsoup.parse(resStr);
                    Elements seasonNodes = doc.select(".std_tb");

                    for(Element seasonNode: seasonNodes) {
                        Elements teamNodes = seasonNode.select("tr");

                        for(Element teamNode: teamNodes) {
                            if(teamNodes.indexOf(teamNode) == 0){ continue; }

                            Elements nodes = teamNode.select("td");
                            List<String> rankElement = new ArrayList<String>();

                            for(Element node: nodes) {
                                if(nodes.indexOf(node) >= 6) { break; }
                                if(nodes.indexOf(node) <= 0 || nodes.indexOf(node) == 2) { continue; }

                                rankElement.add(node.text());
                            }
                            String[] winLose = rankElement.get(1).split("-");
                            rank.add(new Rank(rankElement.get(0), winLose[0], winLose[1], winLose[2], rankElement.get(2), rankElement.get(3)));
                        }

                        rankList.add(rank);
                    }

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

    public static class RankAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List rank;

        public RankAdapter(List rank) {
            this.rank = rank;
        }

        public class HeadViewHolder extends RecyclerView.ViewHolder {

            private final TextView seasonTextView;

            public HeadViewHolder(View itemView) {
                super(itemView);

                seasonTextView = (TextView) itemView.findViewById(R.id.rankSeasonTextView);
            }

        }

        public class RankViewHolder extends RecyclerView.ViewHolder {

            private final ImageView teamImageView;
            private final TextView winTextView;
            private final TextView loseTextView;
            private final TextView tieTextView;
            private final TextView percentageTextView;
            private final TextView gamebehindTextView;

            public RankViewHolder(View itemView) {
                super(itemView);

                teamImageView = (ImageView) itemView.findViewById(R.id.rankTeamImageView);
                winTextView = (TextView) itemView.findViewById(R.id.winTextView);
                loseTextView = (TextView) itemView.findViewById(R.id.loseTextView);
                tieTextView = (TextView) itemView.findViewById(R.id.tieTextView);
                percentageTextView = (TextView) itemView.findViewById(R.id.rateTextView);
                gamebehindTextView = (TextView) itemView.findViewById(R.id.gbTextView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view;

            if(viewType == 1) {
                view = LayoutInflater.from(context).inflate(R.layout.rank_head, parent, false);
                return new HeadViewHolder(view);

            } else {
                view = LayoutInflater.from(context).inflate(R.layout.rank_list, parent, false);
                return new RankViewHolder(view);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0 || position == 5 || position == 10) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if(rank.size() < 1) { return; }

            switch (position) {
                case 0:
                    ((HeadViewHolder)holder).seasonTextView.setText("上半季");
                    break;
                case 5:
                    ((HeadViewHolder)holder).seasonTextView.setText((rank.size() == 8)?"全年度":"下半季");
                    break;
                case 10:
                    ((HeadViewHolder)holder).seasonTextView.setText("全年度");
                    break;
            }

            if(position != 0 && position != 5 && position != 10) {
                int dataPosition = 0;
                Rank rankData = null;

                if(position > 0 && position < 5) {
                    dataPosition = position - 1;
                    rankData = ((List<Rank>)rank.get(0)).get(dataPosition);

                } else if (position > 5 && position < 10) {
                    dataPosition = position - 6;
                    rankData = ((List<Rank>)rank.get(0)).get(dataPosition);
                } else if (position > 10 && position < 15) {
                    dataPosition = position - 11;
                    rankData = ((List<Rank>)rank.get(0)).get(dataPosition);
                }

                ((RankViewHolder)holder).teamImageView.setImageResource(getTeamImage(rankData.getTeam()));
                ((RankViewHolder)holder).winTextView.setText(rankData.getWin());
                ((RankViewHolder)holder).loseTextView.setText(rankData.getLose());
                ((RankViewHolder)holder).tieTextView.setText(rankData.getTie());
                ((RankViewHolder)holder).percentageTextView.setText(rankData.getPercentage());
                ((RankViewHolder)holder).gamebehindTextView.setText(rankData.getGamebehind());
            }
        }

        @Override
        public int getItemCount() {
            switch (rank.size()) {
                case 1: return 5;
                case 2: return 10;
                case 3: return 15;
                default: return 0;
            }
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

}
