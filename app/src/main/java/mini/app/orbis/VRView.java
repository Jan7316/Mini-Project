package mini.app.orbis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @author JS
 * A placeholder implementation, enables further extensions in newer versions
 */

public class VRView extends GLSurfaceView {
    private final VRRenderer renderer;
    public VRView(Context context, Bitmap image) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new VRRenderer(new VRViewerProperties(), image);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
    public void setImage(Bitmap image) {
        renderer.setImage(image);
    }
}