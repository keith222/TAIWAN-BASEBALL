package org.sparkr.taiwan_baseball;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;


public class DateSelectionActivity extends AppCompatActivity {

    private SectionedRecyclerViewAdapter adapter;
    private int year = 0;
    private int month = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("選擇年月");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        adapter = new SectionedRecyclerViewAdapter();
        adapter.addSection(new YearMonthSection("選擇年份", addItemList(0)));
        adapter.addSection(new YearMonthSection("選擇月份", addItemList(1)));

        RecyclerView recyclerView = findViewById(R.id.dateSelectionRecyclerView);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 4);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                    return 4;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
            for (int m = 2; m <= 12; m++) {
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

    private class YearMonthSection extends Section {

        private final String title;
        private final List yearMonthList;
        private int selectedPosition = -1;
        private final int size;

        public YearMonthSection(String title, List yearMonthList) {
            super(SectionParameters.builder().itemResourceId(R.layout.year_month_list).headerResourceId(R.layout.calendar_head).build());
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

            final String yearMonth = (String) yearMonthList.get(position);
            itemHolder.yearMonthTextView.setText(yearMonth);
            if (selectedPosition == position) {
                itemHolder.itemView.setBackgroundColor(getResources().getColor(R.color.lighterCPBLBlue));
            } else {
                itemHolder.itemView.setBackgroundColor(0);
            }

            itemHolder.itemView.setOnClickListener(v -> {
                if (selectedPosition == position) { return; }
                selectedPosition = position;

                if (size == 11) {
                    month = 2 + position;
                } else {
                    year = 1990 + position;
                }
                adapter.notifyDataSetChanged();
                if (year != 0 && month != 0 ) {sendDataBack(year, month);}
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

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView gameDateTextView;

        HeaderViewHolder(View view) {
            super(view);

            gameDateTextView = view.findViewById(R.id.gameDateTextView);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView yearMonthTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            yearMonthTextView = itemView.findViewById(R.id.yearMonthTextView);
        }
    }



}
