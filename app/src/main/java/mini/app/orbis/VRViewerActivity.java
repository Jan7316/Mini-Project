package mini.app.orbis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

/**
 * @author JS
 * Activity that displays a 3D image with distortion correction applied
 */

public class VRViewerActivity extends AppCompatActivity implements AsyncTaskLoadVRImage.ITaskParent {
    private VRViewerProperties properties;
    //private VRRenderer renderer;
    private VRView view;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_viewer);

        properties = new VRViewerProperties();

        view = new VRView(this, BitmapFactory.decodeFile(getIntent().getStringExtra(GlobalVars.EXTRA_PATH)));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,
                properties.interLensDistM*1000*2, getResources().getDisplayMetrics());
        view.setLayoutParams(layoutParams);

        ((RelativeLayout) findViewById(R.id.container)).addView(view);

        findViewById(R.id.container).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onContainerLongClick(v);
                return true;
            }
        });

        findViewById(R.id.action_Bar_top).bringToFront();
        findViewById(R.id.space).bringToFront();
        findViewById(R.id.action_Bar_bottom).bringToFront();
        findViewById(R.id.container).invalidate();

        path = getIntent().getStringExtra(GlobalVars.EXTRA_PATH);
        new AsyncTaskLoadVRImage(this, path).execute();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        findViewById(R.id.action_Bar_top).setVisibility(View.GONE);
        findViewById(R.id.action_Bar_bottom).setVisibility(View.GONE);
        findViewById(R.id.space).setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    public void onImagesLoaded(Bitmap image) {
        view.setImage(image);
    }

    boolean actionBarsVisible = false;

    public void onContainerLongClick(View view) {
        if(!actionBarsVisible) {
            showActionBars();
        }
    }

    public void onSpaceClick(View view) {
        hideActionBars();
    }

    private void showActionBars() {
        actionBarsVisible = true;
        fadeInView(findViewById(R.id.action_Bar_top));
        fadeInView(findViewById(R.id.space));
        fadeInView(findViewById(R.id.action_Bar_bottom));
    }

    private void hideActionBars() {
        actionBarsVisible = false;
        fadeOutView(findViewById(R.id.action_Bar_top));
        fadeOutView(findViewById(R.id.space));
        fadeOutView(findViewById(R.id.action_Bar_bottom));
    }

    private int ANIMATION_DURATION = 300;

    private void fadeInView(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(ANIMATION_DURATION);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(fadeIn);
    }

    private void fadeOutView(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(ANIMATION_DURATION);
        final View icon = view;
        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            public void onAnimationStart(Animation anim) {}
            public void onAnimationRepeat(Animation anim) {}
            public void onAnimationEnd(Animation anim) {
                icon.setVisibility(View.GONE);
            }
        });
        icon.startAnimation(fadeOut);
    }

    public void goBack(View view) {
        finish();
    }
}