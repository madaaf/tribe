package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.EditTextFont;

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

  @Inject TagManager tagManager;

  @Inject RxImagePicker rxImagePicker;

  @BindView(R.id.imgAvatar) ImageView imgAvatar;

  @BindView(R.id.editGroupName) EditTextFont editGroupName;

  @BindView(R.id.btnGo) View btnGo;

  // VARIABLES
  private String imgUri;
  private BottomSheetDialog dialogCamera;
  private LabelSheetAdapter cameraTypeAdapter;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<GroupEntity> createNewGroup = PublishSubject.create();

  public CreateGroupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (subscriptions == null) init();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  @OnClick(R.id.imgAvatar) void clickAvatar() {
    subscriptions.add(DialogFactory.showBottomSheetForCamera(getContext()).subscribe(labelType -> {
      if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
        subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
          loadUri(uri);
        }));
      } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
        subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
          loadUri(uri);
        }));
      }
    }));
  }

  @OnClick(R.id.btnGo) void clickGo() {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerConstants.TEMPLATE, TagManagerConstants.TEMPLATE_CUSTOM);
    tagManager.trackEvent(TagManagerConstants.KPI_GROUP_TEMPLATE_SELECTED, bundle);

    GroupEntity groupEntity = new GroupEntity(editGroupName.getText().toString(), imgUri);
    groupEntity.setCustom(true);
    createNewGroup.onNext(groupEntity);
  }

  @OnClick({
      R.id.viewSuggestionBFF, R.id.viewSuggestionTeam, R.id.viewSuggestionClass,
      R.id.viewSuggestionRoomies, R.id.viewSuggestionWork, R.id.viewSuggestionFamily
  }) void clickSuggestion(View v) {
    GroupSuggestionView groupSuggestionView = (GroupSuggestionView) v;

    Bundle bundle = new Bundle();
    bundle.putString(TagManagerConstants.TEMPLATE, groupSuggestionView.getLabel());
    tagManager.trackEvent(TagManagerConstants.KPI_GROUP_TEMPLATE_SELECTED, bundle);

    GroupEntity groupEntity = new GroupEntity(groupSuggestionView.getLabel(),
        FileUtils.getUriToDrawable(getContext(), groupSuggestionView.getDrawableId()).toString());
    createNewGroup.onNext(groupEntity);
  }

  private void init() {
    btnGo.setEnabled(false);

    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    subscriptions = new CompositeSubscription();

    subscriptions.add(
        RxTextView.textChanges(editGroupName).map(CharSequence::toString).subscribe(s -> {
          if (s.length() >= 2 && btnGo.getAlpha() == 0.5) {
            AnimationUtils.fadeIn(btnGo, DURATION_FADE);
            btnGo.setEnabled(true);
          } else if (s.length() < 2 && btnGo.getAlpha() == 1) {
            AnimationUtils.fadeOutIntermediate(btnGo, DURATION_FADE);
            btnGo.setEnabled(false);
          }
        }));
  }

  private void loadAvatar(String url) {
    Glide.with(getContext())
        .load(url)
        .override(imgAvatar.getWidth(), imgAvatar.getHeight())
        .bitmapTransform(new CropCircleTransformation(getContext()))
        .crossFade()
        .into(imgAvatar);
  }

  public void loadUri(Uri uri) {
    imgUri = uri.toString();
    loadAvatar(uri.toString());
  }

  /**
   * OBSERVABLES
   */
  public Observable<GroupEntity> onCreateNewGroup() {
    return createNewGroup;
  }
}
