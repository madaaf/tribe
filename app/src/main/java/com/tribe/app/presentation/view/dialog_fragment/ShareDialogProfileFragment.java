package com.tribe.app.presentation.view.dialog_fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.DiagonalLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.io.File;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/12/16.
 */
public class ShareDialogProfileFragment extends BaseDialogFragment {

  private static Integer[] colors;

  public static ShareDialogProfileFragment newInstance() {
    Bundle args = new Bundle();
    ShareDialogProfileFragment fragment = new ShareDialogProfileFragment();
    fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.Custom_Dialog);
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.layoutContent) View layoutContent;

  @BindView(R.id.imgInstagram) ImageView imgInstagram;

  @BindView(R.id.imgTwitter) ImageView imgTwitter;

  @BindView(R.id.imgSnapchat) ImageView imgSnapchat;

  @BindView(R.id.imgMore) ImageView imgMore;

  @BindView(R.id.avatar) ImageView avatar;

  @BindView(R.id.txtUsername) TextViewFont txtUsername;

  @BindView(R.id.viewShapeAngle) View viewShapeAngle;

  @BindView(R.id.layoutDiagonal) DiagonalLayout layoutDiagonal;

  @BindView(R.id.layoutBottom) ViewGroup layoutBottom;

  @Inject ScreenUtils screenUtils;

  @Inject Navigator navigator;

  @Inject User currentUser;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View fragmentView =
        inflater.inflate(R.layout.dialog_fragment_share_profile, container, false);
    initDependencyInjector();
    initColors();
    initUi(fragmentView);

    return fragmentView;
  }

  @Override public void onStart() {
    super.onStart();

    if (getDialog() == null) return;

    int dialogWidth = screenUtils.getWidthPx();
    int oneFourth = dialogWidth / 4 - screenUtils.dpToPx(15);
    int dialogHeight = dialogWidth + oneFourth;

    ViewGroup.LayoutParams params = layoutBottom.getLayoutParams();
    params.height = oneFourth;
    layoutBottom.setLayoutParams(params);

    ViewGroup.LayoutParams paramsDiagonal = layoutDiagonal.getLayoutParams();
    paramsDiagonal.height = dialogWidth >> 1;
    layoutDiagonal.setLayoutParams(paramsDiagonal);

    int avatarWidth = (int) (dialogWidth / 2.5f);
    ViewGroup.LayoutParams paramsAvatar = avatar.getLayoutParams();
    paramsAvatar.height = avatarWidth;
    paramsAvatar.width = avatarWidth;
    avatar.setLayoutParams(paramsAvatar);

    getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
  }

  @Override public void removeSubscriptions() {
    super.removeSubscriptions();

    if (subscriptions.hasSubscriptions() && subscriptions != null) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }
  }

  private void initColors() {
    colors = new Integer[] {
        ContextCompat.getColor(getContext(), R.color.tribe_profile_1),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_2),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_3),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_4),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_5),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_6),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_7),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_8),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_9),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_10),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_11),
        ContextCompat.getColor(getContext(), R.color.tribe_profile_12)
    };
  }

  @Override public void initUi(View view) {
    super.initUi(view);

    layoutContent.setBackgroundColor(getRandColor());
    viewShapeAngle.setBackgroundColor(getRandColor());

    subscriptions.add(RxView.clicks(imgSnapchat).subscribe(aVoid -> {
      prepareShareHandle(Navigator.SNAPCHAT);
    }));

    subscriptions.add(RxView.clicks(imgInstagram).subscribe(aVoid -> {
      prepareShareHandle(Navigator.INSTAGRAM);
    }));

    subscriptions.add(RxView.clicks(imgTwitter).subscribe(aVoid -> {
      prepareShareHandle(Navigator.TWITTER);
    }));

    subscriptions.add(RxView.clicks(imgMore).subscribe(aVoid -> {
      prepareShareHandle("");
    }));

    txtUsername.setText("@" + currentUser.getUsername());

    String url = currentUser.getProfilePicture();
    if (!StringUtils.isEmpty(url) && !url.equals(
        getContext().getString(R.string.no_profile_picture_url))) {
      Glide.with(getContext())
          .load(url)
          .bitmapTransform(new CropCircleTransformation(getContext()))
          .crossFade()
          .into(avatar);
    } else {
      Glide.with(getContext())
          .load(R.drawable.picto_placeholder_avatar)
          .bitmapTransform(new CropCircleTransformation(getContext()))
          .crossFade()
          .into(avatar);
    }
  }

  @OnClick(R.id.layoutContent) void changeBackground() {
    layoutContent.setBackgroundColor(getRandColor());
  }

  @OnClick(R.id.viewShapeAngle) void changeViewBackground() {
    viewShapeAngle.setBackgroundColor(getRandColor());
  }

  private int getRandColor() {
    Random r = new Random();
    return colors[r.nextInt(colors.length - 0) + 0];
  }

  private void prepareShareHandle(String selectedPackage) {
    Observable.just("").map(s -> {
      layoutContent.setDrawingCacheEnabled(true);
      layoutContent.buildDrawingCache();
      Bitmap shareBitmap = layoutContent.getDrawingCache();

      File file =
          FileUtils.bitmapToFilePublic("" + System.currentTimeMillis() + ".jpg", shareBitmap,
              getContext());

      layoutContent.setDrawingCacheEnabled(false);
      return file;
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(file -> {
      navigator.shareHandle(getContext(), currentUser.getUsername(), file, selectedPackage);
    });
  }

  /**
   * Dagger setup
   */

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
