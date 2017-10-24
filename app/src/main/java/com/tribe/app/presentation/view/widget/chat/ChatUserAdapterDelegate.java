package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.wang.avi.AVLoadingIndicatorView;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

import static android.view.View.GONE;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatUserAdapterDelegate extends RxAdapterDelegate<List<User>> {

  private static int COUNTER_SECONDE = 10;

  protected LayoutInflater layoutInflater;
  private Context context;
  private List<View> viewDots = new ArrayList<>();
  private int counter = 0;
  private int width = 0;
  private int height = 0;
  private User user;

  private PublishSubject<String> onQuickChat = PublishSubject.create();

  public ChatUserAdapterDelegate(Context context, User user) {
    this.user = user;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
  }

  @Override public boolean isForViewType(@NonNull List<User> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh =
        new ChatUserViewHolder(layoutInflater.inflate(R.layout.item_user_chat, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<User> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ChatUserViewHolder vh = (ChatUserViewHolder) holder;
    User i = items.get(position);

    vh.itemView.setOnClickListener(view -> {
      if (items.size() > 1) onQuickChat.onNext(i.getId());
    });

    vh.name.setText(i.getDisplayName());
    vh.avatarView.load(i.getProfilePicture());
    // Timber.i("SOEF onBindViewHolder " + position + " " + i.toString());

    if (i.isOnline()) {
      vh.container.setBackground(
          ContextCompat.getDrawable(context, R.drawable.shape_rect_chat_blue));
      vh.name.setTextColor(ContextCompat.getColor(context, R.color.blue_new));
    } else {
      vh.container.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rect_chat));
      vh.name.setTextColor(ContextCompat.getColor(context, R.color.grey_chat_border));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<User> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    User i = (User) payloads.get(0);

    ChatUserViewHolder vh = (ChatUserViewHolder) holder;
    counter++;

    if (i.isActive()) {
      extendsDots(vh, i.isTyping());
    } else {
      shrankContainer(vh);
    }

    if (i.isOnline()) {
      vh.container.setBackground(
          ContextCompat.getDrawable(context, R.drawable.shape_rect_chat_blue));
      vh.name.setTextColor(ContextCompat.getColor(context, R.color.blue_new));
    } else {
      vh.container.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rect_chat));
      vh.name.setTextColor(ContextCompat.getColor(context, R.color.grey_chat_border));
    }
  }

  private void extendsDots(ChatUserViewHolder vh, boolean isTyping) {
    if (isTyping) {
      vh.dotsContainer.setVisibility(View.VISIBLE);
      vh.loadingRecordView.setVisibility(View.GONE);
    } else {
      vh.dotsContainer.setVisibility(View.GONE);
    }

    vh.container.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rect_chat_blue));
    vh.name.setTextColor(ContextCompat.getColor(context, R.color.blue_new));
    vh.container.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            vh.container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (width == 0 || height == 0) {
              width = vh.container.getWidth();
              height = vh.container.getHeight();
            }
            ResizeAnimation a = new ResizeAnimation(vh.container);
            a.setDuration(300);
            a.setInterpolator(new OvershootInterpolator());
            a.setAnimationListener(new AnimationListenerAdapter() {
              @Override public void onAnimationStart(Animation animation) {
                if (isTyping) {
                  initDots(vh.dotsContainer);
                  animateSpin();
                } else {
                  vh.loadingRecordView.setVisibility(View.VISIBLE);
                }
              }
            });
            a.setParams(width, width + 70, height, height);
            vh.container.startAnimation(a);
          }
        });
  }

  private void shrankContainer(ChatUserViewHolder vh) {
    vh.dotsContainer.setVisibility(GONE);
    vh.loadingRecordView.setVisibility(GONE);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height);

    params.setMargins(0, 0, 20, 0);
    vh.container.setLayoutParams(params);
    vh.container.invalidate();
    vh.container.requestLayout();
  }

  private LinearLayout initDots(LinearLayout container) {
    if (container.getChildCount() > 0) return container;
    int sizeDot = context.getResources().getDimensionPixelSize(R.dimen.view_dot_size_chat);
    for (int i = 0; i < 3; i++) {
      View v = new View(context);
      v.setBackgroundResource(R.drawable.shape_oval_grey);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.setMargins(0, 0, 10, 0);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      viewDots.add(v);
      container.addView(v);
    }
    return container;
  }

  private void animateSpin() {
    for (int i = 0; i < viewDots.size(); i++) {
      final boolean last = (i == (viewDots.size() - 1));
      int delayBetweenDots = i * 100;
      View viewDot = viewDots.get(i);
      viewDot.animate()
          .setDuration(200)
          .setStartDelay(delayBetweenDots)
          .translationY(-10)
          .setInterpolator(new AccelerateInterpolator())
          .withEndAction(
              () -> viewDot.animate().setDuration(300).translationY(0).withEndAction(() -> {
                if (last) {
                  animateSpin();
                }
              }).start())
          .start();
    }
  }

  static class ChatUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name) TextViewFont name;
    @BindView(R.id.viewAvatar) AvatarView avatarView;
    @BindView(R.id.container) LinearLayout container;
    @BindView(R.id.dotsContainer) LinearLayout dotsContainer;
    @BindView(R.id.loadingRecordView) AVLoadingIndicatorView loadingRecordView;

    public ChatUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<String> onQuickChat() {
    return onQuickChat;
  }
}
