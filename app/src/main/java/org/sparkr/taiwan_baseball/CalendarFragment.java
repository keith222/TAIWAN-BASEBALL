package org.sparkr.taiwan_baseball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.sparkr.taiwan_baseball.Model.Game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private RecyclerView recyclerView;
    private SectionedRecyclerViewAdapter adapter;
    private List tempList;
    private int year = 0;
    private int month = 0;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        adapter = new SectionedRecyclerViewAdapter();

        year = Calendar.getInstance().get(Calendar.YEAR);
        month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        if (month < 2) {
            year--;
            month = 11;
        } else if (month > 11) {
            month = 11;
        }

        fetchGame(Integer.toString(year), Integer.toString(month));
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView) getView().findViewById(R.id.calendarTextView)).setText(year + "年" + month + "月");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.gameRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        ImageButton forwardImageButton = (ImageButton) view.findViewById(R.id.forwardImageButton);
        forwardImageButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                month += 1;
                if (month > 11) {
                    year += 1;
                    month = 2;
                }
                fetchGame(Integer.toString(year), Integer.toString(month));
            }
        });

        ImageButton backImageButton = (ImageButton) view.findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                month -= 1;
                if (month < 2) {
                    year -= 1;
                    month = 11;
                }
                fetchGame(Integer.toString(year), Integer.toString(month));
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_calendar) {
            Intent intent = new Intent(getActivity(), DateSelectionActivity.class);
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            year = data.getIntExtra("year", 0);
            month = data.getIntExtra("month", 0);
            fetchGame(Integer.toString(year), Integer.toString(month));
        }
    }

    private void fetchGame(final String year, final String month) {
        if (getActivity().findViewById(R.id.calendarTextView) != null) {
            ((TextView) getActivity().findViewById(R.id.calendarTextView)).setText(year + "年" + month + "月");
        }

        if (getActivity() != null && !((MainActivity) getContext()).isFinishing() && !((MainActivity) getActivity()).isShowingProgressDialog()) {
            ((MainActivity) getActivity()).showProgressDialog();
        }

        adapter.removeAllSections();
        adapter.notifyDataSetChanged();

        tempList = new ArrayList<>();
        final Map<String, List<Game>> tempMap = new TreeMap<>();

        final DatabaseReference dataReference = FirebaseDatabase.getInstance().getReference().getRef().child(year).child(month);

        dataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() < 1) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null && !((MainActivity) getContext()).isFinishing()) {
                                    ((MainActivity) getActivity()).hideProgressDialog();
                                    Toast.makeText(getContext(), "未有比賽資料。", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    return;
                }


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    List<Game> gameList = new ArrayList<>();
                    for (int i = 0; i < snapshot.getChildrenCount(); i++) {
                        Game game = snapshot.child(Integer.toString(i)).getValue(Game.class);
                        gameList.add(game);
                    }

                    tempMap.put(dataSnapshot.getKey(), gameList);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Integer.parseInt(year), (Integer.parseInt(month) - 1), Integer.parseInt(snapshot.getKey()));
                    ;
                    int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                    adapter.addSection(new GameSection(month + "月" + snapshot.getKey() + "日 " + getChineseWeekDay(weekDay), gameList));

                }

                if (recyclerView != null) {
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) getActivity().findViewById(R.id.calendarTextView)).setText(year + "年" + month + "月");

                            adapter.notifyDataSetChanged();
                            ((MainActivity) getActivity()).hideProgressDialog();

                        }
                    });
                }

                dataReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        dataReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getChildrenCount() < 1) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null && !((MainActivity) getContext()).isFinishing()) {
                                    ((MainActivity) getActivity()).hideProgressDialog();
                                    Toast.makeText(getContext(), "未有比賽資料。", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    return;
                }

                List<Game> gameList = new ArrayList<>();
                for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                    Game game = dataSnapshot.child(Integer.toString(i)).getValue(Game.class);
                    gameList.add(game);
                }

                int index = 0;
                for (int i = 0; i < tempMap.size(); i++) {
                    if (tempMap.keySet().toArray()[i] == dataSnapshot.getKey()) {
                        break;
                    }
                    index++;
                }

                GameSection section = (GameSection) adapter.getSectionForPosition(index);
                section.removeAllItem();
                section.addItem(gameList);

                if (recyclerView != null) {
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            ((MainActivity) getActivity()).hideProgressDialog();
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private String getChineseWeekDay(int weekDay) {
        switch (weekDay) {
            case 1:
                return "週日";
            case 2:
                return "週一";
            case 3:
                return "週二";
            case 4:
                return "週三";
            case 5:
                return "週四";
            case 6:
                return "週五";
            case 7:
                return "週六";
            default:
                return "";
        }
    }

    private class GameSection extends StatelessSection {

        private String title;
        private List<Game> gameList;

        public GameSection(String title, List gameList) {
            super(new SectionParameters.Builder(R.layout.calendar_list).headerResourceId(R.layout.calendar_head).build());
            this.title = title;
            this.gameList = gameList;
        }

        public void addItem(List<Game> gameList) {
            this.gameList = gameList;
        }

        public void removeAllItem() {
            this.gameList.clear();
        }

        @Override
        public int getContentItemsTotal() {
            return gameList.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            Game game = gameList.get(position);
            itemHolder.gameNumberTextView.setText(gameString(game.getGame()));
            itemHolder.guestImageView.setImageResource(teamImageView(game.getGuest()));
            itemHolder.guestScoreTextView.setText((game.getG_score().isEmpty()) ? "--" : game.getG_score());
            itemHolder.homeScoreTextView.setText((game.getH_score().isEmpty()) ? "--" : game.getH_score());
            itemHolder.homeImageView.setImageResource(teamImageView(game.getHome()));
            itemHolder.placeTextView.setText(game.getPlace());

            itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Fragment gameFragment = GameFragment.newInstance(gameList.get(position));
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.add(R.id.fragment_calendar_container, gameFragment, "GameFragment");
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.gameDateTextView.setText(title);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView gameDateTextView;

        HeaderViewHolder(View view) {
            super(view);

            gameDateTextView = (TextView) view.findViewById(R.id.gameDateTextView);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView guestImageView;
        private final TextView gameNumberTextView;
        private final TextView guestScoreTextView;
        private final TextView homeScoreTextView;
        private final ImageView homeImageView;
        private final TextView placeTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            guestImageView = (ImageView) itemView.findViewById(R.id.guestImageView);
            gameNumberTextView = (TextView) itemView.findViewById(R.id.gameNumberTextView);
            guestScoreTextView = (TextView) itemView.findViewById(R.id.guestScoreTextView);
            homeScoreTextView = (TextView) itemView.findViewById(R.id.homeScoreTextView);
            homeImageView = (ImageView) itemView.findViewById(R.id.homeImageView);
            placeTextView = (TextView) itemView.findViewById(R.id.placeTextView);
        }
    }

    private String gameString(int game) {
        if (game == 0) {
            return "All Stars Game";
        } else if (game > 0) {
            return "Game: " + game;
        } else if (game < -10) {
            return "季後挑戰賽: " + (-game % 10);
        } else if (game < 0) {
            return "Taiwan Series: G" + (-game);
        }

        return "";
    }

    private int teamImageView(String team) {
        switch (team) {
            case "-5-2":
                return R.mipmap.t_5_2;
            case "-5-1":
                return R.mipmap.t_5_1;
            case "-5":
                return R.mipmap.t_5;
            case "-4":
                return R.mipmap.t_4;
            case "-3":
                return R.mipmap.t_3;
            case "-2":
                return R.mipmap.t_2;
            case "-1":
                return R.mipmap.t_1;
            case "1":
                return R.mipmap.t1;
            case "2":
                return R.mipmap.t2;
            case "3":
                return R.mipmap.t3;
            case "4":
                return R.mipmap.t4;
            case "4-1":
                return R.mipmap.t4_1;
            case "4-2":
                return R.mipmap.t4_2;
            case "4-3":
                return R.mipmap.t4_3;
            case "A-1":
                return R.mipmap.a_1;
            case "A-2":
                return R.mipmap.a_2;
            default:
                return R.mipmap.t1;
        }
    }


}