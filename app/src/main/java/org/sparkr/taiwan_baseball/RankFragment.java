package org.sparkr.taiwan_baseball;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.sparkr.taiwan_baseball.Model.Rank;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RankFragment extends Fragment {

    private SectionedRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
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

        adapter = new SectionedRecyclerViewAdapter();
        fetchCurrentRank();
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
        final DatabaseReference dataReference = FirebaseDatabase.getInstance().getReference().getRef().child("rank");
        dataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() < 1) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (getActivity() != null && !((MainActivity) requireContext()).isFinishing()) {
                                ((MainActivity) getActivity()).hideProgressDialog();
                                Toast.makeText(getContext(), "排行資料發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    return;
                }


                List<Rank> firstRank = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.child("first").getChildren()) {
                    Rank rank = snapshot.getValue(Rank.class);
                    firstRank.add(rank);
                }
                firstRank.sort(Comparator.comparingInt(Rank::getDisplay_rank));
                sectionList.set(0, new RankSection("上半季", firstRank));

                List<Rank> secondRank = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.child("second").getChildren()) {
                    Rank rank = snapshot.getValue(Rank.class);
                    secondRank.add(rank);
                }
                secondRank.sort(Comparator.comparingInt(Rank::getDisplay_rank));
                sectionList.set(1, new RankSection("下半季", secondRank));

                List<Rank> fullRank = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.child("full").getChildren()) {
                    Rank rank = snapshot.getValue(Rank.class);
                    fullRank.add(rank);
                }
                fullRank.sort(Comparator.comparingInt(Rank::getDisplay_rank));
                sectionList.set(2, new RankSection("全年度", fullRank));
                onDataFetched();

                dataReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void onDataFetched() {
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

    public static class RankSection extends Section {
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
            itemHolder.winTextView.setText(String.valueOf(rankData.getWin()));
            itemHolder.loseTextView.setText(String.valueOf(rankData.getLose()));
            itemHolder.tieTextView.setText(String.valueOf(rankData.getTie()));
            itemHolder.percentageTextView.setText(String.valueOf(rankData.getWinning_rate()));
            itemHolder.gamebehindTextView.setText(String.valueOf(rankData.getGame_behind()));
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
                case "-1":
                    return R.mipmap.t_1;
                case "1":
                    return R.mipmap.t1;
                case "2":
                    return R.mipmap.t2;
                case "3-0":
                    return R.mipmap.t3_0;
                case "4":
                    return R.mipmap.t4;
                case "6":
                    return R.mipmap.t6;
                default:
                    return R.mipmap.logo;
            }
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
