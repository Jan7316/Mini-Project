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
import android.widget.RelativeLayout;

/**
 * @author JS
 * Activity that displays a 3D image with distortion correction applied
 *
 */

public class VRViewerActivity extends AppCompatActivity {// implements AsyncTaskLoadVRImage.ITaskParent {
    private VRViewerProperties properties;
    private VRView view;
    //private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        properties = new VRViewerProperties();

        RelativeLayout layout = new RelativeLayout(this);
        view = new VRView(this, BitmapFactory.decodeFile(getIntent().getStringExtra(GlobalVars.EXTRA_PATH)));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,
                properties.interLensDistM*1000*2, getResources().getDisplayMetrics());
        view.setLayoutParams(layoutParams);
        layout.addView(view);

        setContentView(layout);

        //path = getIntent().getStringExtra(GlobalVars.EXTRA_PATH);
        //new AsyncTaskLoadVRImage(this, path).execute();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /*@Override
    public void onImagesLoaded(Bitmap image) {
        view.setImage(image);
    }*/
}