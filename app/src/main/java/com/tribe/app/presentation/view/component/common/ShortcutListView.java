package com.tribe.app.presentation.view.component.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.viewholder.RecipientHomeViewHolder;
import com.tribe.app.presentation.view.widget.avatar.EmojiGameView;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.app.presentation.view.widget.picto.PictoChatView;
import com.tribe.app.presentation.view.widget.picto.PictoLiveView;
import com.tribe.app.presentation.view.widget.text.TextHomeNameActionView;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 09/04/2017.
 */
public class ShortcutListView extends RelativeLayout {

  @IntDef({ NORMAL, LIVE, CHAT }) public @interface Type {
  }

  public static final int NORMAL = 0;
  public static final int LIVE = 1;
  public static final int CHAT = 2;
  public static final int LIVE_CHAT = 3;

  @BindView(R.id.viewPictoChat) PictoChatView viewPictoChat;
  @BindView(R.id.viewPictoLive) PictoLiveView viewPictoLive;
  @BindView(R.id.txtEmojiGame) EmojiGameView txtEmojiGame;
  @BindView(R.id.viewNewAvatar) NewAvatarView viewAvatar;
  @BindView(R.id.viewHomeNameAction) TextHomeNameActionView viewHomeNameAction;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<View> onLongClick = PublishSubject.create();
  private PublishSubject<View> onMainClick = PublishSubject.create();
  private PublishSubject<View> onLive = PublishSubject.create();
  private PublishSubject<View> onChat = PublishSubject.create();

  // RX SUBSCRIPTIONS / SUBJECTS

  // RESOURCES

  // VARIABLES
  private Unbinder unbinder;
  private int type;
  private Recipient recipient;

  public ShortcutListView(Context context) {
    super(context);
    init();
  }

  public ShortcutListView(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShortcutListView);
    type = a.getInt(R.styleable.ShortcutListView_homeViewType, NORMAL);
    a.recycle();

    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
    initClicks();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    subscriptions.clear();
  }

  private void initResources() {
  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    int resLayout = R.layout.view_home_list;
    LayoutInflater.from(getContext()).inflate(resLayout, this);
    unbinder = ButterKnife.bind(this);

    switch (type) {
      case NORMAL:
        viewPictoLive.setStatus(PictoLiveView.INACTIVE);
        viewPictoChat.setStatus(PictoChatView.INACTIVE);
        viewHomeNameAction.setTextType(TextHomeNameActionView.NORMAL);
        break;

      case LIVE:
        viewPictoChat.setStatus(PictoChatView.INACTIVE);
        viewAvatar.setType(NewAvatarView.LIVE);
        viewPictoLive.setStatus(PictoLiveView.ACTIVE);
        viewHomeNameAction.setTextType(TextHomeNameActionView.LIVE);
        break;

      case CHAT:
        viewPictoChat.setStatus(PictoChatView.ACTIVE);
        viewPictoLive.setStatus(PictoLiveView.INACTIVE);
        viewHomeNameAction.setTextType(TextHomeNameActionView.CHAT);
        break;
    }
  }

  public void initClicks() {
    setOnClickListener(v -> onMainClick.onNext(v));

    setOnLongClickListener(v -> {
      onLongClick.onNext(v);
      return true;
    });

    viewPictoChat.setOnClickListener(v -> onChat.onNext(v));
    viewPictoLive.setOnClickListener(v -> onLive.onNext(v));
  }

  public void setRecipient(Recipient recipient, RecipientHomeViewHolder vh) {
    subscriptions.clear();

    this.recipient = recipient;

    txtEmojiGame.clear();

    if (!(recipient instanceof Invite)) {
      Shortcut shortcut = (Shortcut) recipient;
      viewAvatar.setType(recipient.isOnline() ? NewAvatarView.ONLINE : NewAvatarView.NORMAL);

      if (shortcut.isSingle()) {
        User user = shortcut.getSingleFriend();
        txtEmojiGame.setEmojiList(user.getEmojiLeaderGameList());
      }

      //if (!shortcut.isSingle()) {
      //  subscriptions.add(Observable.interval(1000, TimeUnit.MILLISECONDS)
      //      .observeOn(AndroidSchedulers.mainThread())
      //      .subscribe(interval -> {
      //        User user =
      //            shortcut.getMembers().get((int) (interval % shortcut.getMembers().size()));
      //        viewAvatar.load(user.getProfilePicture());
      //      }));
      //} else {
      //  viewAvatar.load(recipient);
      //}
    }

    //else {
    viewAvatar.load(recipient);
    //}

    if (recipient != null && recipient.isSupport()) {
      viewPictoLive.setVisibility(GONE);
    } else {
      viewPictoLive.setVisibility(VISIBLE);
    }

    if (!recipient.isRead()) {
      viewPictoChat.setStatus(PictoChatView.ACTIVE);
    } else {
      viewPictoChat.setStatus(PictoChatView.INACTIVE);
    }

    viewHomeNameAction.setRecipient(recipient);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onLongClick() {
    return onLongClick;
  }

  public Observable<View> onLiveClick() {
    return onLive;
  }

  public Observable<View> onChatClick() {
    return onChat;
  }

  public Observable<View> onMainClick() {
    return onMainClick;
  }
}
