package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ViewStackHelper;

import java.io.Serializable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class SettingsGroupView extends FrameLayout {

  @Inject User currentUser;

  @BindView(R.id.viewActionInfos) ActionView viewActionInfos;

  @BindView(R.id.viewActionNotifications) ActionView viewActionNotifications;

  @BindView(R.id.viewActionLeaveGroup) ActionView viewActionLeaveGroup;

  // VARIABLES
  private Membership membership;
  private boolean isCurrentUserAdmin = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Void> editGroup = PublishSubject.create();
  private PublishSubject<Boolean> notificationsChange = PublishSubject.create();
  private PublishSubject<Void> leaveGroup = PublishSubject.create();

  public SettingsGroupView(Context context, AttributeSet attrs) {
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
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    subscriptions = new CompositeSubscription();

    updateInfos();

    subscriptions.add(viewActionInfos.onClick().doOnNext(aVoid -> {
      if (!isCurrentUserAdmin) {
        Toast.makeText(getContext(), EmojiParser.demojizedText(
            getContext().getString(R.string.group_infos_error_edit_not_admin)), Toast.LENGTH_SHORT)
            .show();
      }
    }).filter(aVoid -> isCurrentUserAdmin).subscribe(aVoid -> editGroup.onNext(null)));

    subscriptions.add(viewActionLeaveGroup.onClick()
        .flatMap(x -> DialogFactory.dialog(getContext(), membership.getDisplayName(), null,
            getContext().getString(R.string.group_details_leave_title),
            getContext().getString(R.string.action_cancel)))
        .filter(x -> x == true)
        .subscribe(aVoid -> leaveGroup.onNext(null)));

    subscriptions.add(viewActionNotifications.onChecked().subscribe(notificationsChange));
  }

  public void updateGroup(Group group, boolean full) {
    if (full) {
      membership.setGroup(group);
    } else {
      membership.getGroup().setPicture(group.getPicture());
      membership.getGroup().setName(group.getName());
    }

    updateInfos();
  }

  private void updateInfos() {
    viewActionInfos.setTitle(membership.getDisplayName());
    viewActionInfos.setRecipient(membership);
    viewActionNotifications.setValue(!membership.isMute());
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onEditGroup() {
    return editGroup;
  }

  public Observable<Void> onLeaveGroup() {
    return leaveGroup;
  }

  public Observable<Boolean> onNotificationsChange() {
    return notificationsChange;
  }
}
