package org.sparkr.taiwan_baseball;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.Stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private List<Stats> statsList;
    private List<Stats> battingStats;
    private List<Stats> pitchingStats;
    private RecyclerView recyclerView;
    private StatisticsAdapter adapter;
    private String type = "0"; //0=>batting; 1=>pitching
    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatisticsFragment.
     */
    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        battingStats = new ArrayList<>();
        pitchingStats = new ArrayList<>();
        statsList = new ArrayList<>();
        adapter = new StatisticsAdapter(battingStats);
        fetchStats();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.statisticsRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        SegmentedGroup segmentedGroup = (SegmentedGroup) view.findViewById(R.id.statsSegmented);
        segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.batButton:
                        adapter.stats = battingStats;
                        adapter.notifyDataSetChanged();
                        type = "0";
                        break;
                    case R.id.pitchButton:
                        adapter.stats = pitchingStats;
                        adapter.notifyDataSetChanged();
                        type = "1";
                        break;
                }
            }
        });
        return view;
    }

    private void fetchStats() {
        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "stats/toplist.html").build();
        Call mcall = client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity().findViewById(R.id.loadingPanel).getVisibility() == View.VISIBLE) {
                                getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            }
                            Toast.makeText(getContext(), "發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resStr = response.body().string();
                try {
                    Document doc = Jsoup.parse(resStr);
                    Elements statsNodes = doc.select(".statstoplist_box");

                    for(Element statsNode: statsNodes) {

                        String category = getCategory(statsNodes.indexOf(statsNode));

                        List<String> statsElement = new ArrayList<>();
                        Element tag = statsNode.select("table tr").get(1);

                        Elements nodes = tag.select("td");

                        for(Element node: nodes) {
                            if(nodes.indexOf(node) == 0) { continue; }
                            statsElement.add(node.text());
                        }

                        String moreURL = statsNode.select(".more_row").attr("href").toString();
                        statsList.add(new Stats(statsElement.get(0), statsElement.get(1), statsElement.get(2), category, moreURL));
                    }

                    battingStats.add(statsList.get(0));
                    battingStats.add(statsList.get(1));
                    battingStats.add(statsList.get(2));
                    battingStats.add(statsList.get(6));
                    battingStats.add(statsList.get(7));
                    battingStats.add(statsList.get(10));

                    pitchingStats.add(statsList.get(3));
                    pitchingStats.add(statsList.get(4));
                    pitchingStats.add(statsList.get(5));
                    pitchingStats.add(statsList.get(8));
                    pitchingStats.add(statsList.get(9));
                    pitchingStats.add(statsList.get(11));

                    adapter.setOnClick(new StatisticsAdapter.OnItemClicked(){
                        @Override
                        public void onItemClick(int position) {
                            List<String> moreData = new ArrayList<>();
                            moreData.add(((Integer.parseInt(type) == 0)? battingStats:pitchingStats).get(position).getMoreUrl());
                            moreData.add(((Integer.parseInt(type) == 0)? battingStats:pitchingStats).get(position).getCategory());
                            moreData.add(type);
                            ((MainActivity)getActivity()).setMoreData(moreData);

                            Fragment statsListFragment = new StatsListFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_statistics, statsListFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    });

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();

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

    private String getCategory(int index) {
        switch (index) {
            case 0:
                return "AVG";
            case 1:
                return "H";
            case 2:
                return "HR";
            case 3:
                return "ERA";
            case 4:
                return "W";
            case 5:
                return "SV";
            case 6:
                return "RBI";
            case 7:
                return "SB";
            case 8:
                return "SO";
            case 9:
                return "WHIP";
            case 10:
                return "TB";
            case 11:
                return "HLD";
            default:
                return "";
        }
    }

    public static class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.ViewHolder> {

        private List<Stats> stats;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public StatisticsAdapter(List<Stats> stats) {
            this.stats = stats;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView categoryTextView;
            private final TextView statsNameTextView;
            private final TextView statsTeamTextView;
            private final TextView statsTextView;
            private String moreURL;

            public ViewHolder(View itemView) {
                super(itemView);

                categoryTextView = (TextView) itemView.findViewById(R.id.categoryTextView);
                statsNameTextView = (TextView) itemView.findViewById(R.id.statsNameTextView);
                statsTeamTextView = (TextView) itemView.findViewById(R.id.statsTeamTextView);
                statsTextView = (TextView) itemView.findViewById(R.id.statsTextView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.stats_list, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Stats statsData = stats.get(position);
            holder.categoryTextView.setText(statsData.getCategory());
            holder.statsNameTextView.setText(statsData.getName());
            holder.statsTeamTextView.setText(statsData.getTeam());
            holder.statsTextView.setText(statsData.getStats());
            holder.moreURL = statsData.getMoreUrl();
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClick.onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return stats.size();
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }

    }

}
