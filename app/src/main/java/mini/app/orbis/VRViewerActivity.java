package mini.app.orbis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author JS
 * Activity that displays a 3D image with distortion correction applied
 */

public class VRViewerActivity extends AppCompatActivity implements AsyncTaskLoadVRImage.ITaskParent {
    private VRViewerProperties properties;
    private VRView view;
    private int imageID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_viewer);

        properties = new VRViewerProperties();

        imageID = getIntent().getIntExtra(GlobalVars.EXTRA_IMAGE_ID, -1);

        view = new VRView(this, BitmapFactory.decodeResource(getResources(), R.drawable.loading_image_vr_md));
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

        String path = getIntent().getStringExtra(GlobalVars.EXTRA_PATH);
        new AsyncTaskLoadVRImage(this, path).execute();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        findViewById(R.id.action_Bar_top).setVisibility(View.GONE);
        findViewById(R.id.action_Bar_bottom).setVisibility(View.GONE);
        findViewById(R.id.space).setVisibility(View.GONE);

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.title), FontManager.Font.lato);
    }

    @Override
    public void onImagesLoaded(Bitmap image) {
        view.setImage(image);
        isLoading = false;
        ((TextView) findViewById(R.id.title)).setText(FileManager.getFiles(this)[imageID].getName());
    }

    boolean actionBarsVisible = false;

    public void onContainerLongClick(View view) {
        if(isDiashowRunning) {
            stopDiashow();
            showActionBars();
        } else if(!actionBarsVisible) {
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

    public void goLeft(View view) {
        if(imageID <= 0)
            return;
        loadImage(imageID - 1);
    }

    public void goRight(View view) {
        if(imageID >= (FileManager.getFiles(this).length - 1))
            return;
        loadImage(imageID + 1);
    }

    boolean isLoading;
    private void loadImage(int id) {
        if(isLoading)
            return; // Wait for the previous image to finish loading
        isLoading = true;
        imageID = id;
        new AsyncTaskLoadVRImage(this, FileManager.getFiles(this)[id].getAbsolutePath()).execute();
    }

    private Handler handler;
    private int numberIterated;
    private boolean isDiashowRunning = false;
    public void startDiashow(View view) {
        numberIterated = 0;
        handler = new Handler();
        isDiashowRunning = true;
        hideActionBars();
        handler.postDelayed(diashowStepRunnable(), OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowStartDelay) * 1000);
    }

    private void iterateDiashow() {
        numberIterated++;
        Log.d("Orbis", "Number of iterations: " + numberIterated);
        switch(OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowMode)) {
            case 0:
                if(numberIterated >= OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowNumberOfImages)) {
                    handler.postDelayed(showActionBarsRunnable(), OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowTPI) * 1000);
                    return;
                }
                break;
            case 1:
                if(OrbisSettings.getBoolSetting(this, OrbisSettings.OrbisSetting.diashowDirection)) {
                    if(imageID == FileManager.getFiles(this).length - 1) {
                        handler.postDelayed(showActionBarsRunnable(), OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowTPI) * 1000);
                        return;
                    }
                } else {
                    if(imageID == 0) {
                        handler.postDelayed(showActionBarsRunnable(), OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowTPI) * 1000);
                        return;
                    }
                }
                break;
        }
        handler.postDelayed(diashowStepRunnable(), OrbisSettings.getIntSetting(this, OrbisSettings.OrbisSetting.diashowTPI) * 1000);
    }

    private Runnable showActionBarsRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                showActionBars();
                stopDiashow();
            }
        };
    }

    /**
      * NOTE: this method should not be called if the diashow is supposed to end; this will always loop over the end of the file array
      */
    private Runnable diashowStepRunnable() {
        Log.d("Orbis", "Diashow has progressed");
        return new Runnable() {
            @Override
            public void run() {
                int nextImgID;
                if(OrbisSettings.getBoolSetting(getContext(), OrbisSettings.OrbisSetting.diashowDirection)) {
                    if(imageID >= (FileManager.getFiles(getContext()).length) - 1) {
                        nextImgID = 0;
                    } else {
                        nextImgID = imageID + 1;
                    }
                } else {
                    if(imageID <= 0) {
                        nextImgID = FileManager.getFiles(getContext()).length - 1;
                    } else {
                        nextImgID = imageID - 1;
                    }
                }
                loadImage(nextImgID);
                iterateDiashow();
            }
        };
    }

    public Context getContext() {
        return this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isDiashowRunning)
            stopDiashow();
    }

    public void stopDiashow() {
        Log.d("Orbis", "Diashow has been stopped");
        handler.removeCallbacksAndMessages(null);
        isDiashowRunning = false;
    }
}