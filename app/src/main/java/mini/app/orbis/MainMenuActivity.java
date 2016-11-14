package mini.app.orbis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void toGallery(View view) { // TODO check file access permission before opening gallery
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

}
