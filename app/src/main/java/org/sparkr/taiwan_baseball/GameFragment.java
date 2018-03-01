package org.sparkr.taiwan_baseball;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import info.hoang8f.android.segmented.SegmentedGroup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sparkr.taiwan_baseball.Model.Game;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private String gameHtmlString;
    private String boxHtmlString;
    private Timer timer;
    private View gameView;
    private Game tempGame;

    public GameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameFragment newInstance() {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("賽事資訊");
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_game, container, false);        TextView textView = new TextView(getActivity());

        final Game receivedGame = ((MainActivity)getActivity()).getSendedGame();

        ((TextView) view.findViewById(R.id.gameNumberTextView)).setText(gameString(receivedGame.getGame()));
        ((ImageView) view.findViewById(R.id.guestImageView)).setImageResource(teamImageView(receivedGame.getGuest()));
        ((TextView) view.findViewById(R.id.guestScoreTextView)).setText((receivedGame.getG_score().isEmpty())?"--":receivedGame.getG_score());
        ((TextView) view.findViewById(R.id.homeScoreTextView)).setText((receivedGame.getH_score().isEmpty())?"--":receivedGame.getH_score());
        ((ImageView) view.findViewById(R.id.homeImageView)).setImageResource(teamImageView(receivedGame.getHome()));
        ((TextView) view.findViewById(R.id.placeTextView)).setText(receivedGame.getPlace());

        ((ImageView) view.findViewById(R.id.streamImageView)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!receivedGame.getStream().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(receivedGame.getStream()));
                    startActivity(intent);
                }
            }
        });

        SegmentedGroup segmentedGroup = (SegmentedGroup) view.findViewById(R.id.segmented);
        segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.boxButton:
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((WebView) view.findViewById(R.id.gameWebView)).loadData(boxHtmlString, "text/html; charset=utf-8", "UTF-8");
                                ((WebView) view.findViewById(R.id.gameWebView)).invalidate();
                            }
                        });

                        break;
                    case R.id.playButton:
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((WebView) view.findViewById(R.id.gameWebView)).loadData(gameHtmlString, "text/html; charset=utf-8", "UTF-8");
                                ((WebView) view.findViewById(R.id.gameWebView)).invalidate();
                            }
                        });

                        break;
                }
            }
        });

        gameView = view;
        tempGame = receivedGame;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = simpleDateFormat.parse(receivedGame.getDate());
            if(DateUtils.isToday(date.getTime())) {
                timer = new Timer(true);
                timer.schedule(new GameTimerTask(), 0,120000);

            } else {
                loadHtml(view, receivedGame);
            }

        } catch (Exception e) {
            Log.d("error:", e.toString());
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(timer != null) {
            timer.cancel();
        }
    }

    public class GameTimerTask extends TimerTask {
        public void run() {
            loadHtml(gameView, tempGame);
        }
    }

    private String gameString(int game) {
        if(game == 0 ) {
            return "All Stars Game";
        } else if(game > 0) {
            return "Game: " + game;
        } else if(game < -10) {
            return "季後挑戰賽: " + (-game%10);
        } else if(game < 0) {
            return "Taiwan Series: G" + (-game);
        }

        return "";
    }

    private int teamImageView(String team) {
        switch (team) {
            case "1": return R.mipmap.t1;
            case "2": return R.mipmap.t2;
            case "3": return R.mipmap.t3;
            case "4": return R.mipmap.t4;
            case "4-1": return R.mipmap.t4_1;
            case "A-1": return R.mipmap.a_1;
            case "A-2": return R.mipmap.a_2;
            default: return R.mipmap.t1;
        }
    }

    private void loadHtml(View v, Game gameData) {
        final View view = v;
        int gameId = gameData.getGame();
        String gameDate = gameData.getDate();
        String year = gameDate.split("-")[0];

        String gameType;
        if(gameId > 0) {
            gameType = "01";

        } else if(gameId == 0) {
            gameType = "02";
            gameId = 1;

        } else if(gameId > -10) {
            gameType = "03";
            gameId = -gameId;

        } else {
            gameType = "05";
            gameId = (-gameId) % 10;

        }

        final String cssString = "<style>.std_tb{color: #333;font-size: 13px;line-height: 2.2em;}table.std_tb tr{background-color: #f8f8f8;}table.mix_x tr:nth-child(2n+1), table.std_tb tr.change{background-color: #e6e6e6;}table.std_tb th {background-color: #081B2F;color: #fff;font-weight: normal;padding: 0 6px;}table.std_tb td{padding: 0 6px;}table.std_tb th a, table.std_tb th a:link, table.std_tb th a:visited, table.std_tb th a:active {color: #fff;}a, a:link, a:visited, a:active {text-decoration: none;color: #333}table.std_tb td.sub {padding-left: 1.2em;}.box_note{font-size: 13px;color:#081B2F;padding-left:15px;}</style>";

        Request request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "games/play_by_play.html?&game_type=" + gameType + "&game_id=" + gameId + "&game_date=" + gameDate + "&pbyear=" + year).build();
        Call mCall = client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resStr = response.body().string();
                gameHtmlString = cssString;

                try{
                    Document doc = Jsoup.parse(resStr);
                    String gameTable = doc.select(".std_tb:first-child").toString();
                    if(!gameTable.isEmpty()) {
                        gameHtmlString += gameTable;
                        gameHtmlString.replace("display:none;", "");
                    }

                    final String scoreBoard = doc.select(".score_board").toString();
                    if(!scoreBoard.isEmpty()) {
                        final String boardCss = "<style>table{width:100%;}.score_board{background-color: #081B2F;overflow:hidden;}.gap_l20{margin-left:10px;}.score_board_side,.score_board_main{float:left;}table.score_table th{color:#b2b1b1}table.score_table th, table.score_table td{height:34px;padding:0 3px;}table.score_table tr:nth-child(2) td{border-bottom:1px solid #0d0d0d;}table.score_table td{color:#fff;}table.score_table td span {margin: 0 2px;padding: 1px 3px;width: 20px;}table.score_table tr:nth-child(3) td {border-top: 1px solid #575757;}</style>";

                        if(getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((WebView) view.findViewById(R.id.scoreWebView)).loadData(boardCss + scoreBoard, "text/html; charset=utf-8", "UTF-8");
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    Log.d("GameError", e.toString());
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

        request = new Request.Builder().url(this.getString(R.string.CPBLSourceURL) + "games/box.html?&game_type=" + gameType + "&game_id=" + gameId + "&game_date=" + gameDate + "&pbyear=" + year).build();
        mCall = client.newCall(request);
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
                boxHtmlString = cssString;

                try{
                    Document doc = Jsoup.parse(resStr);
                    //batting box
                    boxHtmlString += "<h3 style='color:#081B2F;margin:20px 0 10px 10px;'>打擊成績</h3>";
                    boxHtmlString += doc.select(".half_block.left > table").get(0).toString();
                    boxHtmlString += doc.select(".half_block.left > p.box_note").get(0).toString();
                    boxHtmlString += "<p></p>";
                    boxHtmlString += doc.select(".half_block.right > table").get(0).toString();
                    boxHtmlString += doc.select(".half_block.right > p.box_note").get(0).toString();

                    //pitching box
                    boxHtmlString += "<h3 style='color:#081B2F;margin:20px 0 10px 10px;'>投手成績</h3>";
                    boxHtmlString += doc.select(".half_block.left > table").get(1).toString();
                    boxHtmlString += "<p></p>";
                    boxHtmlString += doc.select(".half_block.right > table").get(1).toString();
                    boxHtmlString += "<h3 style='color:#081B2F;margin:20px 0 10px 10px;'>賽後簡報成績</h3>";
                    boxHtmlString += doc.select(".half_block.right > p.box_note").get(2).toString();

                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((WebView) view.findViewById(R.id.gameWebView)).loadData(boxHtmlString, "text/html; charset=utf-8", "UTF-8");

                                if (getActivity().findViewById(R.id.loadingPanel).getVisibility() == View.VISIBLE) {
                                    getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.d("GameError2", e.toString());
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
