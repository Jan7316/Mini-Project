package mini.app.orbis.VRImaging;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author  JS
 */

public class VRCanvas {
    public static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";

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

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private final int shaderProgram;

    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f,  0.5f, 0.0f };

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    public VRCanvas() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = VRRenderer.loadShader(GLES20.GL_VERTEX_SHADER,VERTEX_SHADER);
        int fragmentShader = VRRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,DISTORTION_CORRECTION_FRAGMENT_SHADER);
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
    }


    /*public void draw() {
        GLES20.glUseProgram(shaderProgram);

        int positionLocation = GLES20.glGetAttribLocation(shaderProgram, "position");
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glVertexAttribPointer(positionLocation, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int inputTextureCoordinateLocation = GLES20.glGetAttribLocation(shaderProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glVertexAttribPointer(positionLocation, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int textureLocation = GLES20.glGetUniformLocation(shaderProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }*/
}
