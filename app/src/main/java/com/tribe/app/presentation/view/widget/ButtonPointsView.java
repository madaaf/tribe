package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
public class ButtonPointsView extends LinearLayout {

    @IntDef({PROFILE, FB_ACTIVE, FB_PROGRESS, FB_DISABLED})
    public @interface ButtonType {}

    public static final int PROFILE = 0;
    public static final int FB_ACTIVE = 1;
    public static final int FB_PROGRESS = 2;
    public static final int FB_DISABLED = 3;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    Picasso picasso;

    @BindView(R.id.viewBG)
    View viewBG;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.txtLabel)
    TextViewFont txtLabel;

    @BindView(R.id.txtSubLabel)
    TextViewFont txtSubLabel;

    @BindView(R.id.txtPoints)
    TextViewFont txtPoints;

    // VARIABLES
    private int drawableId;
    private int type;
    private int label;
    private int subLabel;
    private int points;

    // RESOURCES
    private int radiusImage;
    private int marginVertical;

    public ButtonPointsView(Context context) {
        this(context, null);
        init(context, null);
    }

    public ButtonPointsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_button_points, this, true);
        ButterKnife.bind(this);

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        radiusImage = context.getResources().getDimensionPixelSize(R.dimen.radius_share_img);
        marginVertical = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_small);

        setOrientation(VERTICAL);
        setPadding(marginVertical, marginVertical, marginVertical, marginVertical);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonPointsView);

        setType(a.getInt(R.styleable.ButtonPointsView_buttonType, PROFILE));

        if (a.hasValue(R.styleable.ButtonPointsView_buttonDrawable))
            setDrawableResource(a.getResourceId(R.styleable.ButtonPointsView_buttonDrawable, 0));

        setLabel(a.getResourceId(R.styleable.ButtonPointsView_buttonLabel, R.string.contacts_share_profile_button));
        setSubLabel(a.getResourceId(R.styleable.ButtonPointsView_buttonSubLabel, R.string.contacts_share_profile_description));
        setPoints(a.getInteger(R.styleable.ButtonPointsView_buttonPoints, 0));

        a.recycle();
    }

    public void setType(int type) {
        this.type = type;

        if (type == PROFILE) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        setBackgroundButton();
    }

    public void setBackgroundButton() {
        if (type == PROFILE)
            viewBG.setBackgroundResource(R.drawable.bg_button_blue_text);
        else if (type == FB_ACTIVE)
            viewBG.setBackgroundResource(R.drawable.bg_button_fb_light);
        else if (type == FB_DISABLED)
            viewBG.setBackgroundResource(R.drawable.bg_button_disabled);
    }

    public void setDrawableResource(int res) {
        this.drawableId = res;

        if (type != PROFILE) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(drawableId);
        }
    }

    public void setLabel(int res) {
        label = res;
        txtLabel.setText(label);
    }

    public void setSubLabel(int res) {
        subLabel = res;
        txtSubLabel.setText(res);
    }

    public void setDrawable(String url) {
        if (type == PROFILE) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            picasso.load(url).fit().centerCrop()
                    .transform(new RoundedCornersTransformation(radiusImage, 0, RoundedCornersTransformation.CornerType.LEFT))
                    .into(imageView);
        }
    }

    public void setPoints(int points) {
        txtPoints.setText(getContext().getString(R.string.points_suffix, points));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
