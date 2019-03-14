package com.appcredence.socialmanagerexample;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appcredence.socialmanager.Social;
import com.appcredence.socialmanager.SocialProfile;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by swapnilnandgave on 14/03/19.
 */

public class SocialActivity extends AppCompatActivity {

    private WebView webView;

    final String redirectUrl = "https://dev.dhanvarsha.gravithy.com/oauth";
    // LinkedIN
    //final String clientID = "81lhqusf6wav3s";
    //final String clientSecret = "50hOqbZh3CW1dpVz";

    // Google Plus
    //final String clientID = "143867976707-7i6flpdreavqsftb7hpu6v73mffg4mit.apps.googleusercontent.com";
    //final String clientSecret = "iAF-ZxkzyJOMdI69a8JH_wk5";

    // Facebook
    final String clientID = "855877848121409";
    final String clientSecret = "b2e798791c094658dc4741cbc88f0c0b";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);
        setup();
    }

    private void setup() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        webView = findViewById(R.id.webView);
        //linkedIn();

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                getProfile("AQU7GMklNMZGQwlEGxR3HB5yZNeHFHfcR8YjQ1Q8eZdAKwJbMifOd5fz22SfoC4tf8vDOIgudHeYQdqCFiZW5iHjsQPqUmZJdG_qNAB5gok8UOko1oIEUHIES2MWuL9S4anzfw02a308AWvl0T-EZ6AbNZJG7JvCNhlW3hn9SODYDjVpJxtI5vSgoA-CY7jhMLY968HclmdE1Yp2hyh1GLL61yeb6Ja6wf-msesj640nBvV-6izJ6FR2OkYPrPZMOBwjARf34P1MIBSh_RWdgFVObt2SyCn8jVgVXQhMYnvQwvSSs0M9LAvyM28e8NTc0PrDwiieb7aze9U4DZtGSnZTsVy28Q");
//            }
//        });

        Social.Builder builder = new Social.Builder()
                .setProfile(SocialProfile.Facebook)
                .setRedirectUrl(redirectUrl)
                .setClientID(clientID)
                .setClientSecret(clientSecret);
        Social social = builder.build();
        social.setListener(new Social.ISocialListener() {
            @Override
            public void onFailure(String failureReason) {
                System.out.println("Failed - " + failureReason);
            }

            @Override
            public void onStepCompleted(Social.SocialStep socialStep) {
                System.out.println("Current Step - " + socialStep.toString());
            }

            @Override
            public void onCompleted(String content) {
                System.out.println("Completed - " + content);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        // For Google Sign In
        if (social.getSocialProfile() == SocialProfile.Google) {
            webView.getSettings().setUserAgentString("Android");
        }
        webView.setWebViewClient(social.getWebClient());
        webView.loadUrl(social.requestUrl());
    }

    private void linkedIn() {

        final String responseType = "code";
        final String state = "socialLogin1008";
        final String scope = "r_basicprofile,r_emailaddress";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.authority("www.linkedin.com");
        builder.appendPath("uas").appendPath("oauth2").appendPath("authorization");
        builder.appendQueryParameter("response_type", responseType);
        builder.appendQueryParameter("client_id", clientID);
        builder.appendQueryParameter("redirect_uri", redirectUrl);
        builder.appendQueryParameter("state", state);
        builder.appendQueryParameter("scope", scope);
        final Uri mainUrl = builder.build();

//        final String authUrl = "https://www.linkedin.com/uas/oauth2/authorization";
//        StringBuilder stringBuilder = new StringBuilder(authUrl).append("?");
//        stringBuilder.append("response_type=" + responseType).append("&");
//        stringBuilder.append("client_id=" + clientID).append("&");
//        stringBuilder.append("redirect_uri=" + redirectUrl).append("&");
//        stringBuilder.append("state=" + state).append("&");
//        stringBuilder.append("scope=" + scope).append("&");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.loadUrl(mainUrl.toString());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onPageFinished(WebView view, String urlString) {
                super.onPageFinished(view, urlString);
                try {
                    Uri uri = Uri.parse(urlString);
                    if (uri.getHost().equals(Uri.parse(redirectUrl).getHost())) {
                        System.out.println("Redirect Successfully");
                        final String authCode = uri.getQueryParameter(responseType);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                requestProfile(authCode);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void requestProfile(final String authCode) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.authority("www.linkedin.com");
        builder.appendPath("uas").appendPath("oauth2").appendPath("authorization");
        builder.appendQueryParameter("grant_type", "authorization_code");
        builder.appendQueryParameter("code", authCode);
        builder.appendQueryParameter("redirect_uri", redirectUrl);
        builder.appendQueryParameter("client_id", clientID);
        builder.appendQueryParameter("client_secret", "50hOqbZh3CW1dpVz");
        final String authUrl = "https://www.linkedin.com/uas/oauth2/accessToken";
        String response = "";
        try {

            URL url = new URL(authUrl.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(builder.build().getQuery());
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();

            System.out.println(responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                JSONObject jsonObject = new JSONObject(response);
                final String accessToken = jsonObject.getString("access_token");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                getProfile(accessToken);
                            }
                        });
                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void getProfile(final String accessToken) {

        URL url;
        String response = "";
        final String profileUrl = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,maiden-name,email-address,public-profile-url,picture-urls::(original))?format=json";
        HttpURLConnection httpURLConnection = null;
        try {
            url = new URL(profileUrl);
            httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            int responseCode = httpURLConnection.getResponseCode();

            System.out.println(responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                JSONObject jsonObject = new JSONObject(response);
                System.out.println(jsonObject.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                makeFinish();
                break;
            default:
                break;
        }
        return true;
    }

    public void makeFinish() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
