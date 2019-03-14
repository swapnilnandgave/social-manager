package com.appcredence.socialmanagerexample;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getHashKey();
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchNext();
            }
        });
    }

    private void dispatchNext() {
        Intent intent = new Intent(this, SocialActivity.class);
        startActivity(intent);
    }

    private void getHashKey() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.appcredence.socialmanagerexample",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(getClass().getSimpleName(), e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.d(getClass().getSimpleName(), e.getMessage(), e);
        }

    }

}
