package com.tribe.app.presentation.view.dialog_fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/12/16.
 */
public class PointsDialogFragment extends BaseDialogFragment {

    private static final String LEVEL = "LEVEL";

    public static PointsDialogFragment newInstance(ScoreUtils.Level level) {
        Bundle args = new Bundle();
        PointsDialogFragment fragment = new PointsDialogFragment();
        args.putSerializable(LEVEL, level);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.imgLevel)
    ImageView imgLevel;

    @BindView(R.id.txtPopupTitle)
    TextViewFont txtPopupTitle;

    @BindView(R.id.txtPopupDesc)
    TextViewFont txtPopupDesc;

    @BindView(R.id.txtShare)
    TextViewFont txtShare;

    @BindView(R.id.txtDone)
    TextViewFont txtDone;

    @Inject
    Navigator navigator;

    @Inject
    User user;

    // VARIABLES
    private ScoreUtils.Level level;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        level = (ScoreUtils.Level) getArguments().getSerializable(LEVEL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.dialog_fragment_points, container, false);

        initUi(fragmentView);
        initDependencyInjector();

        return fragmentView;
    }

    @Override
    public void removeSubscriptions() {
        super.removeSubscriptions();

        if (subscriptions.hasSubscriptions() && subscriptions != null) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
    }

    @Override
    public void initUi(View view) {
        super.initUi(view);

        subscriptions.add(RxView.clicks(txtShare).subscribe(aVoid -> {
            navigator.openSms(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), getActivity());
            dismiss();
        }));

        subscriptions.add(RxView.clicks(txtDone).subscribe(aVoid -> {
            dismiss();
        }));

        imgLevel.setImageResource(level.getDrawableId());
        txtPopupTitle.setText(getString(R.string.newlevel_title, ScoreUtils.formatFloatingPoint(getContext(), level.getPoints())));
        txtPopupDesc.setText(getString(R.string.newlevel_description, getString(level.getStringId())));
    }

    /**
     * Dagger setup
     */
    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}
