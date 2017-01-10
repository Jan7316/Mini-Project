package mini.app.orbis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import mini.app.orbis.util.IabHelper;
import mini.app.orbis.util.IabResult;
import mini.app.orbis.util.Inventory;
import mini.app.orbis.util.Purchase;

public class VerifyPurchaseActivity extends AppCompatActivity implements IabHelper.OnIabPurchaseFinishedListener {

    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_headset_purchase);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.instructions), FontManager.Font.lato);
        FontManager.applyFontToView(this, (Button) findViewById(R.id.unlock), FontManager.Font.lato);
        FontManager.applyFontToView(this, (Button) findViewById(R.id.back), FontManager.Font.lato);

        initializeIAB();
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void back(View view) {
        finish();
    }

    public void unlock(View view) {
        try {
            mHelper.launchPurchaseFlow(this, GlobalVars.SKU_PREMIUM, 10001, this, "PURCHASE VERIFIER - TODO");
        } catch(IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void initializeIAB() {
        // FROM https://developer.android.com/training/in-app-billing/preparing-iab-app.html -- Will be found in the developer's console
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
        try {
            mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    if (result.isFailure()) {
                        Log.d("Orbis", "Query Inventory Finished Error");
                    } else {
                        if(inventory.hasPurchase(GlobalVars.SKU_PREMIUM)) {
                            skipToMainMenu();
                        }
                    }
                }
            });
        } catch(IabHelper.IabAsyncInProgressException e) {
            Log.d("Orbis", "IAB query failed");
            e.printStackTrace();
        }
    }

    /**
     * To be called if the user has already purchased the premium version (e.g. on a new device)
     */
    private void skipToMainMenu() {
        startActivity(new Intent(this, MainMenuActivity.class));
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
            markAppAsInitialised();
            startActivity(new Intent(this, PurchaseSuccessfulActivity.class));
        }
    }

    private void markAppAsInitialised() {
        SharedPreferences sharedPref = getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(GlobalVars.KEY_INITIALISED, true);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Orbis", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        // Pass on the activity result to the helper for handling
        if(!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d("Orbis", "onActivityResult handled by IABUtil.");
        }
    }

}
