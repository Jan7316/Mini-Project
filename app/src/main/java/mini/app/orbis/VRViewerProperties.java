package mini.app.orbis;

/**
 * @author JS
 * Provides the physical properties of the VR headset
 */

public class VRViewerProperties {
    public final float interLensDistM;
    public final float screenToLensDistM;
    public final float[] distortionCorrectionParams;

    public VRViewerProperties() {
        this(0.0639f, 0.0393f, new float[]{0.441f, 0.156f});
    }

    private VRViewerProperties(float interLensDistM, float screenToLensDistM, float[] distortionCorrectionParams) {
        this.interLensDistM = interLensDistM;
        this.screenToLensDistM = screenToLensDistM;
        this.distortionCorrectionParams = distortionCorrectionParams;
    }
}