package com.tribe.app.presentation.view.fragment;

import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.widget.EditTextFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment that shows a list of media.
 */
public class GroupsGridFragment extends BaseFragment {

    public GroupsGridFragment() {
        setRetainInstance(true);
    }

    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @BindView(R.id.editTextGroupName)
    EditTextFont editTextGroupName;

    @BindView(R.id.viewCreateGroupBg1)
    View viewCreateGroupBg1;
    @BindView(R.id.viewCreateGroupBg2)
    View getViewCreateGroupBg2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_groups_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initUi();
        fragmentView.setTag(HomeActivity.GROUPS_FRAGMENT_PAGE);
        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initUi() {
        subscriptions.add(RxTextView.textChanges(editTextGroupName).subscribe(charSequence -> {
            if (editTextGroupName.getText().toString().length() > 0) {
                viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rect_green_group_rounded_corners));
                getViewCreateGroupBg2.setClickable(true);
            }
            else {
                viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rect_grey_group_rounded_corners));
                getViewCreateGroupBg2.setClickable(false);
            }
        }));

        subscriptions.add(RxView.clicks(getViewCreateGroupBg2).subscribe(aVoid -> {
            createGroupLoadingAnim();
        }));
    }

    @Override
    public void onDetach() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }


        super.onDetach();
    }

    public void createGroupLoadingAnim() {
        Rect rect = new Rect();
        getViewCreateGroupBg2.getLocalVisibleRect(rect);
        Rect from = new Rect(rect);
        Rect to = new Rect(rect);
        from.right = 0;
        getViewCreateGroupBg2.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofObject(getViewCreateGroupBg2,
                "clipBounds",
                new RectEvaluator(),
                from, to);
        anim.setDuration(2000);
        anim.start();
    }

}
