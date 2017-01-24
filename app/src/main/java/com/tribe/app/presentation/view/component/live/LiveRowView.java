package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static io.fabric.sdk.android.services.concurrency.AsyncTask.init;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveRowView extends FrameLayout {

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewWaiting)
    LiveWaitingView viewWaiting;

    // VARIABLES
    private Unbinder unbinder;

    public LiveRowView(Context context) {
        super(context);
        init();
    }

    public LiveRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        LayoutInflater.from(getContext()).inflate(R.layout.view_row_live, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        super.onFinishInflate();
    }

    private void setColor(int color) {
        viewWaiting.setColor(color);
    }
}
