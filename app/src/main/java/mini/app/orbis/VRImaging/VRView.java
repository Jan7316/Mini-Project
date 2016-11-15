package mini.app.orbis.VRImaging;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * @author JS
 */

public class VRView extends GLSurfaceView {
    private final VRRenderer renderer;

    public VRView(Context context) {
        super(context);
        renderer = new VRRenderer();
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
