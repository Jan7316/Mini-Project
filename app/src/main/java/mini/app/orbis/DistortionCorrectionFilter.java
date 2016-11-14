package mini.app.orbis;

import android.graphics.PointF;
import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * @author JS
 * A filter that applies distortion correction to GPUImages
 */

public class DistortionCorrectionFilter extends GPUImageFilter {

    public static final String DISTORTION_CORRECTION_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "uniform highp vec2 center;\n" +
            "uniform highp vec2 distParams;\n" +
            "uniform highp float viewWidthM;\n" +
            "uniform highp float screenToLensDistM;\n" +
            "uniform highp float widthToHeightRatio;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "highp float destDistM = distance(center,textureCoordinate)*viewWidthM;\n" +
            "highp float viewTan = destDistM/screenToLensDistM;\n" +
            "highp float srcDestM = viewTan*(1.0+distParams.x*viewTan*viewTan+distParams.y*viewTan*viewTan*viewTan*viewTan)*screenToLensDistM;\n" +
            "highp vec2 srcTexCoord = textureCoordinate-center;\n" +
            "srcTexCoord *= srcDestM/destDistM;\n" +
            "if (widthToHeightRatio > 1.0)\n" +
            "{\n" +
            "srcTexCoord *= widthToHeightRatio;\n" +
            "}\n" +
            "srcTexCoord += center;\n" +
            "\n" +
            "if (srcTexCoord.x < 0.0 || srcTexCoord.x > 1.0 || srcTexCoord.y < 0.0 || srcTexCoord.y > 1.0)\n" +
            "{\n" +
            "gl_FragColor = vec4(0.0,0.0,0.0,1.0);\n" +
            "}\n" +
            "else\n" +
            "{\n" +
            "gl_FragColor = texture2D(inputImageTexture,srcTexCoord);\n" +
            "}\n" +
            "}\n";

    private PointF center;
    private float[] distortionCorrectionParams;
    private float viewWidthM;
    private float screenToLensDistM;
    private float widthToHeightRatio;

    private int centerLocation;
    private int distortionCorrectionParamsLocation;
    private int viewWidthMLocation;
    private int screenToLensDistMLocation;
    private int widthToHeightRatioLocation;

    /**
     * @param properties The physical properties of the VR headset
     */
    public DistortionCorrectionFilter(VRViewerProperties properties) {
        super(NO_FILTER_VERTEX_SHADER,DISTORTION_CORRECTION_FRAGMENT_SHADER);
        center = new PointF(0.5f,0.5f);
        distortionCorrectionParams = properties.distortionCorrectionParams;
        viewWidthM = properties.interLensDistM;
        screenToLensDistM = properties.screenToLensDistM;
        widthToHeightRatio = 1;
    }

    public void onInit() {
        super.onInit();
        centerLocation = GLES20.glGetUniformLocation(getProgram(),"center");
        distortionCorrectionParamsLocation = GLES20.glGetUniformLocation(getProgram(),"distParams");
        viewWidthMLocation = GLES20.glGetUniformLocation(getProgram(),"viewWidthM");
        screenToLensDistMLocation = GLES20.glGetUniformLocation(getProgram(),"screenToLensDistM");
        widthToHeightRatioLocation = GLES20.glGetUniformLocation(getProgram(), "widthToHeightRatio");
    }

    public void onInitialized() {
        super.onInitialized();
        setPoint(centerLocation,center);
        setFloatVec2(distortionCorrectionParamsLocation,distortionCorrectionParams);
        setFloat(viewWidthMLocation,viewWidthM);
        setFloat(screenToLensDistMLocation,screenToLensDistM);
        setFloat(widthToHeightRatioLocation, widthToHeightRatio);
    }

    /**
     * Sets the width/height ratio of the image to be distorted for correct scaling. It is 1 by
     * default, which means that the image is going to be scaled so it the height of the view
     * @param widthToHeightRatio width/height of the images to be distorted
     */
    public void setImageWidthToHeightRatio(float widthToHeightRatio) {
        this.widthToHeightRatio = widthToHeightRatio;
        setFloat(widthToHeightRatioLocation,widthToHeightRatio);
    }
}