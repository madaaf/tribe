package com.tribe.app.presentation.view.camera.shader;

import android.content.res.Resources;

import com.tribe.app.presentation.view.camera.renderer.GLES20FramebufferObject;
import com.tribe.app.presentation.view.camera.utils.OpenGlUtils;

import java.util.HashMap;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class GlShader {

    public static final String DEFAULT_ATTRIB_POSITION = "aPosition";
    public static final String DEFAULT_ATTRIB_TEXTURE_COORDINATE = "aTextureCoord";
    public static final String DEFAULT_UNIFORM_SAMPLER = "sTexture";

    protected static final String DEFAULT_VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "gl_Position = aPosition;\n" +
                    "vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

    protected static final String DEFAULT_FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private static final float[] VERTICES_DATA = new float[]{
            // X, Y, Z, U, V
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
    };

    private static final int FLOAT_SIZE_BYTES = 4;
    protected static final int VERTICES_DATA_POS_SIZE = 3;
    protected static final int VERTICES_DATA_UV_SIZE = 2;
    protected static final int VERTICES_DATA_STRIDE_BYTES = (VERTICES_DATA_POS_SIZE + VERTICES_DATA_UV_SIZE) * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_POS_OFFSET = 0 * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_UV_OFFSET = VERTICES_DATA_POS_OFFSET + VERTICES_DATA_POS_SIZE * FLOAT_SIZE_BYTES;

    private final String vertexShaderSource;
    private final String fragmentShaderSource;
    private int program;
    private int vertexShader;
    private int fragmentShader;
    private int vertexBufferName;

    private final HashMap<String, Integer> handleMap = new HashMap<String, Integer>();

    public GlShader() {
        this(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
    }

    public GlShader(final Resources res, final int vertexShaderSourceResId, final int fragmentShaderSourceResId) {
        this(res.getString(vertexShaderSourceResId), res.getString(fragmentShaderSourceResId));
    }

    public GlShader(final String vertexShaderSource, final String fragmentShaderSource) {
        this.vertexShaderSource = vertexShaderSource;
        this.fragmentShaderSource = fragmentShaderSource;
    }

    public void setup() {
        release();
        vertexShader = OpenGlUtils.loadShader(vertexShaderSource, GL_VERTEX_SHADER);
        fragmentShader = OpenGlUtils.loadShader(fragmentShaderSource, GL_FRAGMENT_SHADER);
        program = OpenGlUtils.createProgram(vertexShader, fragmentShader);
        vertexBufferName = OpenGlUtils.createBuffer(VERTICES_DATA);
    }

    public void setFrameSize(final int width, final int height) {
    }

    public void release() {
        glDeleteProgram(program);
        program = 0;
        glDeleteShader(vertexShader);
        vertexShader = 0;
        glDeleteShader(fragmentShader);
        fragmentShader = 0;
        glDeleteBuffers(1, new int[]{vertexBufferName}, 0);
        vertexBufferName = 0;
        handleMap.clear();
    }

    public void draw(final int texName, final GLES20FramebufferObject fbo) {
        useProgram();

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferName);
        glEnableVertexAttribArray(getHandle("aPosition"));
        glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        glEnableVertexAttribArray(getHandle("aTextureCoord"));
        glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texName);
        glUniform1i(getHandle("sTexture"), 0);

        onDraw();

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glDisableVertexAttribArray(getHandle("aPosition"));
        glDisableVertexAttribArray(getHandle("aTextureCoord"));
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    protected void onDraw() {
    }

    protected final void useProgram() {
        glUseProgram(program);
    }

    protected final int getVertexBufferName() {
        return vertexBufferName;
    }

    protected final int getHandle(final String name) {
        final Integer value = handleMap.get(name);

        if (value != null) {
            return value.intValue();
        }

        int location = glGetAttribLocation(program, name);
        if (location == -1) {
            location = glGetUniformLocation(program, name);
        }

        if (location == -1) {
            throw new IllegalStateException("Could not get attrib or uniform location for " + name);
        }

        handleMap.put(name, Integer.valueOf(location));
        return location;
    }

    protected final int[] getHandles(final String... names) {
        if (names == null) {
            return null;
        }

        final int[] results = new int[names.length];
        int count = 0;
        for (final String name : names) {
            results[count] = getHandle(name);
            count++;
        }

        return results;
    }
}
