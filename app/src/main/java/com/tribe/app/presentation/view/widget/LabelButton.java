package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 08/19/2016.
 */
public class LabelButton extends LinearLayout {

    @IntDef({ACTION, INFOS})
    public @interface LabelType {}

    public static final int ACTION = 1;
    public static final int INFOS = 0;

    private final static int DRAWABLE_PADDING = 10;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    Picasso picasso;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.txtLabel)
    TextViewFont txtLabel;

    // VARIABLES
    private int drawableId;
    private int type;
    private String label;

    // RESOURCES
    private int radiusImage;

    public LabelButton(Context context) {
        this(context, null);
        init(context, null);
    }

    public LabelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_label_button, this, true);
        ButterKnife.bind(this);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        radiusImage = context.getResources().getDimensionPixelSize(R.dimen.radius_gallery_img);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LabelButton);

        setType(a.getInt(R.styleable.LabelButton_type, INFOS));

        if (a.hasValue(R.styleable.LabelButton_drawable))
            setDrawableResource(a.getResourceId(R.styleable.LabelButton_drawable, 0));

        a.recycle();
    }

    private void setTextAppearence(int resId) {
        if (Build.VERSION.SDK_INT < 23) {
            txtLabel.setTextAppearance(getContext(), resId);
        } else {
            txtLabel.setTextAppearance(resId);
        }
    }

    public void setType(int type) {
        this.type = type;
        setTextAppearence(type == INFOS ? R.style.Body_One_White : R.style.Body_Two_Black);
        setBackgroundResource(type == INFOS ? R.drawable.bg_infos_transparent_disabled : R.drawable.bg_infos_transparent_enabled);
    }

    public void setDrawableResource(int resource) {
        this.drawableId = resource;
        picasso.load(resource).fit().centerCrop()
                .transform(new RoundedCornersTransformation(radiusImage, 0, RoundedCornersTransformation.CornerType.ALL)).into(imageView);
    }

    public void setText(String text) {
        txtLabel.setText(text);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
