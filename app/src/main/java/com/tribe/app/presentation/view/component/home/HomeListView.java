package com.tribe.app.presentation.view.component.home;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.app.presentation.view.widget.picto.PictoChatView;
import com.tribe.app.presentation.view.widget.picto.PictoLiveView;
import com.tribe.app.presentation.view.widget.text.TextHomeNameActionView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 09/04/2017.
 */
public class HomeListView extends RelativeLayout {

  @IntDef({ NORMAL, LIVE, CHAT }) public @interface Type {
  }

  public static final int NORMAL = 0;
  public static final int LIVE = 1;
  public static final int CHAT = 2;

  @BindView(R.id.viewPictoChat) PictoChatView viewPictoChat;
  @BindView(R.id.viewPictoLive) PictoLiveView viewPictoLive;
  @BindView(R.id.viewNewAvatar) NewAvatarView viewAvatar;
  @BindView(R.id.viewHomeNameAction) TextHomeNameActionView viewHomeNameAction;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  // RX SUBSCRIPTIONS / SUBJECTS

  // RESOURCES

  // VARIABLES
  private Unbinder unbinder;
  private int type;
  private Recipient recipient;

  public HomeListView(Context context) {
    super(context);
    init();
  }

  public HomeListView(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HomeListView);
    type = a.getInt(R.styleable.HomeListView_homeViewType, NORMAL);
    a.recycle();

    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
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

  public void setRecipient(Recipient recipient) {
    this.recipient = recipient;

    if (!(recipient instanceof Invite)) {
      viewAvatar.setType(recipient.isOnline() ? NewAvatarView.ONLINE : NewAvatarView.NORMAL);
    }

    viewAvatar.load(recipient);
  }
}
