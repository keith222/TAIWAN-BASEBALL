package org.sparkr.taiwan_baseball;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private OkHttpClient client;
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

        if(getActivity() != null && !((MainActivity)getContext()).isFinishing() && !((MainActivity)getActivity()).isShowingProgressDialog()) {
            ((MainActivity) getActivity()).showProgressDialog();
        }

        battingStats = new ArrayList<>();
        pitchingStats = new ArrayList<>();
        statsList = new ArrayList<>();
        adapter = new StatisticsAdapter(battingStats);
        client = Utils.getUnsafeOkHttpClient().build();
        fetchStats();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        recyclerView = view.findViewById(R.id.statisticsRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        SegmentedGroup segmentedGroup = view.findViewById(R.id.statsSegmented);
        segmentedGroup.setOnCheckedChangeListener((radioGroup, i) -> {

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
        });
        return view;
    }

    private void fetchStats() {
        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "/stats/toplist").build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(getContext() != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                            Log.e("=====", e.getLocalizedMessage());
                            Toast.makeText(getContext(), "統計資料錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resStr = (response.body() != null) ? response.body().string() : "";
                try {
                    Document doc = Jsoup.parse(resStr);
                    Elements statsNodes = doc.select(".TopFiveList div.item");

                    for(Element statsNode: statsNodes) {
                        String category = getCategory(statsNodes.indexOf(statsNode));

                        Elements topPlayerNode = statsNode.select("ul li:first-child");
                        String[] playerData = topPlayerNode.select(".player").text().trim().replace(")", "").split("\\(");
                        String stats = topPlayerNode.select(".num").text().trim();
                        String moreURL = statsNode.select(".btn_more a").attr("href");
                        statsList.add(new Stats(playerData[1], playerData[0], stats, category, moreURL));
                    }

                    battingStats.add(statsList.get(0));
                    battingStats.add(statsList.get(1));
                    battingStats.add(statsList.get(2));
                    battingStats.add(statsList.get(3));
                    battingStats.add(statsList.get(4));

                    pitchingStats.add(statsList.get(5));
                    pitchingStats.add(statsList.get(6));
                    pitchingStats.add(statsList.get(7));
                    pitchingStats.add(statsList.get(8));
                    pitchingStats.add(statsList.get(9));

                    adapter.setOnClick(position -> {
                        ArrayList<String> moreData = new ArrayList<>();
                        moreData.add(((Integer.parseInt(type) == 0)? battingStats:pitchingStats).get(position).getMoreUrl());
                        moreData.add(((Integer.parseInt(type) == 0)? battingStats:pitchingStats).get(position).getCategory());
                        moreData.add(type);

                        Fragment statsListFragment = new StatsListFragment();
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("moreData", moreData);
                        statsListFragment.setArguments(bundle);
                        transaction.replace(R.id.fragment_statistics_container, statsListFragment, "StatsListFragment");
                        transaction.addToBackStack(null);
                        transaction.commit();
                    });

                    recyclerView.post(() -> {
                        adapter.notifyDataSetChanged();

                        if (getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                            ((MainActivity)getActivity()).hideProgressDialog();
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
                return "RBI";
            case 4:
                return "SB";
            case 5:
                return "ERA";
            case 6:
                return "W";
            case 7:
                return "SV";
            case 8:
                return "HLD";
            case 9:
                return "SO";
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

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView categoryTextView;
            private final TextView statsNameTextView;
            private final TextView statsTeamTextView;
            private final TextView statsTextView;
            private String moreURL;

            public ViewHolder(View itemView) {
                super(itemView);

                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                statsNameTextView = itemView.findViewById(R.id.statsNameTextView);
                statsTeamTextView = itemView.findViewById(R.id.statsTeamTextView);
                statsTextView = itemView.findViewById(R.id.statsTextView);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.stats_list, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Stats statsData = stats.get(position);
            holder.categoryTextView.setText(statsData.getCategory());
            holder.statsNameTextView.setText(statsData.getName());
            holder.statsTeamTextView.setText(statsData.getTeam());
            holder.statsTextView.setText(statsData.getStats());
            holder.moreURL = statsData.getMoreUrl();
            holder.itemView.setOnClickListener(v -> onClick.onItemClick(position));
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
