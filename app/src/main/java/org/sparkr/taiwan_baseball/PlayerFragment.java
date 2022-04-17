package org.sparkr.taiwan_baseball;


import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {

    private String playerData;

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

        if(getArguments() != null) {
            playerData = getArguments().getString("playerData");
        }

        if(getActivity() != null && !((MainActivity)getContext()).isFinishing() && !((MainActivity)getActivity()).isShowingProgressDialog()) {
            ((MainActivity)getActivity()).showProgressDialog();
        }

        if (getActivity() != null) {
            ((MainActivity)getActivity()).setPagingEnabled(false);
        }
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
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        fetchPlayer(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            setActionBar();
        }
    }

    private void setActionBar() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("選手資訊");
        }
    }

    private void fetchPlayer(final View view) {
        final String specialJSCode = "function changeStyle() {\n" +
                "                document.querySelectorAll('.PlayerHeader').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.search').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.record_table_swipe_guide').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.record_table_scroll_ctrl').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.PageTitle').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.BtnTop').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.born').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.debut').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.edu').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.nationality').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.original_name').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.draft').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.adGeek-author').forEach(function(a){a.remove()});\n" +
                "                document.querySelectorAll('.adGeek-popup').forEach(function(a){a.remove()});\n" +
                "                document.getElementById('mm-blocker').remove();\n" +
                "                document.getElementById('Footer').remove();\n" +
                "                document.getElementById('MenuMobile').remove();\n" +
                "                document.getElementById('Header').remove();\n" +
                "                document.getElementById('Breadcrumbs').remove();\n" +
                "                document.getElementById('nav').remove();\n" +
                "                const ad = document.getElementById('adGeek-slot-div-gpt-ad-1633344967434-0');\n" +
                "                if (ad != null) ad.remove();\n" +
                "                const cssTemplateString = `\n" +
                "                *{-webkit-touch-callout:none;-webkit-user-select:none}\n" +
                "                body {background-color: white;}\n" +
                "                .DistTitle h3, .DistTitle .en {color: #081B2F }\n" +
                "                .RecordTable th{background-color: #081B2F}\n" +
                "                .PlayerBrief dd .desc{color: #081B2F}\n" +
                "                .PlayerBrief dd .label{color: #666}\n" +
                "                .PlayerBrief > div {background-color: rgba(255,255,255,0.8)}\n" +
                "                .PlayerBrief {background: #081B2F}\n" +
                "                .PlayerBrief dt { color: #081B2F }\n" +
                "                .PlayerBrief:after{background: none}\n" +
                "                .ContHeader {margin-top: -35px}\n" +
                "                `;\n" +
                "                const styleTag = document.createElement('style');\n" +
                "                styleTag.innerHTML = cssTemplateString;\n" +
                "                document.head.insertAdjacentElement(`beforeend`, styleTag)\n" +
                "            };\n" +
                "            changeStyle();";

        WebView playerWebView = view.findViewById(R.id.playerWebView);
        playerWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.evaluateJavascript(specialJSCode, (result) -> {
                    if (getActivity() != null) {
                        view.setVisibility(View.VISIBLE);
                        ((MainActivity)getActivity()).hideProgressDialog();
                    }
                });
            }

            @Override
            public void onReceivedError (WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).hideProgressDialog();
                }
            }
        });

        playerWebView.getSettings().setJavaScriptEnabled(true);
        playerWebView.setScrollContainer(true);
        playerWebView.bringToFront();
        playerWebView.setScrollbarFadingEnabled(true);
        playerWebView.setVerticalScrollBarEnabled(true);
        playerWebView.setHorizontalScrollBarEnabled(true);
        playerWebView.getSettings().setUseWideViewPort(true);
        playerWebView.setVisibility(View.GONE);
        playerWebView.loadUrl(this.getString(R.string.CPBLSourceURL) + playerData);
    }
}
