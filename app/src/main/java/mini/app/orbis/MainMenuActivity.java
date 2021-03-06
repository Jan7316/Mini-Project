package mini.app.orbis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.util.Date;

import mini.app.orbis.util.IabHelper;
import mini.app.orbis.util.IabResult;
import mini.app.orbis.util.Inventory;
import mini.app.orbis.util.Purchase;

public class MainMenuActivity extends AppCompatActivity implements IabHelper.OnIabPurchaseFinishedListener {

    IabHelper mHelper;

    boolean active; // true if the app is allowed to be used
    boolean bought;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        View logo = findViewById(R.id.logo);
        final View logo_text = findViewById(R.id.logo_text);
        View menu = findViewById(R.id.menu);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.trial_info_top), FontManager.Font.lato_bold);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.trial_info_bottom), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.version_label), FontManager.Font.lato);

        ((TextView) findViewById(R.id.version_label)).setText("v" + VersionManager.versionName);

        if(savedInstanceState != null) {
            logo_text.setVisibility(View.GONE);

            initializeIAB();
            return;
        }

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(1000);

        Animation fadeInMenu = new AlphaAnimation(0, 1);
        fadeInMenu.setDuration(1000);
        fadeInMenu.setStartOffset(4000);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(1000);
        fadeOut.setStartOffset(2500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                logo_text.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        Animation moveToRight = new TranslateAnimation(-50, 0, 0, 0);
        moveToRight.setDuration(400);
        moveToRight.setStartOffset(1000);

        AnimationSet logoTextAnimations = new AnimationSet(true);
        logoTextAnimations.setInterpolator(new AccelerateDecelerateInterpolator());
        logoTextAnimations.addAnimation(fadeIn);
        logoTextAnimations.addAnimation(moveToRight);
        logoTextAnimations.addAnimation(fadeOut);

        logo.setAnimation(fadeIn);
        logo_text.setAnimation(logoTextAnimations);
        menu.setAnimation(fadeInMenu);

        findViewById(R.id.version_label).setAnimation(fadeInMenu);

        findViewById(R.id.trial_info_bar).setVisibility(View.INVISIBLE);

        initializeIAB();
    }

    private void initializeIAB() {
        // FROM https://developer.android.com/training/in-app-billing/preparing-iab-app.html
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv6hzHkl9PpEokdr6Qp312on05zLKSwjobv/sRcSsc/WPTHnJq2bi7x/+Xa+B1ClbL0Rc+i3gUZSYsTlqHta17dJMHWW8kgaLNX0dnQ2kKDZ/9GwCgsKz5bY0cgtsSWKIRZTNdOFX2NTseCZTF3ouC8KTmAZXTM3VBZ7pJcNfKrXRJ0igsk1AMPawcPBlJCzou4oHtNKdDWUPUwqBuUwlPIynj4uXT7DQy2wWsZ6m5MA2m/+ltWCF4qyZZFuY6KVaxY9WbAvXyVFb2tZX03BThYkwNdaxrJSc4jU0JNWZMVd2Dn9LNEQmc6a7DJ2N4gfV74T3YMlE6Debw4+vAKAtTwIDAQAB";
        // TODO: Including this in one string (as opposed to several substrings) is bad practice but will have to do for the first release

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d("Orbis", "Problem setting up In-app Billing: " + result);
                } else {
                    checkPurchaseStatus();
                }
            }
        });
    }

    private void checkPurchaseStatus() {
        SharedPreferences usageStats = getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
        final long firstUsage = usageStats.getLong(GlobalVars.KEY_FIRST_USAGE, new Date().getTime());
        final long today = new Date().getTime();
        final long trialPeriod = 1000 * 60 * 60 * 24 * 7;
        if(GlobalVars.isDebug) {
            active = true;
            return;
        }

        try {
            mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    if (result.isSuccess()) {
                        bought = inventory.hasPurchase(GlobalVars.SKU_PREMIUM);
                        if(bought) {
                            active = true;
                            findViewById(R.id.trial_info_bar).setVisibility(View.INVISIBLE);
                        } else {
                            if(today > firstUsage + trialPeriod) {
                                active = false;
                                showTrialInfoBar(0);
                            } else {
                                active = true;
                                long day = 1000 * 60 * 60 * 24;
                                int daysPassed = (int) ((today - firstUsage) / day);
                                showTrialInfoBar(7 - daysPassed);
                            }
                        }
                    }
                }
            });
        } catch(IabHelper.IabAsyncInProgressException e) {
            Log.d("Orbis", "IAB query failed");
            e.printStackTrace();
        }

        SharedPreferences sharedPref = getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(GlobalVars.KEY_FIRST_USAGE, firstUsage);
        editor.apply();

        VersionManager.run(getApplicationContext());
    }

    /**
     * @param status number of trial left, in days. 0 = trial elapsed
     */
    private void showTrialInfoBar(int status) {
        if(status == 0) {
            ((TextView) findViewById(R.id.trial_info_bottom)).setText("Elapsed");
        } else if(status == 1) {
            ((TextView) findViewById(R.id.trial_info_bottom)).setText("1 day remaining");
        } else {
            ((TextView) findViewById(R.id.trial_info_bottom)).setText(status + " days remaining");
        }
        findViewById(R.id.trial_info_bar).setVisibility(View.VISIBLE);
    }

    public void buyUpgrade(View view) {
        try {
            mHelper.launchPurchaseFlow(this, GlobalVars.SKU_PREMIUM, 10001, this, "PURCHASE VERIFIER - TODO");
        } catch(IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void toGuides(View view) {
        Intent intent = new Intent(this, GuideMenuActivity.class);
        startActivity(intent);
    }

    public void toGallery(View view) {
        if(active) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        GlobalVars.REQUEST_CODE_FILE_PERMISSION);
            } else {
                Intent intent = new Intent(this, GalleryActivity.class);
                startActivity(intent);
            }
        } else {
            try {
                mHelper.launchPurchaseFlow(this, GlobalVars.SKU_PREMIUM, 10001, this, "PURCHASE VERIFIER - TODO");
            } catch(IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GlobalVars.REQUEST_CODE_FILE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(this, GalleryActivity.class);
                    startActivity(intent);
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("App Permissions")
                            .setMessage("The app requires these permissions in order to display images in the gallery and save pictures that you took with the 3D camera." +
                                        "Please grant them in order to proceed.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNeutralButton("Dismiss", null).create();
                    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    dialog.show();
                    dialog.getWindow().getDecorView().setSystemUiVisibility(
                            this.getWindow().getDecorView().getSystemUiVisibility());
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                }
            }
        }
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        if (result.isSuccess() && purchase.getSku().equals(GlobalVars.SKU_PREMIUM)) {
            findViewById(R.id.trial_info_bar).setVisibility(View.INVISIBLE);
            startActivity(new Intent(this, PurchaseSuccessfulActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass on the activity result to the helper for handling
        if(!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null)
            try {
                mHelper.dispose();
            } catch(IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        mHelper = null;
    }

}
