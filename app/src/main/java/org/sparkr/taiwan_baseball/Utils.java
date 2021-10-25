package org.sparkr.taiwan_baseball;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class Utils {

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int teamImageView(String team) {
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
            case "2":
                return R.mipmap.t2;
            case "3-0":
                return R.mipmap.t3_0;
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
            case "AS":
                return R.mipmap.as;
            case "CT":
                return R.mipmap.ct;
            default:
                return R.mipmap.t1; // team: 1
        }
    }

    public static String gameString(int game) {
        if(game == 0 || game == -100) {
            return "All Stars Game";
        } else if(game > 0) {
            return "Game " + game;
        } else if(game < -10) {
            return "Play Off Series G " + (-game%10);
        } else {
            return "Taiwan Series G" + (-game);
        }
    }

    public static String getTeam(String fileName){
        if (fileName.contains("ADD011")) {
            return "統一7-ELEVEn獅";
        } else if (fileName.contains("ACN011")) {
            return "中信兄弟";
        } else if (fileName.contains("AJL011")) {
            return "樂天桃猿";
        } else if (fileName.contains("AEO011")) {
            return "富邦悍將";
        } else if (fileName.contains("AAA011")) {
            return "味全龍";
        }

        return "無";
    }

    public static int getCategoryIndex(String category){
        switch (category) {
            case "AVG":
            case "ERA":
                return 1;
            case "H":
            case "W":
                return 7;
            case "HR":
                return 11;
            case "RBI":
                return 6;
            case "SB":
            case "SO":
                return 21;
            case "SV":
                return 9;
            case "HLD":
                return 10;
            default:
                return 0;
        }
    }

    public static String getCategory(int index) {
        switch (index) {
            case 0:
                return "ERA";
            case 1:
                return "W";
            case 2:
                return "SV";
            case 3:
                return "HLD";
            case 4:
                return "SO";
            case 5:
                return "AVG";
            case 6:
                return "H";
            case 7:
                return "HR";
            case 8:
                return "RBI";
            case 9:
                return "SB";
            default:
                return "";
        }
    }
}
