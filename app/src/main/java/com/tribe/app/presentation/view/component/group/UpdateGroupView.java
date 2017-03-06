package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.Serializable;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class UpdateGroupView extends LinearLayout {

  private int DURATION_FADE = 150;

  @Inject ScreenUtils screenUtils;

  @Inject RxImagePicker rxImagePicker;

  @BindView(R.id.avatarView) AvatarView avatarView;

  @BindView(R.id.editGroupName) EditTextFont editGroupName;

  @BindView(R.id.viewActionNotifications) ActionView viewActionNotifications;

  @BindView(R.id.viewActionLeaveGroup) ActionView viewActionLeaveGroup;

  // VARIABLES
  private String imgUri;
  private Membership membership;
  private GroupEntity groupEntity;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Boolean> notificationsChange = PublishSubject.create();
  private PublishSubject<Void> leaveGroup = PublishSubject.create();

  public UpdateGroupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (membership == null) {
      Serializable serializable = ViewStackHelper.getViewStack(getContext()).getParameter(this);

      if (serializable instanceof Membership) {
        membership = (Membership) serializable;
      }

      init();
    }
  }

  @Override protected void onDetachedFromWindow() {
    if (editGroupName != null) screenUtils.hideKeyboard(editGroupName);
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    subscriptions = new CompositeSubscription();

    groupEntity = new GroupEntity();

    subscriptions.add(
        RxTextView.textChanges(editGroupName).map(CharSequence::toString).subscribe(s -> {
          groupEntity.setName(s);
        }));

    viewActionNotifications.setValue(!membership.isMute());
    viewActionNotifications.setTitle(EmojiParser.demojizedText(
        getContext().getString(R.string.group_details_notifications_title)));
    viewActionLeaveGroup.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.group_details_leave_title)));

    subscriptions.add(viewActionLeaveGroup.onClick()
        .flatMap(x -> DialogFactory.dialog(getContext(), membership.getDisplayName(), null,
            EmojiParser.demojizedText(getContext().getString(R.string.group_details_leave_title)),
            getContext().getString(R.string.action_cancel)))
        .filter(x -> x == true)
        .subscribe(aVoid -> leaveGroup.onNext(null)));

    subscriptions.add(viewActionNotifications.onChecked().subscribe(notificationsChange));

    loadAvatar(membership);
    editGroupName.setText(membership.getDisplayName());
  }

  @OnClick(R.id.avatarView) void clickAvatar() {
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

  public Observable<Void> onLeaveGroup() {
    return leaveGroup;
  }

  public Observable<Boolean> onNotificationsChange() {
    return notificationsChange;
  }
}
