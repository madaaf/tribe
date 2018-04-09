package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.NewChatPresenter;
import com.tribe.app.presentation.mvp.view.adapter.NewChatMVPViewAdapter;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.adapter.NewChatAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerHeadersItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.NewChatLayoutManager;
import com.tribe.app.presentation.view.component.chat.ShortcutCompletionView;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class GameMembersActivity extends BaseBroadcastReceiverActivity
    implements TokenCompleteTextView.TokenListener<Shortcut> {

  public static final String GAME_ID = "game_id";
  public static final String SHORTCUT = "shortcut";
  public static final String CALL_ROULETTE = "call_roulette";

  public static Intent getCallingIntent(Activity activity, String gameId) {
    Intent intent = new Intent(activity, GameMembersActivity.class);
    intent.putExtra(GAME_ID, gameId);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject TagManager tagManager;

  @Inject NewChatPresenter newChatPresenter;

  @Inject NewChatAdapter newChatAdapter;

  @BindView(R.id.btnBack) ImageView btnBack;
  @BindView(R.id.btnPlay) TextViewFont btnPlay;
  @BindView(R.id.viewShortcutCompletion) ShortcutCompletionView viewShortcutCompletion;
  @BindView(R.id.recyclerViewShortcuts) RecyclerView recyclerViewShortcuts;

  // VARIABLES
  private UserComponent userComponent;
  private NewChatMVPViewAdapter newChatMVPViewAdapter;
  private NewChatLayoutManager layoutManager;
  private FilteredArrayAdapter<Shortcut> adapter;
  private List<Shortcut> items;
  private Set<String> selectedIds;
  private int count = 0;
  private GameManager gameManager;
  private Game game;
  private String previousFilter = "";

  // RESOURCES

  // OBSERVABLES
  protected CompositeSubscription subscriptions;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_members);

    ButterKnife.bind(this);

    gameManager = GameManager.getInstance(this);
    game = gameManager.getGameById(getIntent().getStringExtra(GAME_ID));

    initDependencyInjector();
    initPresenter();
    initSubscriptions();
    initUI();
    initRecyclerView();
  }

  @Override protected void onStart() {
    super.onStart();
    newChatPresenter.onViewAttached(newChatMVPViewAdapter);
    newChatPresenter.loadSingleShortcuts();
  }

  @Override protected void onStop() {
    screenUtils.hideKeyboard(viewShortcutCompletion);
    newChatPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  protected void initPresenter() {
    newChatMVPViewAdapter = new NewChatMVPViewAdapter() {
      @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
        Bundle bundle = new Bundle();
        bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.SAVE);
        bundle.putInt(TagManagerUtils.MEMBERS, selectedIds.size());
        tagManager.trackEvent(TagManagerUtils.NewChat, bundle);

        Intent intent = new Intent();
        intent.putExtra(SHORTCUT, shortcut);
        if (game != null) intent.putExtra(GAME_ID, game.getId());
        setResult(RESULT_OK, intent);
        finish();
      }

      @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {
        if (GameMembersActivity.this.items.size() > 0) return;
        GameMembersActivity.this.items.clear();

        if (game != null) {
          GameMembersActivity.this.items.add(new Shortcut(Recipient.ID_CALL_ROULETTE));
        }

        GameMembersActivity.this.items.addAll(singleShortcutList);
        newChatAdapter.setItems(items);
        newChatAdapter.notifyDataSetChanged();
      }

      @Override public void onShortcut(Shortcut shortcut) {

      }
    };
  }

  protected void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {
    items = new ArrayList<>();
    selectedIds = new HashSet<>();

    btnPlay.setEnabled(false);

    new GlideUtils.GameImageBuilder(this, screenUtils).url(game.getIcon())
        .hasBorder(false)
        .hasPlaceholder(true)
        .rounded(true)
        .target(btnBack)
        .load();

    ViewCompat.setElevation(btnBack, screenUtils.dpToPx(5));

    adapter = new FilteredArrayAdapter<Shortcut>(this, R.layout.item_shortcut, items) {
      @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
          convertView = new View(getContext());
        }

        return convertView;
      }

      @Override protected boolean keepObject(Shortcut shortcut, String mask) {
        mask = mask.toLowerCase();
        return shortcut.getSingleFriend() != null &&
            !selectedIds.contains(shortcut.getSingleFriend().getId()) &&
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

    viewShortcutCompletion.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            viewShortcutCompletion.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            screenUtils.showKeyboard(viewShortcutCompletion, 200);
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
          if (shortcut.getId().equals(Shortcut.ID_CALL_ROULETTE)) {
            Intent intent = new Intent();
            intent.putExtra(CALL_ROULETTE, true);
            intent.putExtra(GAME_ID, game.getId());
            setResult(RESULT_OK, intent);
            finish();
            //navigator.navigateToNewCall(this, LiveActivity.SOURCE_CALL_ROULETTE, game.getId());
          } else {
            shortcut.setSelected(!shortcut.isSelected());
            shortcut.setAnimateAdd(true);

            newChatAdapter.update(shortcut);

            if (shortcut.isSelected()) {
              selectedIds.add(shortcut.getSingleFriend().getId());
              viewShortcutCompletion.addObject(shortcut, shortcut.getDisplayName());
            } else {
              selectedIds.remove(shortcut.getSingleFriend().getId());
              viewShortcutCompletion.removeObject(shortcut);
            }

            refactorAction();
          }
        }));

    subscriptions.add(viewShortcutCompletion.onFiltering().subscribe(s -> filter(s)));
  }

  private void filter(String text) {
    if (text.equals(previousFilter)) return;

    previousFilter = text;

    if (StringUtils.isEmpty(text)) {
      Timber.d("Empty filter : " + text);
      newChatAdapter.setItems(items);
      return;
    }

    List<Shortcut> temp = new ArrayList();
    for (Shortcut shortcut : items) {
      if (shortcut.getDisplayName().toLowerCase().startsWith(text)) {
        temp.add(shortcut);
      }
    }

    Timber.d("Filter : " + text);
    newChatAdapter.setItems(temp);
    previousFilter = text;
  }

  private void refactorAction() {
    if (count > 0) {
      btnPlay.setEnabled(true);
      btnPlay.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_new));
    } else {
      btnBack.setEnabled(false);
      btnPlay.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_unblock));
    }
  }

  protected void initDependencyInjector() {
    this.userComponent = DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override public void onTokenAdded(Shortcut token) {
    if (token == null || token.getSingleFriend() == null) return;
    Timber.d("Token Added");
    count++;
    selectedIds.add(token.getSingleFriend().getId());
    if (!token.isSelected()) {
      token.setSelected(true);
      token.setAnimateAdd(true);
      newChatAdapter.update(token);
    }
    refactorAction();
  }

  @Override public void onTokenRemoved(Shortcut token) {
    count--;
    Timber.d("Token removed");
    selectedIds.remove(token.getSingleFriend().getId());
    if (token.isSelected()) {
      token.setSelected(false);
      token.setAnimateAdd(true);
      newChatAdapter.update(token);
    }
    refactorAction();
  }

  /**
   * ONCLICK
   */

  @OnClick(R.id.btnBack) void clickBack() {
    finish();
  }

  @OnClick(R.id.btnPlay) void clickPlay() {
    if (selectedIds.size() > 0) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.NAME, game.getId());
      bundle.putString(TagManagerUtils.SOURCE, TagManagerUtils.NEW_GAME);
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.LAUNCHED);
      tagManager.trackEvent(TagManagerUtils.NewGame, bundle);

      screenUtils.hideKeyboard(viewShortcutCompletion);
      newChatPresenter.createShortcut(selectedIds.toArray(new String[selectedIds.size()]));
    } else {
      showToastMessage(getString(R.string.newchat_create_error_noone_selected));
    }
  }

  /**
   * PUBLIC
   */

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  /**
   * OBSERVABLES
   */
}
