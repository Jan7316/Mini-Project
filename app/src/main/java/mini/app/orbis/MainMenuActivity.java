package mini.app.orbis;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
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

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu); //TODO temp

        View logo = findViewById(R.id.logo);
        final View logo_text = findViewById(R.id.logo_text);
        View menu = findViewById(R.id.menu);

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

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void toGallery(View view) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    GlobalVars.REQUEST_CODE_FILE_PERMISSION);
        } else {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GlobalVars.REQUEST_CODE_FILE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(this, GalleryActivity.class);
                    startActivity(intent);
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("App Permissions")
                            .setMessage("The app requires both of these permissions in order to display images in the gallery and save pictures that you took with the 3D camera." +
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

}
