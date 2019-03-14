package com.appcredence.socialmanager;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

/**
 * Created by swapnilnandgave on 14/03/19.
 */

public final class Social {

    public enum SocialStep {
        RequestedCode,
        RequestingToken,
        RequestingProfile
    }

    private enum APIMethod {
        GET,
        POST
    }

    public interface ISocialListener {
        void onFailure(String failureReason);

        void onStepCompleted(SocialStep socialStep);

        void onCompleted(SocialProfile socialProfile);

    }

    private static final String REQUEST_CODE = "code";

    private String redirectUrl = null;
    private String clientID = null;
    private String clientSecret = null;
    private SocialProfileSelection socialProfileSelection = null;
    private ISocialListener listener = null;

    // Auto Assign Properties
    private String state = null;
    private String scope = null;
    private WebViewClient webViewClient;
    private SocialStep socialStep;

    // API Connection Properties
    private Handler handler;
    private HandlerThread handlerThread = new HandlerThread("SocialLogin");

    Social(Builder builder) {
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
        this.socialProfileSelection = builder.socialProfileSelection;
        this.redirectUrl = builder.redirectUrl;
        this.clientID = builder.clientID;
        this.clientSecret = builder.clientSecret;
        this.state = "SocialLogin" + new Random().nextInt(1000);
        switch (socialProfileSelection) {
            case Google:
                scope = "https://www.googleapis.com/auth/userinfo.email";
                break;
            case Facebook:
                scope = "email";
                break;
            case LinkedIn:
                scope = "r_basicprofile,r_emailaddress";
                break;
        }
        webViewClient = new WebViewClient() {

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
                        final String authCode = uri.getQueryParameter(REQUEST_CODE);
                        socialStep = SocialStep.RequestedCode;
                        sendStep();
                        getAuthToken(authCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public SocialProfileSelection getSocialProfileSelection() {
        return socialProfileSelection;
    }

    public WebViewClient getWebClient() {
        return webViewClient;
    }

    public String requestUrl() {
        Uri.Builder builder = this.socialProfileSelection.authBuilder();
        switch (socialProfileSelection) {
            case LinkedIn:
            case Google:
            case Facebook:
                builder.appendQueryParameter("response_type", REQUEST_CODE);
                builder.appendQueryParameter("client_id", clientID);
                builder.appendQueryParameter("redirect_uri", redirectUrl);
                builder.appendQueryParameter("state", state);
                builder.appendQueryParameter("scope", scope);
                break;
        }
        return builder.build().toString();
    }

    public void setListener(ISocialListener listener) {
        this.listener = listener;
    }

    private void getAuthToken(final String authCode) {
        Uri.Builder builder = this.socialProfileSelection.tokenBuilder();
        String authUrl = builder.build().toString();
        String queryParams = null;
        switch (socialProfileSelection) {
            case LinkedIn:
            case Google:
            case Facebook:
                builder.appendQueryParameter("grant_type", "authorization_code");
                builder.appendQueryParameter("code", authCode);
                builder.appendQueryParameter("redirect_uri", redirectUrl);
                builder.appendQueryParameter("client_id", clientID);
                builder.appendQueryParameter("client_secret", clientSecret);
                break;
        }
        socialStep = SocialStep.RequestingToken;
        sendStep();
        APIMethod method = APIMethod.POST;
        switch (socialProfileSelection) {
            case LinkedIn:
                method = APIMethod.POST;
                queryParams = builder.build().getQuery();
                break;
            case Facebook:
                method = APIMethod.GET;
                authUrl = builder.build().toString();
                break;
            case Google:
                method = APIMethod.POST;
                queryParams = builder.build().getQuery();
                break;
        }
        handler.post(new APIRunnable(method, authUrl, null, queryParams));
    }

    private void getProfile(final String accessToken) {
        String profileUrl = this.socialProfileSelection.profileUrl();
        socialStep = SocialStep.RequestingProfile;
        sendStep();
        APIMethod method = APIMethod.GET;
        switch (socialProfileSelection) {
            case LinkedIn:
                method = APIMethod.GET;
                break;
            case Facebook:
                method = APIMethod.GET;
                break;
            case Google:
                method = APIMethod.GET;
                break;
        }
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json");
        handler.post(new APIRunnable(method, profileUrl, headers, null));
    }

    private void fetchedProfile(final String content) {
        if (listener != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onCompleted(new SocialProfile(content));
                }
            });
        }
    }

    private void sendError(final String errorMsg) {
        if (listener != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(errorMsg);
                }
            });
        }
    }

    private void sendStep() {
        if (listener != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStepCompleted(socialStep);
                }
            });
        }
    }

    private void parseContent(String response) {
        switch (socialStep) {
            case RequestingProfile:
                fetchedProfile(response);
                break;
            case RequestingToken:
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    final String accessToken = jsonObject.getString("access_token");
                    getProfile(accessToken);
                } catch (Exception e) {
                    sendError("Failed to parse Auth Token - " + e.toString());
                }
                break;
        }
    }

    private class APIRunnable implements Runnable {

        private String authUrl;
        private String params;
        private APIMethod method;
        private LinkedHashMap<String, String> headers;

        public APIRunnable(APIMethod method, String url, LinkedHashMap<String, String> headers, String params) {
            this.method = method;
            this.authUrl = url;
            this.params = params;
            this.headers = headers;
        }

        @Override
        public void run() {
            try {

                URL url = new URL(authUrl.toString());
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod(method.toString());
                if (headers != null && headers.size() > 0) {
                    Set<String> keys = headers.keySet();
                    for (String key : keys) {
                        httpURLConnection.setRequestProperty(key, headers.get(key));
                    }
                }
                if (params != null) {
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    bufferedWriter.write(params);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    httpURLConnection.connect();
                }

                final int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                    br.close();
                    parseContent(stringBuilder.toString());
                } else {
                    sendError("Failed with Code - " + responseCode + " at step " + socialStep.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendError(e.toString());
            }

        }
    }

    public static final class Builder {

        private String redirectUrl;
        private String clientID;
        private String clientSecret;
        private SocialProfileSelection socialProfileSelection;

        public Builder() {

        }

        public Social.Builder setProfile(SocialProfileSelection socialProfileSelection) {
            this.socialProfileSelection = socialProfileSelection;
            return this;
        }

        public Social.Builder setRedirectUrl(String url) {
            this.redirectUrl = url;
            return this;
        }

        public Social.Builder setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public Social.Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Social build() {
            if (socialProfileSelection == null) {
                new RuntimeException("Social Profile is required");
            }
            if (clientID == null) {
                new RuntimeException("Client ID is required");
            }
            if (clientSecret == null) {
                new RuntimeException("Client Secret is required");
            }
            if (redirectUrl == null) {
                new RuntimeException("Redirect URL is required");
            }
            return new Social(this);
        }

    }

}
