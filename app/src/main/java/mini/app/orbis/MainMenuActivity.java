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

import java.util.Date;

import mini.app.orbis.util.IabHelper;
import mini.app.orbis.util.IabResult;
import mini.app.orbis.util.Inventory;
import mini.app.orbis.util.Purchase;

public class MainMenuActivity extends AppCompatActivity implements IabHelper.OnIabPurchaseFinishedListener {

    IabHelper mHelper;

    boolean active; // true if the app is allowed to be used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu); //TODO temp

        View logo = findViewById(R.id.logo);
        final View logo_text = findViewById(R.id.logo_text);
        View menu = findViewById(R.id.menu);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

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
        fadeInMenu.setStartOffset(4000); // TODO

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

        initializeIAB();

        SharedPreferences usageStats = getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
        boolean initialised = usageStats.getBoolean(GlobalVars.KEY_INITIALISED, false);
        if(!initialised) {
            startActivity(new Intent(this, InitialWelcomeActivity.class));
        }
    }

    private void initializeIAB() {
        // FROM https://developer.android.com/training/in-app-billing/preparing-iab-app.html
        String base64EncodedPublicKey = ""; // This should be compiled at runtime from separate strings

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
        long firstUsage = usageStats.getLong(GlobalVars.KEY_FIRST_USAGE, new Date().getTime());
        long today = new Date().getTime();
        long trialPeriod = 1000 * 60 * 60 * 24 * 7;
        if(GlobalVars.isDebug) {
            active = true;
            return;
        }
        if(today > firstUsage + trialPeriod) {
            active = false;
            try {
                mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                        if (result.isFailure()) {
                        }
                        else {
                            active = inventory.hasPurchase(GlobalVars.SKU_PREMIUM);
                        }
                    }
                });
            } catch(IabHelper.IabAsyncInProgressException e) {
                Log.d("Orbis", "IAB query failed");
                e.printStackTrace();
            }
        } else {
            active = true;
            SharedPreferences sharedPref = getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(GlobalVars.KEY_FIRST_USAGE, firstUsage);
            editor.apply();
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
        if (result.isFailure()) {
            Log.d("Orbis", "Error purchasing: " + result);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("An Error Occurred")
                    .setMessage("An error has occurred while processing your purchase. Please try again. If this error persists, please reinstall the app or contact the Orbis team.")
                    .setIcon(R.drawable.ic_error)
                    .setNeutralButton("Dismiss", null).create();
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            dialog.show();
            dialog.getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        } else if (purchase.getSku().equals(GlobalVars.SKU_PREMIUM)) {
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
