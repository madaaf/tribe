package com.tribe.app.presentation.mvp.presenter.common;

import android.util.Pair;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CreateShortcut;
import com.tribe.app.domain.interactor.user.GetCloudBlockedHiddenShortcuts;
import com.tribe.app.domain.interactor.user.GetDiskBlockedHiddenShortcuts;
import com.tribe.app.domain.interactor.user.GetDiskSingleShortcut;
import com.tribe.app.domain.interactor.user.GetShortcutForUserIds;
import com.tribe.app.domain.interactor.user.RemoveShortcut;
import com.tribe.app.domain.interactor.user.UpdateShortcut;
import com.tribe.app.presentation.mvp.presenter.Presenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class ShortcutPresenter implements Presenter {

  // VIEW ATTACHED
  private ShortcutMVPView shortcutView;

  // USECASES
  private GetDiskSingleShortcut getDiskSingleShortcut;
  private GetDiskBlockedHiddenShortcuts getDiskBlockedShortcuts;
  private GetCloudBlockedHiddenShortcuts getCloudBlockedShortcuts;
  private GetShortcutForUserIds getShortcutForUserIds;
  private CreateShortcut createShortcut;
  private UpdateShortcut updateShortcut;
  private RemoveShortcut removeShortcut;

  // SUBSCRIBERS
  private CreateShortcutSubscriber createShortcutSubscriber;
  private UpdateShortcutSubscriber updateShortcutSubscriber;
  private RemoveShortcutSubscriber removeShortcutSubscriber;
  private SingleShortcutsSubscriber singleShortcutsSubscriber;
  private BlockedShortcutsSubscriber blockedShortcutsSubscriber;
  private ShortcutForUserIdsSubscriber shortcutForUserIdsSubscriber;

  @Inject public ShortcutPresenter(CreateShortcut createShortcut, UpdateShortcut updateShortcut,
      RemoveShortcut removeShortcut, GetDiskSingleShortcut getDiskSingleShortcut,
      GetDiskBlockedHiddenShortcuts getDiskBlockedShortcuts,
      GetCloudBlockedHiddenShortcuts getCloudBlockedShortcuts,
      GetShortcutForUserIds getShortcutForUserIds) {
    this.getCloudBlockedShortcuts = getCloudBlockedShortcuts;
    this.createShortcut = createShortcut;
    this.updateShortcut = updateShortcut;
    this.removeShortcut = removeShortcut;
    this.getDiskSingleShortcut = getDiskSingleShortcut;
    this.getDiskBlockedShortcuts = getDiskBlockedShortcuts;
    this.getShortcutForUserIds = getShortcutForUserIds;
  }

  @Override public void onViewDetached() {
    createShortcut.unsubscribe();
    updateShortcut.unsubscribe();
    removeShortcut.unsubscribe();
    getDiskSingleShortcut.unsubscribe();
    getDiskBlockedShortcuts.unsubscribe();
    getShortcutForUserIds.unsubscribe();
    shortcutView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    shortcutView = (ShortcutMVPView) v;
  }

  public void createShortcut(String... userIds) {
    if (createShortcutSubscriber != null) createShortcutSubscriber.unsubscribe();
    createShortcutSubscriber = new CreateShortcutSubscriber();
    createShortcut.setup(userIds);
    createShortcut.execute(createShortcutSubscriber);
  }

  private class CreateShortcutSubscriber extends DefaultSubscriber<Shortcut> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      if (shortcutView != null) shortcutView.onShortcutCreatedError();
    }

    @Override public void onNext(Shortcut shortcut) {
      if (shortcutView != null) shortcutView.onShortcutCreatedSuccess(shortcut);
    }
  }

  public void muteShortcut(String shortcutId, boolean mute) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.MUTE, String.valueOf(mute)));
    updateShortcut(shortcutId, values);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.STATUS, status));
    updateShortcut(shortcutId, values);
  }

  public void updateShortcutName(String shortcutId, String name) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.NAME, name));
    updateShortcut(shortcutId, values);
  }

  public void updateShortcutPicture(String shortcutId, String imageUri) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.PICTURE, imageUri));
    updateShortcut(shortcutId, values);
  }

  public void readShortcut(String shortcutId) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.READ, String.valueOf(true)));
    updateShortcut(shortcutId, values);
  }

  public void pinShortcut(String shortcutId, boolean pinned) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.PINNED, String.valueOf(pinned)));
    updateShortcut(shortcutId, values);
  }

  public void leaveOnline(String shortcutId, long leaveOnlineDate) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.LEAVE_ONLINE_UNTIL, String.valueOf(leaveOnlineDate)));
    updateShortcut(shortcutId, values);
  }

  private void updateShortcut(String shortcutId, List<Pair<String, String>> values) {
    if (updateShortcutSubscriber != null) updateShortcutSubscriber.unsubscribe();
    updateShortcutSubscriber = new UpdateShortcutSubscriber();
    updateShortcut.setup(shortcutId, values);
    updateShortcut.execute(updateShortcutSubscriber);
  }

  private class UpdateShortcutSubscriber extends DefaultSubscriber<Shortcut> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      shortcutView.onShortcutUpdatedError();
    }

    @Override public void onNext(Shortcut shortcut) {
      shortcutView.onShortcutUpdatedSuccess(shortcut);
    }
  }

  public void removeShortcut(String shortcutId) {
    if (removeShortcutSubscriber != null) removeShortcutSubscriber.unsubscribe();
    removeShortcutSubscriber = new RemoveShortcutSubscriber();
    removeShortcut.setup(shortcutId);
    removeShortcut.execute(removeShortcutSubscriber);
  }

  private class RemoveShortcutSubscriber extends DefaultSubscriber<Shortcut> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      shortcutView.onShortcutRemovedError();
    }

    @Override public void onNext(Shortcut shortcut) {
      shortcutView.onShortcutRemovedSuccess();
    }
  }

  public void loadSingleShortcuts() {
    if (singleShortcutsSubscriber != null) singleShortcutsSubscriber.unsubscribe();
    singleShortcutsSubscriber = new SingleShortcutsSubscriber();
    getDiskSingleShortcut.execute(singleShortcutsSubscriber);
  }

  private class SingleShortcutsSubscriber extends DefaultSubscriber<List<Shortcut>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Shortcut> shortcutList) {
      shortcutView.onSingleShortcutsLoaded(shortcutList);
    }
  }

  public void loadBlockedShortcuts() {
    getCloudBlockedShortcuts.execute(new DefaultSubscriber());

    if (blockedShortcutsSubscriber != null) blockedShortcutsSubscriber.unsubscribe();
    blockedShortcutsSubscriber = new BlockedShortcutsSubscriber();
    getDiskBlockedShortcuts.execute(blockedShortcutsSubscriber);
  }

  private class BlockedShortcutsSubscriber extends DefaultSubscriber<List<Shortcut>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Shortcut> shortcutList) {
      shortcutView.onSingleShortcutsLoaded(shortcutList);
    }
  }

  public void unsubscribeLoadShortcuts() {
    if (singleShortcutsSubscriber != null) singleShortcutsSubscriber.unsubscribe();
    if (blockedShortcutsSubscriber != null) blockedShortcutsSubscriber.unsubscribe();
  }

  public void shortcutForUserIds(String... userIds) {
    if (shortcutForUserIdsSubscriber != null) shortcutForUserIdsSubscriber.unsubscribe();
    shortcutForUserIdsSubscriber = new ShortcutForUserIdsSubscriber();
    getShortcutForUserIds.setup(userIds);
    getShortcutForUserIds.execute(shortcutForUserIdsSubscriber);
  }

  private class ShortcutForUserIdsSubscriber extends DefaultSubscriber<Shortcut> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Shortcut shortcut) {
      shortcutView.onShortcut(shortcut);
    }
  }
}
