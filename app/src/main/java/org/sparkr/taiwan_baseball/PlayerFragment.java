package org.sparkr.taiwan_baseball;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private String playerURL;

    public PlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayerFragment.
     */
    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("選手資訊");
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        fetchPlayer(view);

        return view;
    }

    private void fetchPlayer(final View view) {
        ((TextView)view.findViewById(R.id.statsTextView)).setText((((MainActivity)getActivity()).getPlayerData()[1] == "0")?"打擊成績":"投球成績");

        final String cssString = "<style>.std_tb{color: #333;font-size: 13px;line-height: 2.2em;}table.std_tb tr{background-color: #f8f8f8;}table.mix_x tr:nth-child(2n+1), table.std_tb tr.change{background-color: #e6e6e6;}table.std_tb th {background-color: #081B2F;color: #fff;font-weight: normal;padding: 0 6px;}table.std_tb td{padding: 0 6px;}table.std_tb th a, table.std_tb th a:link, table.std_tb th a:visited, table.std_tb th a:active {color: #fff;}a, a:link, a:visited, a:active {text-decoration: none;}</style>";

        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + ((MainActivity)getActivity()).getPlayerData()[0].substring(1)).build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
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
                //gameHtmlString = cssString;

                try{
                    Document doc = Jsoup.parse(resStr);

                    String headElement = doc.select(".player_info div img").attr("src").toString();
                    final String headURL = (headElement.endsWith(".jpg") || headElement.endsWith(".png")) ? headElement : (headElement + "/phone/images/playerhead.png");

                    final String gameURL = headURL.replace("head", "game");
                    final String statsHtml = cssString + doc.select(".std_tb").get(0).toString().replace("display:none;", "");;
                    final String fieldHtml = cssString + doc.select(".std_tb").get(1).toString().replace("詳細","");
                    final String teamHtml = cssString + doc.select(".std_tb").get(doc.select(".std_tb").size() - 2).toString().replace("display:none;", "");
                    final String singleHtml = cssString + doc.select(".std_tb").get(doc.select(".std_tb").size() - 1).toString();
                    final String playerInfo = (doc.select(".player_info_name").text().isEmpty()) ? doc.select(".player_info3_name").text() : doc.select(".player_info_name").text().replace(" ", "").replace("球隊:","｜");;

                    String position,batpitch,height,weight;
                    Elements playerInfoOther = doc.select(".player_info_other tr:first-child td");
                    if(playerInfoOther.size() < 1) {
                        playerInfoOther = doc.select(".player_info3_other tr:first-child td");
                        position = playerInfoOther.get(0).text().split(":")[1];
                        batpitch = playerInfoOther.get(1).text().split(":")[1];
                        Elements playerInfoOther2 = doc.select(".player_info3_other tr:nth-child(2) td");
                        height = playerInfoOther2.get(0).text().split(":")[1];
                        height = height.replaceAll("\\(?\\)?", "").toLowerCase();
                        weight = playerInfoOther2.get(1).toString().split(":")[1];
                        weight = weight.replaceAll("\\(?\\)?", "").toLowerCase();

                    } else {
                        position = playerInfoOther.get(0).text().split(":")[1];
                        batpitch = playerInfoOther.get(1).text().split(":")[1];
                        height = playerInfoOther.get(2).text().split(":")[1];
                        height = height.replaceAll("\\(?\\)?", "").toLowerCase();
                        weight = playerInfoOther.get(3).text().split(":")[1];
                        weight = weight.replaceAll("\\(?\\)?", "").toLowerCase();
                    }

                    final String infoString = position + "｜" + batpitch + "｜" + height + "/" + weight;

                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(getContext()).load(headURL).centerCrop().into((ImageView)view.findViewById(R.id.headImageView));
                                Glide.with(getContext()).load(gameURL).into((ImageView)view.findViewById(R.id.gameImageView));

                                ((WebView)view.findViewById(R.id.statsWebView)).loadData(statsHtml, "text/html; charset=utf-8", "UTF-8");
                                ((WebView)view.findViewById(R.id.fieldingWebView)).loadData(fieldHtml, "text/html; charset=utf-8", "UTF-8");
                                ((WebView)view.findViewById(R.id.teamStatsWebView)).loadData(teamHtml, "text/html; charset=utf-8", "UTF-8");
                                ((WebView)view.findViewById(R.id.singleGameWebView)).loadData(singleHtml, "text/html; charset=utf-8", "UTF-8");

                                ((TextView)view.findViewById(R.id.nameTextView)).setText(playerInfo);
                                ((TextView)view.findViewById(R.id.dataTextView)).setText(infoString);

                                getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.d("error:", e.toString());

                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                Toast.makeText(getContext(), "發生錯誤，請稍後再試。", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

}
