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
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sparkr.taiwan_baseball.Model.StatsList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

    private OkHttpClient client;
    private List<StatsList> statslistList;
    private List<String> moreData;
    private RecyclerView recyclerView;
    private StatsListAdapter adapter;
    private int totalPage = 1;
    private int page = 1;
    private final int visibleThreshold = 4;
    private boolean isLoading = false;
    int lastVisibleItem, totalItemCount;

    public StatsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            moreData = getArguments().getStringArrayList("moreData");
        }

        if(getActivity() != null && !((MainActivity)getActivity()).isShowingProgressDialog() && !((MainActivity)getContext()).isFinishing()) {
            ((MainActivity) getActivity()).showProgressDialog();
        }

        if (getActivity() != null) {
            ((MainActivity)getActivity()).setPagingEnabled(false);
        }

        statslistList = new ArrayList<>();
        adapter = new StatsListAdapter(statslistList);
        client = Utils.getUnsafeOkHttpClient().build();
        fetchStatsList(page);
    }

    @Override
    public void onResume() {
        super.onResume();
        setActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statslist, container, false);

        recyclerView = view.findViewById(R.id.statsListRecyclerView);
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
                    page++;
                    if(page <= totalPage) {
                        recyclerView.post(() -> {
                            statslistList.add(null);
                            adapter.notifyItemInserted(statslistList.size() - 1);
                        });

                        fetchStatsList(page);
                    }
                    isLoading = true;
                }
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        client.dispatcher().cancelAll();
        if(!statslistList.isEmpty() && statslistList.get(statslistList.size()-1) == null) {
            statslistList.remove(statslistList.size()-1);
            adapter.notifyItemRemoved(statslistList.size());
            setLoaded();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getActivity() != null) {
            ((MainActivity)getActivity()).setPagingEnabled(true);
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            setActionBar();
        }
    }

    private void setActionBar() {
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(moreData.get(1));
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void fetchStatsList(final int newPage) {
        String route = moreData.get(0);
        final String category = moreData.get(1);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR) - ((month < 3) ? 1 : 0);

        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + route + "&page=" + newPage + "&year=" + year).build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(getContext() != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(getActivity() != null && !((MainActivity)getContext()).isFinishing()) {
                            ((MainActivity) getActivity()).hideProgressDialog();
                            Toast.makeText(getContext(), "統計資料發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resStr = (response.body() != null) ? response.body().string() : "";
                try {
                    Document doc = Jsoup.parse(resStr);

                    final Elements nodes = doc.select(".RecordTable tr");
                    for(Element node: nodes) {
                        if(nodes.indexOf(node) == 0) { continue; }

                        int categoryIndex = Utils.getCategoryIndex(category);

                        String numData = node.select(".rank").text();
                        Elements player = node.select("td .name a");
                        String nameData = player.text();
                        String[] teamDataArray = node.select("td .team_logo a").attr("href").split("=");
                        String teamData = Utils.getTeam(teamDataArray[teamDataArray.length - 1]);
                        String statsData = node.select("td.num").get(categoryIndex - 1).text();
                        String playerURLData = player.attr("href");

                        statslistList.add(new StatsList(numData, nameData, teamData, statsData, playerURLData));
                    }

                    if(!doc.select(".setting").text().split("/")[0].replaceAll("[^\\d]", "").isEmpty()) {
                        totalPage = Integer.parseInt(doc.select(".setting").text().split("/")[0].replaceAll("[^\\d]", ""));
                    }

                    adapter.setOnClick(position -> {
                        ((MainActivity)getActivity()).setTempTitle(category);
                        String playerData = statslistList.get(position).getPlayerUrl();

                        Fragment playerFragment = new PlayerFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("playerData", playerData);
                        playerFragment.setArguments(bundle);
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.add(R.id.fragment_statslist_container, playerFragment, "PlayerFragment");
                        transaction.addToBackStack(null);
                        transaction.commit();
                    });

                    recyclerView.post(() -> {
                        adapter.notifyDataSetChanged();

                        if(((statslistList.size() - nodes.size()) > 0) && ((statslistList.size() - nodes.size() < statslistList.size()))) {
                            statslistList.remove(statslistList.size() - nodes.size());
                            adapter.notifyItemRemoved(statslistList.size());
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

    public static class StatsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<StatsList> statsList;
        private OnItemClicked onClick;

        public interface OnItemClicked {
            void onItemClick(int position);
        }

        public StatsListAdapter(List<StatsList> statsList) {
            this.statsList = statsList;
        }

        public static class StatsListViewHolder extends RecyclerView.ViewHolder {

            private final TextView numTextView;
            private final TextView nameTextView;
            private final TextView teamTextView;
            private final TextView statsTextView;

            public StatsListViewHolder(View itemView) {
                super(itemView);

                numTextView = itemView.findViewById(R.id.numTextView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                teamTextView = itemView.findViewById(R.id.teamTextView);
                statsTextView = itemView.findViewById(R.id.statsTextView);
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
                View view = LayoutInflater.from(context).inflate(R.layout.statslist_list, parent, false);
                return new StatsListViewHolder(view);

            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof StatsListViewHolder) {
                StatsList statslistData = statsList.get(position);
                StatsListViewHolder statsListViewHolder = (StatsListViewHolder)holder;
                statsListViewHolder.numTextView.setText(statslistData.getNum());
                statsListViewHolder.nameTextView.setText(statslistData.getName());
                statsListViewHolder.teamTextView.setText(statslistData.getTeam());
                statsListViewHolder.statsTextView.setText(statslistData.getStats());
                statsListViewHolder.itemView.setOnClickListener(v -> onClick.onItemClick(position));
            } else if(holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder)holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return statsList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return (statsList.get(position) == null) ? 1 : 0;
        }

        public void setOnClick(OnItemClicked onClick) {
            this.onClick = onClick;
        }

    }

}
