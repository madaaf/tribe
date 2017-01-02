package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class TileView extends SquareFrameLayout {

    public final static int TYPE_GRID = 0;

    @Inject
    ScreenUtils screenUtils;

    @Nullable @BindView(R.id.txtName) public TextViewFont txtName;
    @BindView(R.id.viewShadow) public View viewShadow;
    @BindView(R.id.avatar) public AvatarView avatar;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private Unbinder unbinder;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> click = PublishSubject.create();

    // RESOURCES
    private int sizeAvatar;

    // VARIABLES
    private Recipient recipient;
    private int type;

    public TileView(Context context) {
        super(context);
        init(context, null);
    }

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        type = a.getInt(R.styleable.TileView_tileType, TYPE_GRID);
        a.recycle();

        subscriptions = new CompositeSubscription();

        sizeAvatar = context.getResources().getDimensionPixelSize(R.dimen.avatar_size);

        int resLayout = 0;

        switch (type) {
            case TYPE_GRID:
                resLayout = R.layout.view_tile_grid;
                break;
        }

        LayoutInflater.from(getContext()).inflate(resLayout, this);
        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void initSize() {
        int size = (int) ((screenUtils.getWidthPx() >> 1) * 0.4f);
        ViewGroup.LayoutParams params = avatar.getLayoutParams();
        params.width = size;
        params.height = size;
        avatar.setLayoutParams(params);

        params = viewShadow.getLayoutParams();
        params.width = size + screenUtils.dpToPx(25);
        params.height = size + screenUtils.dpToPx(25);
        viewShadow.setLayoutParams(params);

        avatar.invalidate();
        avatar.requestLayout();
    }

    public void initClicks() {
        if (type == TYPE_GRID) {
            prepareTouchesMore();
            prepareClickOnView();
        }
    }

    private void prepareTouchesMore() {
        txtName.setOnClickListener(v -> clickMoreView.onNext(this));
    }

    private void prepareClickOnView() {
        setOnClickListener(v -> {
            click.onNext(v);
        });
    }

    public void setInfo(Recipient recipient) {
        this.recipient = recipient;
        avatar.load(recipient);

        if (type == TYPE_GRID) {
            if (recipient instanceof Membership) {
                txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
            } else {
                txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            txtName.setText(recipient.getDisplayName());
        }
    }

    public void setBackground(int position) {
        UIUtils.setBackgroundGrid(screenUtils, this, position, true);
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onClick() { return click; }

    private void initDependencyInjector() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
    }
}
