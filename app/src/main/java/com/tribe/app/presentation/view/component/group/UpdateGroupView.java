package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.CameraType;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.NewGroupEntity;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class UpdateGroupView extends LinearLayout {

    private int DURATION_FADE = 150;

    @Inject
    RxImagePicker rxImagePicker;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.editGroupName)
    EditTextFont editGroupName;

    // VARIABLES
    private String imgUri;
    private BottomSheetDialog dialogCamera;
    private LabelSheetAdapter cameraTypeAdapter;
    private Membership membership;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<NewGroupEntity> updateGroup = PublishSubject.create();

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

        subscriptions.add(
                RxTextView.textChanges(editGroupName)
                        .map(CharSequence::toString)
                        .subscribe(s -> {

                        })
        );

        loadAvatar(membership.getProfilePicture());
        editGroupName.setText(membership.getDisplayName());
    }

    @OnClick(R.id.imgAvatar)
    void clickAvatar() {
        setupBottomSheetCamera();
    }

    private void loadAvatar(String url) {
        Glide.with(getContext()).load(url)
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgAvatar);
    }

    /**
     * Bottom sheet set-up
     */
    private void prepareBottomSheetCamera(List<LabelType> items) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_camera_type, null);
        RecyclerView recyclerViewCameraType = (RecyclerView) view.findViewById(R.id.recyclerViewCameraType);
        recyclerViewCameraType.setHasFixedSize(true);
        recyclerViewCameraType.setLayoutManager(new LinearLayoutManager(getContext()));
        cameraTypeAdapter = new LabelSheetAdapter(getContext(), items);
        cameraTypeAdapter.setHasStableIds(true);
        recyclerViewCameraType.setAdapter(cameraTypeAdapter);
        subscriptions.add(cameraTypeAdapter.clickLabelItem()
                .map((View labelView) -> cameraTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    CameraType cameraType = (CameraType) labelType;

                    if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_CAMERA)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA)
                                .subscribe(uri -> {
                                    loadUri(uri);
                                }));
                    } else if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_PHOTOS)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY)
                                .subscribe(uri -> {
                                    loadUri(uri);
                                }));
                    }

                    dismissDialogSheetCamera();
                }));

        dialogCamera = new BottomSheetDialog(getContext());
        dialogCamera.setContentView(view);
        dialogCamera.show();
        dialogCamera.setOnDismissListener(dialog -> {
            cameraTypeAdapter.releaseSubscriptions();
            dialogCamera = null;
        });
    }

    public void loadUri(Uri uri) {
        imgUri = uri.toString();
        loadAvatar(uri.toString());
    }

    private void setupBottomSheetCamera() {
        if (dismissDialogSheetCamera()) {
            return;
        }

        List<LabelType> cameraTypes = new ArrayList<>();
        cameraTypes.add(new CameraType(getContext().getString(R.string.image_picker_camera), CameraType.OPEN_CAMERA));
        cameraTypes.add(new CameraType(getContext().getString(R.string.image_picker_library), CameraType.OPEN_PHOTOS));

        prepareBottomSheetCamera(cameraTypes);
    }

    private boolean dismissDialogSheetCamera() {
        if (dialogCamera != null && dialogCamera.isShowing()) {
            dialogCamera.dismiss();
            return true;
        }

        return false;
    }

    /**
     * OBSERVABLES
     */
    public Observable<NewGroupEntity> onUpdateGroup() {
        return updateGroup;
    }
}
