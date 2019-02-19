package org.sparkr.taiwan_baseball;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class DateSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SectionedRecyclerViewAdapter adapter;
    private int year = 0;
    private int month = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("選擇年月");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        adapter = new SectionedRecyclerViewAdapter();
        adapter.addSection(new YearMonthSection("選擇年份", addItemList(0)));
        adapter.addSection(new YearMonthSection("選擇月份", addItemList(1)));

        recyclerView = (RecyclerView) findViewById(R.id.dateSelectionRecyclerView);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 4);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 4;
                    default:
                        return 1;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<String> addItemList(int type) {
        List<String> yearMonthList = new ArrayList<>();
        if (type == 0) {
            // year list
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int y = 1990; y <= currentYear; y++) {
                yearMonthList.add(y+"年");
            }
        } else {
            // month list
            for (int m = 2; m <= 11; m++) {
                yearMonthList.add(m+"月");
            }
        }
        return yearMonthList;
    }

    private void sendDataBack(int year, int month) {
        Intent intent = new Intent();
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class YearMonthSection extends StatelessSection {

        private String title;
        private List<String> yearMonthList;
        private int selectedPosition = -1;
        private int size;

        public YearMonthSection(String title, List yearMonthList) {
            super(new SectionParameters.Builder(R.layout.year_month_list).headerResourceId(R.layout.calendar_head).build());
            this.title = title;
            this.yearMonthList = yearMonthList;
            this.size = yearMonthList.size();
        }

        @Override
        public int getContentItemsTotal() { return yearMonthList.size(); }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            final String yearMonth = yearMonthList.get(position);
            itemHolder.yearMonthTextView.setText(yearMonth);
            if (selectedPosition == position) {
                itemHolder.itemView.setBackgroundColor(getResources().getColor(R.color.lighterCPBLBlue));
            } else {
                itemHolder.itemView.setBackgroundColor(0);
            }

            itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == position) { return; }
                    selectedPosition = position;

                    if (size == 10) {
                        month = 2 + position;
                    } else {
                        year = 1990 + position;
                    }
                    adapter.notifyDataSetChanged();
                    if (year != 0 && month != 0 ) {sendDataBack(year, month);}
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

        private final TextView yearMonthTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            yearMonthTextView = (TextView) itemView.findViewById(R.id.yearMonthTextView);
        }
    }



}
