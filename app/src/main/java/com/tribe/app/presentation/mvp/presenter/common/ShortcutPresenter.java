package com.tribe.app.presentation.mvp.presenter.common;

import android.util.Pair;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CreateShortcut;
import com.tribe.app.domain.interactor.user.RemoveShortcut;
import com.tribe.app.domain.interactor.user.UpdateShortcut;
import com.tribe.app.presentation.mvp.presenter.Presenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ShortcutView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class ShortcutPresenter implements Presenter {

  // VIEW ATTACHED
  private ShortcutView shortcutView;

  // USECASES
  private CreateShortcut createShortcut;
  private UpdateShortcut updateShortcut;
  private RemoveShortcut removeShortcut;

  // SUBSCRIBERS
  private CreateShortcutSubscriber createShortcutSubscriber;
  private UpdateShortcutSubscriber updateShortcutSubscriber;
  private RemoveShortcutSubscriber removeShortcutSubscriber;

  @Inject public ShortcutPresenter(CreateShortcut createShortcut, UpdateShortcut updateShortcut,
      RemoveShortcut removeShortcut) {
    this.createShortcut = createShortcut;
    this.updateShortcut = updateShortcut;
    this.removeShortcut = removeShortcut;
  }

  @Override public void onViewDetached() {
    createShortcut.unsubscribe();
    updateShortcut.unsubscribe();
    removeShortcut.unsubscribe();
  }

  @Override public void onViewAttached(MVPView v) {

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
    }

    @Override public void onNext(Shortcut shortcut) {

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
    }

    @Override public void onNext(Shortcut shortcut) {

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
    }

    @Override public void onNext(Shortcut shortcut) {

    }
  }
}
