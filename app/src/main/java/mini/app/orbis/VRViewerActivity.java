package mini.app.orbis;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cyberagent.android.gpuimage.GPUImage;

/**
 * @author JS
 * Activity that displays two images side by side with distortion correction applied
 */

public class VRViewerActivity extends AppCompatActivity {
    private VRViewerProperties properties;
    private DistortionCorrectionFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrviewer);

        properties = new VRViewerProperties();
        filter = new DistortionCorrectionFilter(properties);

        setViewDimensions(R.id.leftView);
        setViewDimensions(R.id.rightView);

        Bitmap bitmap = null; // TODO: Load pictures from storage into the viewer
        createGPUImage(bitmap, R.id.leftView);
        bitmap = null;
        createGPUImage(bitmap, R.id.rightView);

        filter.setImageWidthToHeightRatio((float)bitmap.getWidth()/bitmap.getHeight());
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

    /**
     * Sets the absolute dimensions of a GLSurfaceView to match the physical dimensions of the headset
     * @param id id of the adjusted view
     */
    private void setViewDimensions(int id) {
        View view = findViewById(id);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,
                properties.interLensDistM*1000, getResources().getDisplayMetrics());;
        view.setLayoutParams(params);
    }

    /**
     * Creates a GPUImage from a bitmap and displays it in the given GLSurfaceView with distortion correction applied
     * @param bitmap The image to be displayed
     * @param glSurfaceViewId The id of the target view
     * @return A pointer to the created GPUImage
     */
    private GPUImage createGPUImage(Bitmap bitmap, int glSurfaceViewId) {
        GPUImage image = new GPUImage(this);
        image.setGLSurfaceView((GLSurfaceView) findViewById(glSurfaceViewId));
        image.setImage(bitmap);
        image.setFilter(filter);
        return image;
    }

    /*private Bitmap loadScaledImage(Resources res, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,id,options);

        final int width = options.outWidth;
        int inSampleSize = 4;
        int reqWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,
                properties.interLensDistM*1000, getResources().getDisplayMetrics());
        while (width/inSampleSize > reqWidth) {
            inSampleSize *= 2;
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,id,options);
    }*/
}