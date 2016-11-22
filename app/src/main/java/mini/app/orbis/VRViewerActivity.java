package mini.app.orbis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author JS
 * Activity that displays a 3D image with distortion correction applied
 */

public class VRViewerActivity extends AppCompatActivity implements AsyncTaskLoadVRImage.ITaskParent {
    private VRViewerProperties properties;
    private VRRenderer renderer;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrviewer);

        // Initialize renderer and attach it to the view
        properties = new VRViewerProperties();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.id.loading_indicator);
        renderer = new VRRenderer(properties, bitmap);
        VRView view = (VRView) findViewById(R.id.vrView);
        view.setRenderer(renderer);

        // Adjust absolute dimensions of the view
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,
                properties.interLensDistM*1000*2, getResources().getDisplayMetrics());;
        view.setLayoutParams(params);

        path = getIntent().getStringExtra(GlobalVars.EXTRA_PATH);
        new AsyncTaskLoadVRImage(this, path).execute();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
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
        renderer.setImage(image);
    }
}