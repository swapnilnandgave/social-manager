package com.appcredence.socialmanagerexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;

import com.appcredence.socialmanager.Social;
import com.appcredence.socialmanager.SocialProfile;

/**
 * Created by swapnilnandgave on 14/03/19.
 */

public class SocialActivity extends AppCompatActivity {

    private WebView webView;

    final String redirectUrl = "https://dev.dhanvarsha.gravithy.com/oauth";

    // LinkedIN
    final String clientID = "xxxxxxxxxxxxxx";
    final String clientSecret = "xxxxxxxxxxxxxxxx";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        webView = findViewById(R.id.webView);
        setupSocialLogin();
    }

    private void setupSocialLogin() {

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
                finish();
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
