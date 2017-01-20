package com.tribe.app.presentation.view.widget.avatar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 17/02/2016.
 */
public class AvatarLiveView extends RelativeLayout implements Avatar {

    @IntDef({ LIVE, CONNECTED })
    public @interface AvatarLiveType {}

    public static final int LIVE = 0;
    public static final int CONNECTED = 1;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.avatarView)
    AvatarView avatar;

    @BindView(R.id.imgInd)
    ImageView imgInd;

    // VARIABLES
    private int type;

    // RESOURCES

    // SUBSCRIPTIONS

    public AvatarLiveView(Context context) {
        this(context, null);
        init(context, null);
    }

    public AvatarLiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_avatar_live, this, true);
        ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarLiveView);
        setType(a.getInt(R.styleable.AvatarLiveView_avatarLiveType, LIVE));

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                refactorSize();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getWidth() != 0 && getHeight() != 0) {
            refactorSize();
        }
    }

    @Override
    public void load(Recipient recipient) {
        avatar.load(recipient);
    }

    @Override
    public void load(String url) {
        avatar.load(url);
    }

    public void setType(int type) {
        this.type = type;

        if (type == LIVE) imgInd.setImageResource(R.drawable.picto_live);
        else imgInd.setImageResource(R.drawable.picto_online);
    }

    private void refactorSize() {
        MarginLayoutParams params = (MarginLayoutParams) avatar.getLayoutParams();
        params.width = getWidth();
        params.height = getHeight();
        avatar.setLayoutParams(params);

        params = (MarginLayoutParams) imgInd.getLayoutParams();
        params.width = avatar.getRadius() * 2;
        params.height = avatar.getRadius() * 2;
        imgInd.setLayoutParams(params);
    }
}
