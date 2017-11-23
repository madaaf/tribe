package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.NewChatPresenter;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.adapter.NewChatAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.NewChatLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.component.chat.ShortcutCompletionView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

public class NewChatActivity extends BaseActivity
    implements NewChatMVPView, ShortcutMVPView, TokenCompleteTextView.TokenListener<Shortcut> {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, NewChatActivity.class);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject NewChatPresenter newChatPresenter;

  @Inject NewChatAdapter newChatAdapter;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.viewShortcutCompletion) ShortcutCompletionView viewShortcutCompletion;

  @BindView(R.id.recyclerViewShortcuts) RecyclerView recyclerViewShortcuts;

  // VARIABLES
  private Unbinder unbinder;
  private NewChatLayoutManager layoutManager;
  private FilteredArrayAdapter<Shortcut> adapter;
  private List<Shortcut> items;
  private Set<String> selectedIds;
  private int count = 0;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_chat);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init(savedInstanceState);
    initPresenter();
    initUI();
    initRecyclerView();
  }

  @Override protected void onStart() {
    super.onStart();
    newChatPresenter.onViewAttached(this);
  }

  @Override protected void onResume() {
    super.onResume();
    screenUtils.showKeyboard(viewShortcutCompletion, 500);
  }

  @Override protected void onPause() {
    screenUtils.hideKeyboard(viewShortcutCompletion);
    super.onPause();
  }

  @Override protected void onStop() {
    newChatPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {

    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

    super.onDestroy();
  }

  private void init(Bundle savedInstanceState) {
    items = new ArrayList<>();
    selectedIds = new HashSet<>();
  }

  private void initPresenter() {
    newChatPresenter.onViewAttached(this);
    newChatPresenter.loadSingleShortcuts();
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initUI() {
    adapter = new FilteredArrayAdapter<Shortcut>(this, R.layout.item_shortcut, items) {
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
    layoutManager = new NewChatLayoutManager(this);
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
    txtAction.setEnabled(count > 0);
  }

  @OnClick(R.id.btnClose) void close() {
    finish();
  }

  @OnClick(R.id.txtAction) void create() {
    if (selectedIds.size() > 0) {
      newChatPresenter.createShortcut(selectedIds.toArray(new String[selectedIds.size()]));
    } else {
      Toast.makeText(this, R.string.newchat_create_error_noone_selected, Toast.LENGTH_LONG).show();
    }
  }

  @Override public void finish() {
    if (selectedIds.size() == 0) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.CANCEL);
      bundle.putInt(TagManagerUtils.MEMBERS, selectedIds.size());
      tagManager.trackEvent(TagManagerUtils.NewChat, bundle);
    }

    super.finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.SAVE);
    bundle.putInt(TagManagerUtils.MEMBERS, selectedIds.size());
    tagManager.trackEvent(TagManagerUtils.NewChat, bundle);
    finish();
  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut, BaseListViewHolder viewHolder) {

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
}
