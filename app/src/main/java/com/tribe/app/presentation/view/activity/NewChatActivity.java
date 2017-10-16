package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
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
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.component.chat.ShortcutCompletionView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.util.FontCache;
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

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.viewShortcutCompletion) ShortcutCompletionView viewShortcutCompletion;

  // VARIABLES
  private Unbinder unbinder;
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
          LayoutInflater l =
              (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
          convertView = l.inflate(R.layout.item_shortcut, parent, false);
        }

        Shortcut shortcut = getItem(position);
        TextViewFont textViewFont = ButterKnife.findById(convertView, R.id.txtName);
        NewAvatarView viewNewAvatar = ButterKnife.findById(convertView, R.id.viewNewAvatar);
        viewNewAvatar.load(shortcut);
        textViewFont.setText(shortcut.getDisplayName());

        return convertView;
      }

      @Override protected boolean keepObject(Shortcut shortcut, String mask) {
        mask = mask.toLowerCase();
        return shortcut.getDisplayName().toLowerCase().startsWith(mask);
      }
    };

    viewShortcutCompletion.setAdapter(adapter);
    viewShortcutCompletion.setDropDownWidth(screenUtils.getWidthPx());
    viewShortcutCompletion.setTokenListener(this);
    viewShortcutCompletion.allowCollapse(false);
    viewShortcutCompletion.allowDuplicates(false);
    ViewCompat.setElevation(viewShortcutCompletion, 0);
    viewShortcutCompletion.setDropDownVerticalOffset(screenUtils.dpToPx(17.5f));
    viewShortcutCompletion.setDropDownBackgroundDrawable(null);
    viewShortcutCompletion.setThreshold(0);
    viewShortcutCompletion.setPrefix(getString(R.string.newchat_to).toUpperCase() + "   ",
        ContextCompat.getColor(this, R.color.black_opacity_40),
        FontCache.getTypeface(FontUtils.PROXIMA_BOLD, this));
    viewShortcutCompletion.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
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
    super.finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    finish();
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
    adapter.notifyDataSetChanged();
  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  @Override public void onTokenAdded(Shortcut token) {
    count++;
    selectedIds.add(token.getSingleFriend().getId());
    refactorAction();
  }

  @Override public void onTokenRemoved(Shortcut token) {
    count--;
    selectedIds.remove(token.getSingleFriend().getId());
    refactorAction();
  }
}
