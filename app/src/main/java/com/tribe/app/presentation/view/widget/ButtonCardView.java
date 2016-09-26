package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 08/19/2016.
 */
public class ButtonCardView extends LinearLayout {

    @IntDef({POINTS, METEO, LOCATION, CITY})
    public @interface ButtonType {}

    public static final int POINTS = 0;
    public static final int METEO = 1;
    public static final int LOCATION = 2;
    public static final int CITY = 3;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.imageView)
    ImageView imageView;

    @Nullable
    @BindView(R.id.txtLabel)
    TextViewFont txtLabel;

    @Nullable
    @BindView(R.id.txtPoints)
    TextViewFont txtPoints;

    // VARIABLES
    private int drawableId;
    private int type;

    // RESOURCES
    private int margin;

    // OBSERVABLES
    private final PublishSubject<View> clickButton = PublishSubject.create();

    public ButtonCardView(Context context) {
        this(context, null);
        init(context, null);
    }

    public ButtonCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        margin = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonCardView);

        type =  a.getInt(R.styleable.ButtonCardView_buttonCardType, LOCATION);

        if (a.hasValue(R.styleable.ButtonCardView_buttonCardDrawable))
            setDrawableResource(a.getResourceId(R.styleable.ButtonCardView_buttonCardDrawable, 0));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (type == POINTS)
            inflater.inflate(R.layout.view_button_card_points, this, true);
        else
            inflater.inflate(R.layout.view_button_card, this, true);
        ButterKnife.bind(this);

        setBackgroundCard();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setPadding(margin, margin, margin, margin);

        a.recycle();
    }

    private void setBackgroundCard() {
        if (type == LOCATION) {
            setBackgroundResource(R.drawable.bg_cards_location);
        } else {
            setBackgroundResource(R.drawable.bg_cards);
        }
    }

    public void setDrawableResource(int res) {
        this.drawableId = res;
        imageView.setImageResource(drawableId);
    }

    public void setText(String str) {
        txtLabel.setText(str);
    }

    public void setText(int str) {
        txtLabel.setText(str);
    }

    public void setLevel(ScoreUtils.Level level) {
        txtPoints.setText(level.getStringId());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    // OBSERVABLES
    public Observable<View> onClick() {
        return clickButton;
    }
}
