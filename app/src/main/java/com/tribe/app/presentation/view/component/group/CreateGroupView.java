package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.CameraType;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.NewGroupEntity;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

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
 * Created by tiago on 11/19/2016.
 */

public class CreateGroupView extends FrameLayout {

    private int DURATION_FADE = 150;

    @Inject
    RxImagePicker rxImagePicker;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.editGroupName)
    EditTextFont editGroupName;

    @BindView(R.id.btnGo)
    View btnGo;

    // VARIABLES
    private String imgUri;
    private BottomSheetDialog dialogCamera;
    private LabelSheetAdapter cameraTypeAdapter;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private PublishSubject<NewGroupEntity> createNewGroup = PublishSubject.create();

    public CreateGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    @OnClick(R.id.imgAvatar)
    void clickAvatar() {
        setupBottomSheetCamera();
    }

    @OnClick(R.id.btnGo)
    void clickGo() {
        createNewGroup.onNext(new NewGroupEntity(editGroupName.getText().toString(), imgUri));
    }

    @OnClick({ R.id.viewSuggestionBFF, R.id.viewSuggestionTeam, R.id.viewSuggestionClass, R.id.viewSuggestionRoomies,
            R.id.viewSuggestionWork, R.id.viewSuggestionFamily })
    void clickSuggestion(View v) {
        GroupSuggestionView groupSuggestionView = (GroupSuggestionView) v;
        NewGroupEntity newGroupEntity = new NewGroupEntity(groupSuggestionView.getLabel(), FileUtils.getUriToDrawable(getContext(), groupSuggestionView.getDrawableId()).toString());
        createNewGroup.onNext(newGroupEntity);
    }

    private void init() {
        btnGo.setEnabled(false);

        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        subscriptions = new CompositeSubscription();

        subscriptions.add(
                RxTextView.textChanges(editGroupName)
                        .map(CharSequence::toString)
                        .subscribe(s -> {
                            if (s.length() >= 2 && btnGo.getAlpha() == 0.5) {
                                AnimationUtils.fadeIn(btnGo, DURATION_FADE);
                                btnGo.setEnabled(true);
                            } else if (s.length() < 2 && btnGo.getAlpha() == 1) {
                                AnimationUtils.fadeOutIntermediate(btnGo, DURATION_FADE);
                                btnGo.setEnabled(false);
                            }
                        })
        );
    }

    private void loadAvatar(String url) {
        Glide.with(getContext()).load(url)
                .override(imgAvatar.getWidth(), imgAvatar.getHeight())
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
    public Observable<NewGroupEntity> onCreateNewGroup() {
        return createNewGroup;
    }
}
