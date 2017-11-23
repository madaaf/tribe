package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.NewChatPresenter;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.adapter.NewChatAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.NewChatLayoutManager;
import com.tribe.app.presentation.view.component.chat.ShortcutCompletionView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class GamesMembersView extends LinearLayout
    implements NewChatMVPView, ShortcutMVPView, TokenCompleteTextView.TokenListener<Shortcut> {

  @Inject ScreenUtils screenUtils;

  @Inject TagManager tagManager;

  @Inject NewChatPresenter newChatPresenter;

  @Inject NewChatAdapter newChatAdapter;

  @BindView(R.id.viewShortcutCompletion) ShortcutCompletionView viewShortcutCompletion;

  @BindView(R.id.recyclerViewShortcuts) RecyclerView recyclerViewShortcuts;

  // VARIABLES
  private Unbinder unbinder;
  private NewChatLayoutManager layoutManager;
  private FilteredArrayAdapter<Shortcut> adapter;
  private List<Shortcut> items;
  private Set<String> selectedIds;
  private int count = 0;
  private Game game;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onFinish = PublishSubject.create();
  private PublishSubject<Boolean> onHasMembers = PublishSubject.create();

  public GamesMembersView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    initDependencyInjector();
    init();
    initPresenter();
    initUI();
    initRecyclerView();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    newChatPresenter.onViewAttached(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void init() {
    items = new ArrayList<>();
    selectedIds = new HashSet<>();
  }

  private void initPresenter() {
    newChatPresenter.onViewAttached(this);
    newChatPresenter.loadSingleShortcuts();
  }

  private void initUI() {
    setOrientation(VERTICAL);

    adapter = new FilteredArrayAdapter<Shortcut>(getContext(), R.layout.item_shortcut, items) {
      @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
          convertView = new View(getContext());
        }

        return convertView;
      }

      @Override protected boolean keepObject(Shortcut shortcut, String mask) {
        mask = mask.toLowerCase();
        return !selectedIds.contains(shortcut.getSingleFriend().getId()) &&
            (StringUtils.isEmpty(mask) || shortcut.getDisplayName().toLowerCase().startsWith(mask));
      }
    };

    viewShortcutCompletion.setAdapter(adapter);

    viewShortcutCompletion.setDropDownWidth(screenUtils.getWidthPx());
    viewShortcutCompletion.setTokenListener(this);
    viewShortcutCompletion.allowCollapse(false);
    viewShortcutCompletion.allowDuplicates(false);
    ViewCompat.setElevation(viewShortcutCompletion, 0);
    viewShortcutCompletion.setDropDownVerticalOffset(screenUtils.dpToPx(17.5f));
    viewShortcutCompletion.setDropDownHeight(0);
    viewShortcutCompletion.setDropDownBackgroundDrawable(null);
    viewShortcutCompletion.setThreshold(0);
    viewShortcutCompletion.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
    viewShortcutCompletion.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (StringUtils.isEmpty(charSequence.toString())) filter(charSequence.toString());
      }

      @Override public void afterTextChanged(Editable editable) {

      }
    });
  }

  private void initRecyclerView() {
    layoutManager = new NewChatLayoutManager(getContext());
    recyclerViewShortcuts.setLayoutManager(layoutManager);
    recyclerViewShortcuts.setItemAnimator(null);
    recyclerViewShortcuts.addItemDecoration(
        new DividerHeadersItemDecoration(screenUtils.dpToPx(10), screenUtils.dpToPx(10)));
    recyclerViewShortcuts.setAdapter(newChatAdapter);

    newChatAdapter.setItems(items);

    subscriptions.add(newChatAdapter.onClick()
        .map(view -> newChatAdapter.getItemAtPosition(
            recyclerViewShortcuts.getChildLayoutPosition(view)))
        .subscribe(shortcut -> {
          shortcut.setSelected(!shortcut.isSelected());
          newChatAdapter.update(shortcut);

          if (shortcut.isSelected()) {
            selectedIds.add(shortcut.getSingleFriend().getId());
            viewShortcutCompletion.addObject(shortcut, shortcut.getDisplayName());
          } else {
            selectedIds.remove(shortcut.getSingleFriend().getId());
            viewShortcutCompletion.removeObject(shortcut);
          }

          refactorAction();
        }));

    subscriptions.add(viewShortcutCompletion.onFiltering().subscribe(s -> filter(s)));
  }

  private void filter(String text) {
    if (StringUtils.isEmpty(text)) newChatAdapter.setItems(items);

    List<Shortcut> temp = new ArrayList();
    for (Shortcut shortcut : items) {
      if (shortcut.getDisplayName().toLowerCase().startsWith(text)) {
        temp.add(shortcut);
      }
    }

    newChatAdapter.setItems(temp);
  }

  private void refactorAction() {
    onHasMembers.onNext(count > 0);
  }

  public void close() {
    if (selectedIds.size() == 0) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.CANCEL);
      bundle.putInt(TagManagerUtils.MEMBERS, selectedIds.size());
      tagManager.trackEvent(TagManagerUtils.NewChat, bundle);
    }

    onFinish.onNext(null);
  }

  public void create() {
    if (selectedIds.size() > 0) {
      newChatPresenter.createShortcut(selectedIds.toArray(new String[selectedIds.size()]));
    } else {
      Toast.makeText(getContext().getApplicationContext(),
          R.string.newchat_create_error_noone_selected, Toast.LENGTH_LONG).show();
    }
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.SAVE);
    bundle.putInt(TagManagerUtils.MEMBERS, selectedIds.size());
    tagManager.trackEvent(TagManagerUtils.NewChat, bundle);
    onFinish.onNext(null);
  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {
    this.items.clear();
    this.items.addAll(singleShortcutList);
    newChatAdapter.setItems(items);
    newChatAdapter.notifyDataSetChanged();
  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  @Override public void onTokenAdded(Shortcut token) {
    if (token == null || token.getSingleFriend() == null) return;
    count++;
    selectedIds.add(token.getSingleFriend().getId());
    token.setSelected(true);
    newChatAdapter.update(token);
    refactorAction();
  }

  @Override public void onTokenRemoved(Shortcut token) {
    count--;
    selectedIds.remove(token.getSingleFriend().getId());
    token.setSelected(false);
    newChatAdapter.update(token);
    refactorAction();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onFinish() {
    return onFinish;
  }

  public Observable<Boolean> onHasMembers() {
    return onHasMembers;
  }
}
