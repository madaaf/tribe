package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.tribe.app.R;

public class CircularImageView extends ImageView {

    private boolean hasBorder;
    private int borderWidth;
    private int canvasSize;

    // Objects used for the actual drawing
    private BitmapShader shader;
    private Bitmap image;
    private Paint paint;
    private Paint paintBorder;
    private Paint paintSelectorBorder;

    public CircularImageView(Context context) {
        this(context, null);
        init(context, null);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Initializes paint objects and sets desired attributes.
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        // Initialize paint objects
        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
        paintSelectorBorder = new Paint();
        paintSelectorBorder.setAntiAlias(true);

        // load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView);

        // Check if border and/or border is enabled
        hasBorder = attributes.getBoolean(R.styleable.CircularImageView_border, false);

        // Border
        if (hasBorder) {
            int defaultBorderSize = (int) (2 * context.getResources().getDisplayMetrics().density + 0.5f);
            setBorderWidth(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_border_width, defaultBorderSize));
            setBorderColor(attributes.getColor(R.styleable.CircularImageView_border_color, Color.WHITE));
        }

        // Shadow
        if (attributes.getBoolean(R.styleable.CircularImageView_shadow, false))
            addShadow();

        attributes.recycle();
    }

    /**
     * Sets the CircularImageView's border width in pixels.
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.requestLayout();
        this.invalidate();
    }

    /**
     * Sets the CircularImageView's basic border color.
     *
     * @param borderColor
     */
    public void setBorderColor(int borderColor) {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);
        this.invalidate();
    }

    /**
     * Adds a dark shadow to this CircularImageView.
     */
    public void addShadow() {
        setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        paintBorder.setShadowLayer(2.5f, 0.0f, 1f, getContext().getResources().getColor(R.color.black_opacity_50));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (image == null)
            return;

        if (image.getHeight() == 0 || image.getWidth() == 0)
            return;

        int oldCanvasSize = canvasSize;

        canvasSize = canvas.getWidth();
        if (canvas.getHeight() < canvasSize)
            canvasSize = canvas.getHeight();

        if (oldCanvasSize != canvasSize)
            refreshBitmapShader();

        paint.setShader(shader);

        int outerWidth = 0;

        int center = canvasSize / 2;

        if (hasBorder) {
            outerWidth = borderWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;
            paint.setColorFilter(null);
            canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - 4.0f, paintBorder);
        } else {
            paint.setColorFilter(null);
        }

        canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) - 4.0f, paint);
    }

    public void invalidate(Rect dirty) {
        super.invalidate(dirty);
        image = drawableToBitmap(getDrawable());
        if (shader != null || canvasSize > 0)
            refreshBitmapShader();
    }

    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        image = drawableToBitmap(getDrawable());
        if (shader != null || canvasSize > 0)
            refreshBitmapShader();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        image = drawableToBitmap(getDrawable());
        if (shader != null || canvasSize > 0)
            refreshBitmapShader();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }

        return (result + 2);
    }

    /**
     * Convert a drawable object into a Bitmap
     *
     * @param drawable
     * @return
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) { // Don't do anything without a proper drawable
            return null;
        } else if (drawable instanceof BitmapDrawable) { // Use the getBitmap() method instead if BitmapDrawable
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Reinitializes the shader texture used to fill in
     * the Circle upon drawing.
     */
    public void refreshBitmapShader() {
        shader = new BitmapShader(Bitmap.createScaledBitmap(image, canvasSize, canvasSize, false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }
}