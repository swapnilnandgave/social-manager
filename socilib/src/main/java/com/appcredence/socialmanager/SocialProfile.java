package com.appcredence.socialmanager;

import org.json.JSONObject;

/**
 * Created by swapnilnandgave on 14/03/19.
 */

public class SocialProfile {

    private String rawContent;

    private String name;

    private String email;

    private boolean verified = true;

    private String profilePicture;

    public SocialProfile(String rawContent) {
        if (rawContent == null) {
            new RuntimeException("Raw Content should not be null");
        }
        this.rawContent = rawContent;
        try {
            JSONObject jsonObject = new JSONObject(rawContent);

            String key = "name";
            if (jsonObject.has(key)) {
                this.name = jsonObject.getString(key);
            }

            key = "firstName";
            String key1 = "lastName";
            if (jsonObject.has(key) && jsonObject.has(key1)) {
                this.name = jsonObject.getString(key) + " " + jsonObject.getString(key1);
            }

            key = "verified_email";
            if (jsonObject.has(key)) {
                this.verified = jsonObject.getBoolean(key);
            }

            key = "emailAddress";
            if (jsonObject.has(key)) {
                this.email = jsonObject.getString(key);
            }

            key = "email";
            if (jsonObject.has(key)) {
                this.email = jsonObject.getString(key);
            }

            key = "picture";
            if (jsonObject.has(key)) {
                JSONObject pictureJson = jsonObject.optJSONObject(key);
                if (pictureJson != null) {
                    try {
                        profilePicture = pictureJson.getJSONObject("data").getString("url");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    profilePicture = jsonObject.getString(key);
                }
            }

            key = "pictureUrls";
            if (jsonObject.has(key)) {
                try {
                    profilePicture = jsonObject.getJSONObject(key).getJSONArray("values").get(0).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
