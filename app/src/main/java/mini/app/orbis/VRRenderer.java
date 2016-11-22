package mini.app.orbis;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author JS
 */

public class VRRenderer implements GLSurfaceView.Renderer{
    private static final String vertexShaderCode = "" +
            "attribute vec4 position;\n" +
            "attribute vec2 inputTextureCoordinate;" +
            " \n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   gl_Position = position;\n" +
            "   textureCoordinate = inputTextureCoordinate;\n" +
            "}";

    private static final String fragmentShaderCode = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "uniform highp vec2 distParams;\n" +
            "uniform highp float viewWidthM;\n" +
            "uniform highp float screenToLensDistM;\n" +
            "\n" +
            "uniform highp float imageRatio;\n" +
            "uniform highp float viewRatio;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   highp float aspectRatio = imageRatio/viewRatio;\n" +
            "   highp vec2 srcTexCoord = textureCoordinate;\n" +
            "   srcTexCoord.y = textureCoordinate.y*aspectRatio+0.5-0.5*aspectRatio;" +
            "   highp vec2 center = vec2(0.25,0.5);\n" +
            "   if (srcTexCoord.x > 0.5)\n" +
            "   {\n" +
            "       center.x = 0.75;\n" +
            "   }\n" +
            "   highp float destDistM = sqrt((center.x-textureCoordinate.x)*(center.x-textureCoordinate.x)+((center.y-textureCoordinate.y)/viewRatio)*((center.y-textureCoordinate.y)/viewRatio))*viewWidthM;\n" +
            "   highp float viewTan = destDistM/screenToLensDistM;\n" +
            "   highp float srcDestM = viewTan*(1.0+distParams.x*viewTan*viewTan+distParams.y*viewTan*viewTan*viewTan*viewTan)*screenToLensDistM;\n" +
            "   srcTexCoord -= center;\n" +
            "   srcTexCoord *= srcDestM/destDistM;\n" +
            "   if (srcTexCoord.x < -0.25 || srcTexCoord.x > 0.25 || srcTexCoord.y < -0.5 || srcTexCoord.y > 0.5)\n" +
            "   {\n" +
            "       gl_FragColor = vec4(0.0,0.0,0.0,1.0);\n" +
            "   }\n" +
            "   else\n" +
            "   {\n" +
            "       gl_FragColor = texture2D(inputImageTexture, srcTexCoord+center);\n" +
            "   }\n" +
            "}\n";

    private static final int bytesPerFloat = 4;
    private static final int bytesPerShort = 2;
    private static final int coordsPerVertex = 3;
    private static final int texCoordsPerVertex = 2;

    private final float squareCoords[] = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };
    private final short order[] = {0,1,2,0,2,3};

    private final float textureCoords[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    private FloatBuffer vertexBuffer;
    private ShortBuffer orderBuffer;
    private FloatBuffer textureCoordBuffer;

    private int programHandle;
    private int glTextureDataHandle;

    private int positionHandle;
    private int texCoordHandle;
    private int textureHandle;
    private int viewWidthHeightRatioHandle;
    private int imageWidthHeightRatioHandle;
    private int viewWidthMHandle;
    private int screenToLensDistMHandle;
    private int distParamsHandle;

    private Bitmap image;
    private VRViewerProperties properties;
    private float viewWidthHeightRatio;

    /**
     * Image to be loaded. Should not be inScaled
     * @param image
     */
    public VRRenderer(VRViewerProperties properties, Bitmap image) {
        this.image = image;
        this.properties = properties;
    }

    /**
     * Image to be displayed. Should not be inScaled
     * @param image
     */
    public void setImage(Bitmap image) {
        this.image = image;
        /*int[] textureHandleArray = new int[1];
        GLES20.glGenTextures(1,textureHandleArray,0);
        glTextureDataHandle = textureHandleArray[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureDataHandle);*/
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);

        ByteBuffer vbb = ByteBuffer.allocateDirect(squareCoords.length*bytesPerFloat);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer obb = ByteBuffer.allocateDirect(order.length*bytesPerShort);
        obb.order(ByteOrder.nativeOrder());
        orderBuffer = obb.asShortBuffer();
        orderBuffer.put(order);
        orderBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length*bytesPerFloat);
        tbb.order(ByteOrder.nativeOrder());
        textureCoordBuffer = tbb.asFloatBuffer();
        textureCoordBuffer.put(textureCoords);
        textureCoordBuffer.position(0);

        int vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);
        programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle,vertexShaderHandle);
        GLES20.glAttachShader(programHandle,fragmentShaderHandle);
        GLES20.glLinkProgram(programHandle);

        int[] textureHandleArray = new int[1];
        GLES20.glGenTextures(1,textureHandleArray,0);
        glTextureDataHandle = textureHandleArray[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureDataHandle);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        viewWidthHeightRatio = (float) width/height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(programHandle);

        positionHandle = GLES20.glGetAttribLocation(programHandle, "position");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, coordsPerVertex, GLES20.GL_FLOAT, false, coordsPerVertex*bytesPerFloat, vertexBuffer);

        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, texCoordsPerVertex, GLES20.GL_FLOAT, false, texCoordsPerVertex*bytesPerFloat, textureCoordBuffer);

        textureHandle = GLES20.glGetUniformLocation(programHandle, "inputImageTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureDataHandle);
        GLES20.glUniform1i(textureHandle,0);

        viewWidthHeightRatioHandle = GLES20.glGetUniformLocation(programHandle, "viewRatio");
        GLES20.glUniform1f(viewWidthHeightRatioHandle, viewWidthHeightRatio);

        imageWidthHeightRatioHandle = GLES20.glGetUniformLocation(programHandle, "imageRatio");
        GLES20.glUniform1f(imageWidthHeightRatioHandle, (float) image.getWidth()/image.getHeight());

        viewWidthMHandle = GLES20.glGetUniformLocation(programHandle, "viewWidthM");
        GLES20.glUniform1f(viewWidthMHandle, properties.interLensDistM*2);

        screenToLensDistMHandle = GLES20.glGetUniformLocation(programHandle, "screenToLensDistM");
        GLES20.glUniform1f(screenToLensDistMHandle, properties.screenToLensDistM);

        distParamsHandle = GLES20.glGetUniformLocation(programHandle, "distParams");
        GLES20.glUniform2fv(distParamsHandle,1,properties.distortionCorrectionParams,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,order.length,GLES20.GL_UNSIGNED_SHORT,orderBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}