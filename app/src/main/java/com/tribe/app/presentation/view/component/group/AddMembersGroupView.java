package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/21/2016.
 */

public class AddMembersGroupView extends FrameLayout {

    private int DURATION_FADE = 150;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    // VARIABLES

    // OBSERVABLES
    private CompositeSubscription subscriptions;

    public AddMembersGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

        super.onDetachedFromWindow();
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }
}
