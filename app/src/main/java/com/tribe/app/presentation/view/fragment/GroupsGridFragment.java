package com.tribe.app.presentation.view.fragment;

import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.CameraType;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.component.PrivatePublicView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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

    @BindView(R.id.imageGroup)
    ImageView imageGroup;
    @BindView(R.id.imageEditGroup)
    ImageView imageEditGroup;
    @BindView(R.id.imagePrivacyStatus)
    ImageView imagePrivacyStatus;
    @BindView(R.id.imageInvite)
    ImageView imageInvite;

    @BindView(R.id.textPrivacyStatus)
    TextViewFont textPrivacyStatus;
    @BindView(R.id.textCreateInvite)
    TextViewFont textCreateInvite;
    @BindView(R.id.textCreateInviteDesc)
    TextViewFont textCreateInviteDesc;

    @BindView(R.id.editTextGroupName)
    EditTextFont editTextGroupName;

    @BindView(R.id.viewCreateGroupBg1)
    View viewCreateGroupBg1;
    @BindView(R.id.viewCreateGroupBg2)
    View viewCreateGroupBg2;
    @BindView(R.id.viewDividerBackground)
    View viewDividerBackground;


    @BindView(R.id.layoutPrivacyStatus)
    LinearLayout layoutPrivacyStatus;
    @BindView(R.id.layoutCreateInvite)
    FrameLayout layoutCreateInvite;

    @BindView(R.id.privatePublicView)
    PrivatePublicView privatePublicView;

    @Inject
    Navigator navigator;

    @Inject
    ScreenUtils screenUtils;

    // VARIABLES
    private BottomSheetDialog dialogCamera;
    private RecyclerView recyclerViewCameraType;
    private LabelSheetAdapter cameraTypeAdapter;
    private int animDuration = 300 * 2;
    private int loadingAnimDuration = 1000 * 2;
    private boolean privateGroup = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_groups_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();
        initUi();
        fragmentView.setTag(HomeActivity.GROUPS_FRAGMENT_PAGE);
        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initUi() {
        int startTranslation = -200;
        imageEditGroup.setTranslationY(startTranslation);
        imageInvite.setScaleX(0);
        imageInvite.setScaleY(0);

        subscriptions.add(RxView.clicks(imageGroup).subscribe(aVoid -> {
            setupBottomSheetCamera();
        }));

        subscriptions.add(RxTextView.textChanges(editTextGroupName).subscribe(charSequence -> {
            if (editTextGroupName.getText().toString().length() > 0) {
                viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
                viewCreateGroupBg2.setEnabled(true);
            }
            else {
                viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_disabled));
                viewCreateGroupBg2.setEnabled(false);
            }
        }));
        viewCreateGroupBg2.setEnabled(false);
        subscriptions.add(RxView.clicks(viewCreateGroupBg2).subscribe(aVoid -> {
            createGroupLoadingAnim();

            Observable.timer(loadingAnimDuration, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                       animSet1();
                    });

            Observable.timer(loadingAnimDuration + animDuration, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                       animSet2();
                    });

            Observable.timer(loadingAnimDuration + animDuration*2, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        AnimationUtils.scaleIn(imageInvite, animDuration);
                    });
        }));
    }

    private void animSet1() {
        viewDividerBackground.setVisibility(View.INVISIBLE);
        AnimationUtils.collapseScale(privatePublicView, animDuration);
        privatePublicView.animate()
                .setStartDelay(0)
                .alpha(0f)
                .setDuration(animDuration)
                .start();

        if (privateGroup) {
            textPrivacyStatus.setText(getString(R.string.group_private_title));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_grey));
        } else {
            textPrivacyStatus.setText(getString(R.string.group_public_title));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_grey));
        }
        layoutPrivacyStatus.animate()
                .setDuration(animDuration)
                .alpha(1)
                .setStartDelay(0)
                .translationY(-screenUtils.dpToPx(48))
                .start();

        bringInIcon(imageEditGroup);

        imageGroup.animate()
                .scaleY(.7f)
                .scaleX(.7f)
                .setDuration(animDuration)
                .start();
        editTextGroupName.bringToFront();
        editTextGroupName.setKeyListener(null);
        editTextGroupName.setCursorVisible(false);
        editTextGroupName.animate()
                .translationY(-screenUtils.dpToPx(60))
                .setDuration(animDuration)
                .start();

        textCreateInvite.setText(getString(R.string.group_button_share));
        textCreateInviteDesc.setText(getString(R.string.group_share_description));


    }

    private void animSet2() {
        layoutCreateInvite.animate()
                .translationY(-screenUtils.dpToPx(138))
                .setDuration(animDuration)
                .start();

    }

    private void bringInIcon(ImageView imageView) {
        imageView.animate()
                .translationY(0)
                .setDuration(animDuration)
                .start();
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

    private void prepareBottomSheetCamera(List<LabelType> items) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_camera_type, null);
        recyclerViewCameraType = (RecyclerView) view.findViewById(R.id.recyclerViewCameraType);
        recyclerViewCameraType.setHasFixedSize(true);
        recyclerViewCameraType.setLayoutManager(new LinearLayoutManager(getActivity()));
        cameraTypeAdapter = new LabelSheetAdapter(getContext(), items);
        cameraTypeAdapter.setHasStableIds(true);
        recyclerViewCameraType.setAdapter(cameraTypeAdapter);
        subscriptions.add(cameraTypeAdapter.clickLabelItem()
        .map(labelView -> cameraTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
        .subscribe(labelType -> {
            CameraType cameraType = (CameraType) labelType;
            if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_CAMERA)) {
                navigator.getImageFromCamera(getActivity(), HomeActivity.OPEN_CAMERA_RESULT);
            }
            if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_PHOTOS)) {
                navigator.getImageFromCameraRoll(getActivity(), HomeActivity.OPEN_GALLERY_RESULT);
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

    private void setupBottomSheetCamera() {
        if (dismissDialogSheetCamera()) {
            return;
        }

        List<LabelType> cameraTypes = new ArrayList<>();
        cameraTypes.add(new CameraType(getString(R.string.image_picker_camera), CameraType.OPEN_CAMERA));
        cameraTypes.add(new CameraType(getString(R.string.image_picker_library), CameraType.OPEN_PHOTOS));

        prepareBottomSheetCamera(cameraTypes);

    }

    private boolean dismissDialogSheetCamera() {
        if (dialogCamera != null && dialogCamera.isShowing()) {
            dialogCamera.dismiss();
            return true;
        }

        return false;
    }

    private void createGroupLoadingAnim() {
        Rect rect = new Rect();
        viewCreateGroupBg2.getLocalVisibleRect(rect);
        Rect from = new Rect(rect);
        Rect to = new Rect(rect);
        from.right = 0;
        viewCreateGroupBg2.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofObject(viewCreateGroupBg2,
                "clipBounds",
                new RectEvaluator(),
                from, to);
        anim.setDuration(loadingAnimDuration);
        anim.start();

        Observable.timer(loadingAnimDuration, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    viewCreateGroupBg2.setVisibility(View.INVISIBLE);
                });

    }

    public void setGroupPicture(Bitmap bitmap) {
        imageGroup.setImageBitmap(formatBitmapforView(bitmap));
    }

    public Bitmap formatBitmapforView(Bitmap thumbnail) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(imageGroup.getWidth() >> 1, 0, RoundedCornersTransformation.CornerType.ALL);
        thumbnail = ImageUtils.centerCropBitmap(thumbnail);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, imageGroup.getWidth(), imageGroup.getWidth(), false);
        thumbnail = roundedCornersTransformation.transform(thumbnail);
        return thumbnail;
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

}
