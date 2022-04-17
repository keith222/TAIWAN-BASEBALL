package org.sparkr.taiwan_baseball;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import org.sparkr.taiwan_baseball.Model.Game;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;


import info.hoang8f.android.segmented.SegmentedGroup;

import okhttp3.OkHttpClient;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment {

    private OkHttpClient client;
    private String guestImageString;
    private String homeImageString;
    private View gameView;

    // JS, CSS and HTML Strings
    private final String changeStyleJSCode = ""+
            "document.querySelectorAll('.PageTitle').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('.BtnTop').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('.GameSearch').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('.GameDateSelect').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('#Footer').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('#MenuMobile').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('#Header').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('#Breadcrumbs').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('#nav').forEach(function(a){a.remove()});"+
            "document.querySelectorAll('.adGeek-author').forEach(function(a){a.remove()});" +
            "document.querySelectorAll('.adGeek-popup').forEach(function(a){a.remove()});"+
            "document.getElementById('mm-blocker').remove();";


    private final String boxJSCode = "" +
            "function changeStyle() {\n" +
            "   document.querySelectorAll('.en').forEach(function(element) {element.remove();});\n" +
            "   const cssTemplateString = `\n" +
            "       body { background-color: white; padding: 0 10px; }\n" +
            "       .GameBoxDetail > .tabs li.active a, .RecordTable th {background-color: #081B2F }\n" +
            "       .DistTitle h3, .DistTitle .en {color: #081B2F }\n" +
            "       .GameNote, .editable_content {color: #333 }\n" +
            "       .GameBoxDetail > .tabs li a { background-color: #808080; }\n" +
            "       .GameBoxDetail > .tabs li.active:after {border: none}\n" +
            "       .GameBoxDetail > .tabs li a > span {background: none; color: white}`;\n" +
            "   const styleTag = document.createElement('style');\n" +
            "   styleTag.innerHTML = cssTemplateString;\n" +
            "   document.head.insertAdjacentElement(`beforeend`, styleTag)\n" +
            "   document.querySelectorAll('.player .name a').forEach(function(element){ element.removeAttribute('href');});" +
                changeStyleJSCode +
            "   setTimeout(() => {\n" +
            "       document.querySelectorAll('.record_table_swipe_guide').forEach(function(a){a.remove()});\n"+
            "       if (app.curtGameDetail.GameStatus == 1 || app.curtGameDetail.GameStatus == 5 || app.curtGameDetail.GameStatus == 6) {\n" +
            "           Android.showNoGame();" +
            "           return;\n" +
            "       }\n" +
            "       let score_board = document.getElementsByClassName('linescore_table');\n" +
            "       if(score_board != null) {\n" +
            "           score_board = score_board[0].outerHTML.toString()\n" +
            "           Android.showScoreBoard(score_board); \n" +
            "           document.querySelectorAll('.GameHeader').forEach(function(a){a.remove()});\n" +
            "       } else {\n" +
            "           Android.showScoreBoard(\"\"); \n" +
            "       }\n" +
            "   }, 1000);\n" +
            "};\n" +
            "changeStyle();" +
            "setTimeout(() => {\n" +
            "   document.querySelectorAll('.adGeek-author').forEach(function(a){a.remove()});\n" +
            "   document.querySelectorAll('.adGeek-popup').forEach(function(a){a.remove()});\n" +
            "   document.getElementById('mm-blocker').remove();\n" +
            "   const ad = document.getElementById('adGeek-slot-div-gpt-ad-1633344967434-0');\n" +
            "   if (ad != null) ad.remove();\n" +
            "}, 6000);\n";

    private final String liveJSCode = "" +
            "app.switchTabs(3);\n" +
            "setTimeout(changeStyle, 1000);\n" +
            "function changeStyle() {\n" +
                changeStyleJSCode +
            "   const cssTemplateString = `\n" +
            "       body { background-color: white;}\n" +
            "       .InningPlays .title, .InningPlays .item.action { background: none; }\n" +
            "       .GamePlaysDetail > .tabs li.active a, .InningPlays .title, .InningPlaysGroup .tabs li.active a {background-color: #081B2F}\n" +
            "       .InningPlays .item.action { background-color: #505050; }\n" +
            "       .InningPlays .item.action .desc { color: white; }"+
            "       .InningPlays .play .detail .pitches_count, .col_title h3, .GamePlaysDetail .tab_cont .col_title .en {color: #081B2F}\n" +
            "       .GameNote, .editable_content {color: black}\n" +
            "       .desc, .score, .InningPlaysGroup .tabs li a, .InningPlays .play .detail .call_desc, .InningPlays .item .call_desc a, .InningPlays .item .desc a, .InningPlays .item .desc a:focus, .InningPlays .item .desc a:hover { color: #333 }\n" +
            "       .InningPlays .play .detail .no-pitch .call_desc, .InningPlays .play .detail .event .call_desc { background-color: transparent; }\n" +
            "       .InningPlays .item .desc a::after { height: 0px; }\n" +
            "       .InningPlays .item .desc a { font-weight: bold; }\n" +
            "       .GamePlaysDetail > .tabs li.active:after, .InningPlaysGroup .tabs, .InningPlaysGroup .tabs li.active:after {border: none}\n" +
            "       .GamePlaysDetail > .tabs li a > span {background: none; color: white}\n" +
            "       .InningPlaysGroup .tab_container { margin-right: 0px }\n" +
            "       .InningPlaysGroup .tabs { float: none }\n" +
            "       .InningPlaysGroup .tabs ul { overflow: scroll; white-space: nowrap; }\n" +
            "       .InningPlaysGroup .tabs li { margin-bottom: 4px; display: inline-block; }\n" +
            "       .InningPlaysGroup .tabs li a { border-radius: 0}\n" +
            "       .team_image { width: 25px; height: 25px; margin-top: 6px; }\n" +
            "   `;\n" +
            "   const styleTag = document.createElement('style');\n" +
            "   styleTag.innerHTML = cssTemplateString;\n" +
            "   document.head.insertAdjacentElement(`beforeend`, styleTag)\n" +
            "   document.querySelectorAll('.GameHeader').forEach(function(a){a.remove()});\n" +
            "   document.querySelectorAll('.GamePlaysDetail > .tabs').forEach(function(element){element.remove();});\n" +
            "   document.querySelectorAll('.batter_event').forEach(function(element){element.remove();});\n" +
            "   document.querySelectorAll('.no-pitch-action-remind').forEach(function(element){ element.remove();});\n" +
            "   document.querySelectorAll('.en').forEach(function(element) {element.remove();});\n" +
            "   document.querySelectorAll('.title > a').forEach(function(element){element.remove();});\n" +
            "   document.querySelectorAll('.player a').forEach(function(element){ element.removeAttribute('href');});\n" +
            "   document.querySelectorAll('.InningPlays .item .desc a').forEach(function(element){ element.removeAttribute('href')});\n" +
            "   document.querySelectorAll('.team.away').forEach(function(a){a.innerHTML = '<span><img class=\"team_image\" src=\"data:application/png;base64,%GI\"></span>'});\n" +
            "   document.querySelectorAll('.team.home').forEach(function(a){a.innerHTML = '<span><img class=\"team_image\" src=\"data:application/png;base64,%HI\"></span>'});\n" +
            "   document.querySelectorAll('.InningPlaysGroup .tabs li a').forEach(function(element){\n" +
            "       element.addEventListener('click', e => {\n" +
            "           document.querySelectorAll('.batter_event').forEach(function(element){element.remove();});\n" +
            "           document.querySelectorAll('.no-pitch-action-remind').forEach(function(element){ element.remove();});\n" +
            "           document.querySelectorAll('.title > a').forEach(function(element){element.remove();});\n" +
            "           document.querySelectorAll('.player a').forEach(function(element){ element.removeAttribute('href');});\n" +
            "           document.querySelectorAll('.InningPlays .item .desc a').forEach(function(element){ element.removeAttribute('href')});\n" +
            "           document.querySelectorAll('.team.away').forEach(function(a){a.innerHTML = '<span><img class=\"team_image\" src=\"data:application/png;base64,%GI\"></span>'});\n" +
            "           document.querySelectorAll('.team.home').forEach(function(a){a.innerHTML = '<span><img class=\"team_image\" src=\"data:application/png;base64,%HI\"></span>'});\n" +
            "       });\n" +
            "   });\n" +
            "};\n" +
            "setTimeout(() => {\n" +
            "   document.querySelectorAll('.adGeek-author').forEach(function(a){a.remove()});\n" +
            "   document.querySelectorAll('.adGeek-popup').forEach(function(a){a.remove()});\n" +
            "   document.getElementById('mm-blocker').remove();\n" +
            "   const ad = document.getElementById('adGeek-slot-div-gpt-ad-1633344967434-0');\n" +
            "   if (ad != null) ad.remove();\n" +
            "}, 6000);";

    private final String scoreHtml = "" +
            "<!DOCTYPE html>" +
            "<html>\n" +
            "   <head>\n" +
            "       <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0'>\n" +
            "       <style>\n" +
            "           body {\n" +
            "               margin: 0;\n" +
            "               color: white;\n" +
            "               font-family: '-apple-system','Helvetica';\n" +
            "               background-color: #081B2F;\n" +
            "           }\n" +
            "           table {\n" +
            "               width: 100%;\n" +
            "               table-layout: auto;\n" +
            "               border-collapse: collapse;\n" +
            "               border-spacing: 0;\n" +
            "               font-family: '.SF UI Text';\n" +
            "           }\n" +
            "           .linescore_table {\n" +
            "               padding: 5px 10px;\n" +
            "               width: 500px;\n" +
            "           }\n" +
            "           .linescore_table td {\n" +
            "               height: 40px;\n" +
            "           }\n" +
            "           .linescore_table .linescore th span {\n" +
            "               display: block;\n" +
            "           }\n" +
            "           .linescore_table th {\n" +
            "               height: 30px;\n" +
            "               line-height: 30px;\n" +
            "           }\n" +
            "           .linescore td {\n" +
            "               text-align: center;\n" +
            "           }\n" +
            "           .team_name {\n" +
            "               width: fit-content;\n" +
            "           }\n" +
            "           .linescore.scrollable {\n" +
            "               width: 50%;\n" +
            "               margin-left: 10px;\n" +
            "               overflow-y: auto;\n" +
            "           }\n" +
            "           .linescore.fixed {\n" +
            "               width: 20%\n" +
            "           }\n" +
            "           .linescore, .team_name {\n" +
            "               float: left;\n" +
            "           }\n" +
            "           .linescore_table div:not(.team_name) th, .linescore_table>div:not(.team_name) .away td {\n" +
            "               border-bottom: 1px solid rgba(255, 255, 255, 0.3);\n" +
            "           }\n" +
            "           .card, .inning, .away td, .home td, .linescore th span {\n" +
            "               font-family: '-apple-system','Helvetica'\n" +
            "           }\n" +
            "           .short {\n" +
            "               width: 20px;\n" +
            "               height: 30px;\n" +
            "               background: linear-gradient(to bottom, rgba(255, 255, 255, 1) 0%, rgba(240, 240, 240, 1) 50%, rgba(255, 255, 255, 1) 100%);\n" +
            "               border-radius: 5px;\n" +
            "               display: block;\n" +
            "           }\n" +
            "           .team_name td img {\n" +
            "               width: 20px;\n" +
            "               height: 20px;\n" +
            "               margin: 5px 0;\n" +
            "           }\n" +
            "       </style>\n" +
            "   </head>\n" +
            "   <body>%@</body>\n" +
            "   <script type=\"text/javascript\" charset=\"utf-8\">\n" +
            "       document.querySelectorAll('.short').forEach(function (a) { a.remove(); });\n" +
            "       document.querySelectorAll('.team_name .away td').forEach(function(a){a.innerHTML = '<span class=\"short\"><img src=\"data:application/png;base64,%GI\"></span>'});\n" +
            "       document.querySelectorAll('.team_name .home td').forEach(function(a){a.innerHTML = '<span class=\"short\"><img src=\"data:application/png;base64,%HI\"></span>'})" +
            "   </script>\n" +
            "</html>";

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
    public static GameFragment newInstance(Game game) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putSerializable("gameData", game);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        client = Utils.getUnsafeOkHttpClient().build();
        if(getActivity() != null && !((MainActivity)getContext()).isFinishing() && !((MainActivity)getActivity()).isShowingProgressDialog()) {
            ((MainActivity)getActivity()).showProgressDialog();
        }

        if (getActivity() != null) {
            ((MainActivity)getActivity()).setPagingEnabled(false);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_game, container, false);
        final Game receivedGame = (Game)getArguments().getSerializable("gameData");

        ((TextView) view.findViewById(R.id.gameNumberTextView)).setText(Utils.gameString(receivedGame.getGame()));
        ((ImageView) view.findViewById(R.id.guestImageView)).setImageResource(Utils.teamImageView(receivedGame.getGuest()));
        ((TextView) view.findViewById(R.id.guestScoreTextView)).setText((TextUtils.isEmpty(receivedGame.getG_score()))?"--":receivedGame.getG_score());
        ((TextView) view.findViewById(R.id.homeScoreTextView)).setText((TextUtils.isEmpty(receivedGame.getH_score()))?"--":receivedGame.getH_score());
        ((ImageView) view.findViewById(R.id.homeImageView)).setImageResource(Utils.teamImageView(receivedGame.getHome()));
        ((TextView) view.findViewById(R.id.placeTextView)).setText(receivedGame.getPlace());
        view.findViewById(R.id.boxWebView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.gameWebView).setVisibility(View.GONE);

        view.findViewById(R.id.streamImageView).setOnClickListener(v -> {
            if(!TextUtils.isEmpty(receivedGame.getStream())) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(receivedGame.getStream()));
                startActivity(intent);
            }
        });

        SegmentedGroup segmentedGroup = view.findViewById(R.id.segmented);
        segmentedGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.boxButton:
                    if (getActivity() == null) { break; }

                    getActivity().runOnUiThread(() -> {
                        view.findViewById(R.id.gameWebView).setVisibility(View.GONE);
                        view.findViewById(R.id.boxWebView).setVisibility(View.VISIBLE);
                    });

                    break;
                case R.id.playButton:
                    if (getActivity() == null) { break; }

                    getActivity().runOnUiThread(() -> {
                        view.findViewById(R.id.gameWebView).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.boxWebView).setVisibility(View.GONE);
                    });

                    break;
            }
        });

        gameView = view;
        processTeamImage(receivedGame);
        loadHtml(view, receivedGame);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Log.d("onAttach", "onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();

        setActionBar();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            setActionBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        client.dispatcher().cancelAll();

        Log.d("onPause", "onPause");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem menuItem = menu.findItem(R.id.action_calendar);
        if (menuItem != null) {
            menuItem.setVisible(false);
        }
    }

    private void setActionBar() {
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("賽事資訊");
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void processTeamImage(Game gameData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), Utils.teamImageView(gameData.getGuest()));
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        guestImageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), Utils.teamImageView(gameData.getHome()));
        bitmap2.compress(Bitmap.CompressFormat.PNG, 100, baos2);
        byte[] imageBytes2 = baos2.toByteArray();
        homeImageString = Base64.encodeToString(imageBytes2, Base64.NO_WRAP);
    }

    private void loadHtml(View v, Game gameData) {
        int gameId = gameData.getGame();
        String gameDate = gameData.getDate();
        String year = gameDate.split("-")[0];

        String gameType;
        if(gameId > 0) {
            gameType = "A";

        } else if(gameId == 0 || gameId <= -100) {
            gameType = "B";
            gameId = ((-gameId) % 9) + 1;

        } else if(gameId > -10) {
            gameType = "C";
            gameId = -gameId;

        } else {
            gameType = "E";
            gameId = -gameId;

        }



        WebView boxWebView = v.findViewById(R.id.boxWebView);
        boxWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.evaluateJavascript(boxJSCode, null);
            }

            @Override
            public void onReceivedError (WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).hideProgressDialog();
                }
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        boxWebView.addJavascriptInterface(new WebAppInterface(getContext()), "Android");
        boxWebView.getSettings().setJavaScriptEnabled(true);
        boxWebView.setScrollContainer(true);
        boxWebView.bringToFront();
        boxWebView.setScrollbarFadingEnabled(true);
        boxWebView.setVerticalScrollBarEnabled(true);
        boxWebView.setHorizontalScrollBarEnabled(true);
        boxWebView.getSettings().setUseWideViewPort(true);
        boxWebView.setVisibility(View.GONE);
        boxWebView.loadUrl(this.getString(R.string.CPBLSourceURL) + "/box?year=" + year + "&kindCode=" + gameType + "&gameSno=" + gameId);


        WebView gameWebView = v.findViewById(R.id.gameWebView);
        gameWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                final String liveJSCodeString = liveJSCode.replace("%GI", guestImageString).replace("%HI", homeImageString);
                view.evaluateJavascript(liveJSCodeString, null);
            }

            @Override
            public void onReceivedError (WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (getActivity() != null) {
                    view.setVisibility(View.GONE);
                    ((MainActivity)getActivity()).hideProgressDialog();
                }
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        gameWebView.getSettings().setJavaScriptEnabled(true);
        gameWebView.setScrollContainer(true);
        gameWebView.bringToFront();
        gameWebView.setScrollbarFadingEnabled(true);
        gameWebView.setVerticalScrollBarEnabled(true);
        gameWebView.setHorizontalScrollBarEnabled(true);
        gameWebView.getSettings().setUseWideViewPort(true);
        gameWebView.setVisibility(View.GONE);
        gameWebView.loadUrl(this.getString(R.string.CPBLSourceURL) + "/box/live?year=" + year + "&kindCode=" + gameType + "&gameSno=" + gameId);
    }

    private class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showScoreBoard(String html) {
            final String scoreWebHtml = scoreHtml.replace("%@", html).replace("%GI", guestImageString).replace("%HI", homeImageString);
            WebView scoreWebView = gameView.findViewById(R.id.scoreWebView);
            scoreWebView.setVisibility(View.VISIBLE);
            scoreWebView.setScrollbarFadingEnabled(true);
            scoreWebView.setHorizontalScrollBarEnabled(true);
            scoreWebView.setScrollContainer(true);
            scoreWebView.post(() -> {
                scoreWebView.getSettings().setJavaScriptEnabled(true);
                scoreWebView.getSettings().setUseWideViewPort(true);
                scoreWebView.loadDataWithBaseURL(null, scoreWebHtml, "text/html; charset=utf-8", "UTF-8", null);
                scoreWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (getActivity() != null) {
                            gameView.findViewById(R.id.segmented).setVisibility(View.VISIBLE);
                            gameView.findViewById(R.id.boxWebView).setVisibility(View.VISIBLE);

                            ((MainActivity)getActivity()).hideProgressDialog();
                        }
                    }

                    @Override
                    public void onReceivedError (WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                        Log.e("Error", error.getDescription()+"");
                        if (getActivity() != null) {
                            ((MainActivity)getActivity()).hideProgressDialog();
                        }
                    }
                });
            });
        }

        @JavascriptInterface
        public void showNoGame() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> gameView.findViewById(R.id.segmented).setVisibility(View.GONE));

                ((MainActivity)getActivity()).hideProgressDialog();
            }
        }
    }
}
