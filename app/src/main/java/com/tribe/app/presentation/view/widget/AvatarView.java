package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 17/02/2016.
 */
public class AvatarView extends RoundedCornerLayout {

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    // VARIABLES
    private boolean hasBorder = true;

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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
        hasBorder = a.getBoolean(R.styleable.AvatarView_border, false);

        setWillNotDraw(false);
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (hasBorder) {
            float borderWidth = 1f;

            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            borderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, borderWidth, metrics);

            Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintBorder.setAntiAlias(true);
            paintBorder.setColor(Color.WHITE);
            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setAntiAlias(true);
            paintBorder.setStrokeWidth(borderWidth);

            float viewWidth = getWidth() - ((int) borderWidth * 2);
            float circleCenter = viewWidth / 2;

            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth,
                    circleCenter, paintBorder);
        }
    }

    public void load(String url) {
        if (!StringUtils.isEmpty(url)) {
            Glide.with(getContext())
                    .load(url)
                    .centerCrop()
                    .crossFade()
                    .into(imgAvatar);
        } else {
            Glide.with(getContext())
                    .load(R.drawable.picto_avatar_placeholder)
                    .crossFade()
                    .into(imgAvatar);
        }
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }
}
