package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.fragment.SettingBlockFragment;
import com.tribe.app.presentation.view.fragment.SettingFragment;
import com.tribe.app.presentation.view.fragment.SettingUpdateProfileFragment;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * SettingActivity.java
 * Created by horatiothomas on 8/26/16.
 */
public class SettingActivity extends BaseActivity implements SettingView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @BindView(R.id.imgBack)
    ImageView imgBack;

    @BindView(R.id.imgDone)
    ImageView imgDone;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    FragmentManager fragmentManager;
    private SettingFragment settingFragment;
    private SettingUpdateProfileFragment settingUpdateProfileFragment;
    private SettingBlockFragment settingBlockFragment;

    private int shortDuration = 150;

    @Inject
    SettingPresenter settingPresenter;

    @Inject
    ScreenUtils screenUtils;

    private static final int CAMERA_REQUEST = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        initDependencyInjector();
        initPresenter();
    }

    @Override
    protected void onPause() {
        txtTitle.animate().setListener(null).start();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            subscriptions.add(
                Observable.just((Bitmap) data.getExtras().get("data"))
                        .map(bitmap -> ImageUtils.formatForUpload(bitmap))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            settingUpdateProfileFragment.setImgProfilePic(bitmap, Uri.fromFile(FileUtils.bitmapToFile(bitmap, this)).toString());
                        })
            );
        }

        // 2. Get image from Gallery
        if (requestCode == ProfileInfoView.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            subscriptions.add(
                    Observable.just(data.getData())
                            .map(uri -> {
                                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                cursor.close();

                                return ImageUtils.formatForUpload(ImageUtils.loadFromPath(picturePath));
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(bitmap -> {
                                settingUpdateProfileFragment.setImgProfilePic(bitmap, Uri.fromFile(FileUtils.bitmapToFile(bitmap, this)).toString());

                            })
            );
        }
    }


    private void initUi() {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);

        settingFragment = SettingFragment.newInstance();
        settingUpdateProfileFragment = SettingUpdateProfileFragment.newInstance();
        settingBlockFragment = SettingBlockFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layoutFragmentContainer, settingFragment);
        fragmentTransaction.commit();

        subscriptions.add(RxView.clicks(imgBack).subscribe(aVoid -> {
            goToMain();
        }));

        subscriptions.add(RxView.clicks(imgDone).subscribe(aVoid -> {

            if (fragmentManager.findFragmentById(R.id.layoutFragmentContainer) instanceof SettingFragment) {
                Intent resultIntent = new Intent();
                setResult(BaseActivity.RESULT_OK, resultIntent);
                finish();
            }
            if (fragmentManager.findFragmentById(R.id.layoutFragmentContainer) instanceof SettingUpdateProfileFragment) {
                settingPresenter.updateUser(
                        settingUpdateProfileFragment.getUsername(),
                        settingUpdateProfileFragment.getDisplayName(),
                        settingUpdateProfileFragment.getImgUri()
                );

                goToMain();
            }
        }));
    }

    private void updateAnim() {
        imgBack.animate()
                .alpha(1)
                .setStartDelay(0)
                .start();
    }

    public void setImgDoneEnabled(boolean isEnabled) {
        if (isEnabled) {
            imgDone.setClickable(true);
            imgDone.animate()
                    .alpha(1)
                    .setStartDelay(0)
                    .start();
        } else {
            imgDone.setClickable(false);
            imgDone.animate()
                    .alpha(.4f)
                    .setStartDelay(0)
                    .start();
        }
    }

    private void mainSettingAnim() {
        imgBack.animate()
                .alpha(0)
                .setStartDelay(0)
                .start();
    }

    public void goToMain() {
        screenUtils.hideKeyboard(this);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_left, R.anim.fragment_out_from_right);
        fragmentTransaction.replace(R.id.layoutFragmentContainer, settingFragment);
        fragmentTransaction.commit();
        txtTitle.setText(getString(R.string.settings_title));
        imgDone.setAlpha(1f);
        mainSettingAnim();
    }

    public void goToUpdateProfile() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_right, R.anim.fragment_out_from_left);
        fragmentTransaction.add(R.id.layoutFragmentContainer, settingUpdateProfileFragment);
        fragmentTransaction.addToBackStack("Update");
        fragmentTransaction.commit();
        txtTitle.setText(getString(R.string.settings_profile_title));
        updateAnim();
    }

    public void goToBlock() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_right, R.anim.fragment_out_from_left);
        fragmentTransaction.add(R.id.layoutFragmentContainer, settingBlockFragment);
        fragmentTransaction.addToBackStack("Block");
        fragmentTransaction.commit();
        txtTitle.setText(getString(R.string.hiddenblocked_empty_title));
        imgDone.setAlpha(0f);
        updateAnim();
    }


    @Override
    public void updateUser(String username, String displayName, String pictureUri) {

    }

    @Override
    public void goToLauncher() {
        navigator.navigateToLauncher(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return null;
    }


    @Override
    public void setProfilePic(String profilePicUrl) {
        settingFragment.setPicture(profilePicUrl);
    }

    private void initPresenter() {
        settingPresenter.attachView(this);
    }

    /**
     * Dagger Setup
     */

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }
}
