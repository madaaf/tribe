package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

/**
 * Created by tiago on 17/02/2016.
 */
public class AvatarView extends RoundedCornerLayout {

    @Inject
    Picasso picasso;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    public AvatarView(Context context) {
        this(context, null);
        init(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_avatar, this, true);
        ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        float borderWidth = 1f;

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        borderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, borderWidth, metrics);

        Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setAntiAlias(true);
        paintBorder.setColor(Color.WHITE);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setAntiAlias(true);
        paintBorder.setStrokeWidth(borderWidth);

        int viewWidth = getWidth() - ((int) borderWidth * 2);
        int circleCenter = viewWidth / 2;

        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth,
                circleCenter + borderWidth, paintBorder);
    }

    public void load(String url) {
        picasso.load(R.drawable.fry)
                .fit()
                .centerCrop()
                .into(imgAvatar);
    }
}
