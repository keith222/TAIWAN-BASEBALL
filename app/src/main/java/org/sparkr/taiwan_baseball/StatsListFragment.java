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
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.StatsList;

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
 */
public class StatsListFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private List<StatsList> statslistList;
    private StatsListAdapter adapter;
    private int totalPage = 1;
    private int page = 1;
    private int previousTotal = 0;
    private int visibleThreshold = 4;
    private Boolean loading = true;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    public StatsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(((MainActivity)getActivity()).getMoreData().get(1));

        statslistList = new ArrayList<>();
        adapter = new StatsListAdapter(statslistList);
        fetchStatsList(page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statslist, container, false);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.statsListRecyclerView);
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
                    page++;
                    if(page <= totalPage) {
                        fetchStatsList(page);
                        loading = true;
                    }
                }
            }
        });

        return view;
    }

    public void fetchStatsList(final int newPage) {
        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        String route = ((MainActivity)getActivity()).getMoreData().get(0).substring(1);
        final String category = ((MainActivity)getActivity()).getMoreData().get(1);

        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + route + "&per_page=" + Integer.toString(newPage)).build();
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
                    Elements nodes = doc.select(".std_tb tr");

                    for(Element node: nodes) {
                        if(nodes.indexOf(node) == 0) { continue; }

                        int categoryIndex = getCategoryIndex(category);
                        String numData = node.select("td").get(0).text();
                        String nameData = node.select("td").get(1).text().replace("*","").trim();
                        String teamData = getTeam(node.select("td").get(1).select("img").attr("src").toString());
                        String statsData = node.select("td").get(categoryIndex).text();
                        String playerURLData = node.select("td").get(1).select("a").attr("href").toString();

                        statslistList.add(new StatsList(numData, nameData, teamData, statsData, playerURLData));
                    }

                    if(!doc.select("a.page:nth-last-child(2)").text().isEmpty()) {
                        totalPage = Integer.parseInt(doc.select("a.page:nth-last-child(2)").text());
                    }

                    adapter.setOnClick(new StatsListAdapter.OnItemClicked(){
                        @Override
                        public void onItemClick(int position) {
                            ((MainActivity)getActivity()).setTempTitle(category);
                            ((MainActivity)getActivity()).setPlayerData(new String[]{statslistList.get(position).getPlayerUrl(), ((MainActivity)getActivity()).getMoreData().get(2)});
                            Fragment playerFragmenr = new PlayerFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.add(R.id.fragment_statslist, playerFragmenr);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    });

                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();

                                if (getActivity().findViewById(R.id.loadingPanel).getVisibility() == View.VISIBLE) {
                                    getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                }
                            }
                        });
                    }


                } catch (Exception e) {
                    Log.d("error:", e.toString());
                }
            }
        });
    }

    private String getTeam(String fileName){
        if(fileName.contains("B03")){
            return "義大犀牛";
        }else if(fileName.contains("A02")){
            return "Lamigo";
        }else if(fileName.contains("E02")){
            return "中信兄弟";
        }else if(fileName.contains("L01")){
            return "統一獅";
        }else if(fileName.contains("B04")){
            return "富邦";
        }
        return "無";
    }

    private int getCategoryIndex(String category){
        switch (category) {
            case "AVG":
                return 17;
            case "H":
                return 7;
            case "HR":
                return 11;
            case "ERA":
                return 15;
            case "W":
                return 8;
            case "SV":
                return 10;
            case "RBI":
                return 5;
            case "SB":
                return 14;
            case "SO":
                return 23;
            case "WHIP":
                return 14;
            case "TB":
                return 12;
            case "HLD":
                return 12;
            default:
                return 0;
        }
    }

    public static class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.ViewHolder> {

        private List<StatsList> statsList;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public StatsListAdapter(List<StatsList> statsList) {
            this.statsList = statsList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView numTextView;
            private final TextView nameTextView;
            private final TextView teamTextView;
            private final TextView statsTextView;
            private String plyaerURL;

            public ViewHolder(View itemView) {
                super(itemView);

                numTextView = (TextView) itemView.findViewById(R.id.numTextView);
                nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
                teamTextView = (TextView) itemView.findViewById(R.id.teamTextView);
                statsTextView = (TextView) itemView.findViewById(R.id.statsTextView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.statslist_list, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            StatsList statslistData = statsList.get(position);

            holder.numTextView.setText(statslistData.getNum());
            holder.nameTextView.setText(statslistData.getName());
            holder.teamTextView.setText(statslistData.getTeam());
            holder.statsTextView.setText(statslistData.getStats());
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onClick.onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return statsList.size();
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }

    }

}
