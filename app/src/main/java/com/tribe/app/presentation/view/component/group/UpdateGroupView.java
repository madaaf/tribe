package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.io.Serializable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class UpdateGroupView extends LinearLayout {

    private int DURATION_FADE = 150;

    @Inject
    RxImagePicker rxImagePicker;

    @BindView(R.id.avatarView)
    AvatarView avatarView;

    @BindView(R.id.editGroupName)
    EditTextFont editGroupName;

    // VARIABLES
    private String imgUri;
    private BottomSheetDialog dialogCamera;
    private LabelSheetAdapter cameraTypeAdapter;
    private Membership membership;
    private GroupEntity groupEntity;

    // OBSERVABLES
    private CompositeSubscription subscriptions;

    public UpdateGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (membership == null) {
            Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);

            if (serializable instanceof Membership) {
                membership = (Membership) serializable;
            }

            init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    private void init() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

        groupEntity = new GroupEntity();

        subscriptions.add(
                RxTextView.textChanges(editGroupName)
                        .map(CharSequence::toString)
                        .subscribe(s -> {
                            groupEntity.setName(s);
                        })
        );

        loadAvatar(membership);
        editGroupName.setText(membership.getDisplayName());
    }

    @OnClick(R.id.avatarView)
    void clickAvatar() {
        subscriptions.add(DialogFactory.showBottomSheetForCamera(getContext())
                .subscribe(labelType -> {
                    if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA)
                                .subscribe(uri -> {
                                    loadUri(uri);
                                }));
                    } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY)
                                .subscribe(uri -> {
                                    loadUri(uri);
                                }));
                    }
                })
        );
    }

    private void loadAvatar(Membership membership) {
        avatarView.load(membership);
    }

    private void loadAvatar(String url) {
        avatarView.load(url);
    }

    public void loadUri(Uri uri) {
        imgUri = uri.toString();
        groupEntity.setImgPath(uri.toString());
        loadAvatar(uri.toString());
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    /**
     * OBSERVABLES
     */

}