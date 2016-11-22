package mini.app.orbis;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * @author JS
 * A placeholder implementation, enables further extensions in newer versions
 */

public class VRView extends GLSurfaceView {
    public VRView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}