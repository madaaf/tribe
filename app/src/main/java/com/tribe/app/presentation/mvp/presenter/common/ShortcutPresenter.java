package com.tribe.app.presentation.mvp.presenter.common;

import android.util.Pair;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CreateShortcut;
import com.tribe.app.domain.interactor.user.GetDiskBlockedHiddenSingleShortcuts;
import com.tribe.app.domain.interactor.user.GetDiskSingleShortcut;
import com.tribe.app.domain.interactor.user.RemoveShortcut;
import com.tribe.app.domain.interactor.user.UpdateShortcut;
import com.tribe.app.presentation.mvp.presenter.Presenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class ShortcutPresenter implements Presenter {

  // VIEW ATTACHED
  private ShortcutMVPView shortcutView;

  // USECASES
  private GetDiskSingleShortcut getDiskSingleShortcut;
  private GetDiskBlockedHiddenSingleShortcuts getDiskBlockedShortcuts;
  private CreateShortcut createShortcut;
  private UpdateShortcut updateShortcut;
  private RemoveShortcut removeShortcut;

  // SUBSCRIBERS
  private CreateShortcutSubscriber createShortcutSubscriber;
  private UpdateShortcutSubscriber updateShortcutSubscriber;
  private RemoveShortcutSubscriber removeShortcutSubscriber;
  private SingleShortcutsSubscriber singleShortcutsSubscriber;
  private BlockedSingleShortcutsSubscriber blockedSingleShortcutsSubscriber;

  @Inject public ShortcutPresenter(CreateShortcut createShortcut, UpdateShortcut updateShortcut,
      RemoveShortcut removeShortcut, GetDiskSingleShortcut getDiskSingleShortcut,
      GetDiskBlockedHiddenSingleShortcuts getDiskBlockedShortcuts) {
    this.createShortcut = createShortcut;
    this.updateShortcut = updateShortcut;
    this.removeShortcut = removeShortcut;
    this.getDiskSingleShortcut = getDiskSingleShortcut;
    this.getDiskBlockedShortcuts = getDiskBlockedShortcuts;
  }

  @Override public void onViewDetached() {
    createShortcut.unsubscribe();
    updateShortcut.unsubscribe();
    removeShortcut.unsubscribe();
    getDiskSingleShortcut.unsubscribe();
    getDiskBlockedShortcuts.unsubscribe();
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
      shortcutView.onShortcutCreatedError();
    }

    @Override public void onNext(Shortcut shortcut) {
      shortcutView.onShortcutCreatedSuccess(shortcut);
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

  public void loadBlockedSingleShortcuts() {
    if (blockedSingleShortcutsSubscriber != null) blockedSingleShortcutsSubscriber.unsubscribe();
    blockedSingleShortcutsSubscriber = new BlockedSingleShortcutsSubscriber();
    getDiskBlockedShortcuts.execute(blockedSingleShortcutsSubscriber);
  }

  private class BlockedSingleShortcutsSubscriber extends DefaultSubscriber<List<Shortcut>> {

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
    if (blockedSingleShortcutsSubscriber != null) blockedSingleShortcutsSubscriber.unsubscribe();
  }
}
