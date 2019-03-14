package com.appcredence.socialmanager;

import android.net.Uri;

/**
 * Created by swapnilnandgave on 14/03/19.
 */

public enum SocialProfile {

    LinkedIn,
    Google,
    Facebook;

    public Uri.Builder authBuilder() {
        Uri.Builder builder = null;
        switch (this) {
            case LinkedIn:
                //https://www.linkedin.com/uas/oauth2/authorization
                builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("www.linkedin.com");
                builder.appendPath("uas").appendPath("oauth2").appendPath("authorization");
                break;
            case Facebook:
                //https://www.facebook.com/v3.2/dialog/oauth
                builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("www.facebook.com");
                builder.appendPath("v3.2").appendPath("dialog").appendPath("oauth");
                break;
            case Google:
                //https://accounts.google.com/o/oauth2/v2/auth
                builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("accounts.google.com");
                builder.appendPath("o").appendPath("oauth2").appendPath("v2").appendPath("auth");
                break;
        }
        return builder;
    }

    public Uri.Builder tokenBuilder() {
        Uri.Builder builder = null;
        switch (this) {
            case LinkedIn:
                //https://www.linkedin.com/uas/oauth2/accessToken
                builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("www.linkedin.com");
                builder.appendPath("uas").appendPath("oauth2").appendPath("accessToken");
                break;
            case Facebook:
                //https://graph.facebook.com/v3.2/oauth/access_token
                builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("graph.facebook.com");
                builder.appendPath("v3.2").appendPath("oauth").appendPath("access_token");
                break;
            case Google:
                //https://www.googleapis.com/oauth2/v4/token
                builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("www.googleapis.com");
                builder.appendPath("oauth2").appendPath("v4").appendPath("token");
                break;
        }
        return builder;
    }

    public String profileUrl() {
        String url = null;
        switch (this) {
            case LinkedIn:
                url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,maiden-name,email-address,public-profile-url,picture-urls::(original))?format=json";
                break;
            case Facebook:
                url = "https://graph.facebook.com/me?fields=id,name,email,picture.width(800).height(800)";
                break;
            case Google:
                url = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";
                break;
        }
        return url;
    }

}
