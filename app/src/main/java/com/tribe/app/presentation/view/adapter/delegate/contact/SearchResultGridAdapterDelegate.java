package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class SearchResultGridAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

  public static final String ACTION_ADD = "action_add";

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  // VARIABLES
  private int avatarSize;
  private Map<SearchResultViewHolder, AnimatorSet> animations = new HashMap<>();

  // OBSERVABLES
  private PublishSubject<View> onHangLive = PublishSubject.create();

  public SearchResultGridAdapterDelegate(Context context) {
    super(context);
    this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller);
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof SearchResult);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    SearchResultViewHolder vh =
        new SearchResultViewHolder(layoutInflater.inflate(R.layout.item_search, parent, false));
    vh.gradientDrawable = new GradientDrawable();
    vh.gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    vh.gradientDrawable.setCornerRadius(screenUtils.dpToPx(100));
    vh.btnAdd.setBackground(vh.gradientDrawable);
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    SearchResultViewHolder vh = (SearchResultViewHolder) holder;
    SearchResult searchResult = (SearchResult) items.get(position);

    if (animations.containsKey(holder)) {
      animations.get(holder).cancel();
    }

    vh.btnAdd.setVisibility((!StringUtils.isEmpty(searchResult.getDisplayName())
        && !searchResult.isMyself()
        && !searchResult.isInvisibleMode()) ? View.VISIBLE : View.GONE);
    vh.imgGhost.setVisibility(searchResult.isInvisibleMode() ? View.VISIBLE : View.GONE);

    if (searchResult.isShouldAnimateAdd()) {
      animateAddSuccessful(vh, R.string.action_hang_live, ContextCompat.getColor(context,
          (searchResult.getFriendship() != null && (searchResult.getFriendship().isHidden()
              || !searchResult.getFriendship().isBlockedOrHidden())) ? R.color.blue_new
              : R.color.grey_unblock), ContextCompat.getColor(context, R.color.red));
      searchResult.setShouldAnimateAdd(false);

      setInfos(searchResult, vh);
    } else {
      vh.txtAction.setAlpha(1);
      vh.progressBarAdd.setAlpha(0);

      if (!StringUtils.isEmpty(searchResult.getDisplayName())) {
        if (searchResult.getFriendship() == null) {
          setAddFriendStyle(vh);
        } else if (searchResult.getFriendship() != null && !searchResult.getFriendship()
            .isBlockedOrHidden()) {
          setHangLiveStyle(vh);
        } else if (searchResult.getFriendship().isBlocked()) {
          setUnblock(vh);
        } else if (searchResult.getFriendship().isHidden()) {
          setUnhide(vh);
        }

        setInfos(searchResult, vh);
      } else {
        if (searchResult.isSearchDone()) {
          vh.txtName.setText("No user found");
        } else {
          vh.txtName.setText(context.getString(R.string.contacts_section_search_searching));
        }

        vh.txtUsername.setText("@" + searchResult.getUsername());

        GlideUtils.load(context, avatarSize, vh.imgAvatar);
      }
    }

    if (!searchResult.isInvisibleMode() && !searchResult.isMyself() && (searchResult.getFriendship()
        == null || searchResult.getFriendship().isBlockedOrHidden())) {
      vh.btnAdd.setOnClickListener(v -> {
        searchResult.setShouldAnimateAdd(true);
        onClick(vh);
      });
    } else {
      vh.btnAdd.setOnClickListener(v -> onHangLive.onNext(vh.itemView));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  private void setInfos(SearchResult searchResult, SearchResultViewHolder vh) {
    vh.txtName.setText(searchResult.getDisplayName());
    vh.txtUsername.setText("@" + searchResult.getUsername());

    if (!StringUtils.isEmpty(searchResult.getPicture())) {
      GlideUtils.load(context, searchResult.getPicture(), avatarSize, vh.imgAvatar);
    }
  }

  private void setHangLiveStyle(SearchResultViewHolder vh) {
    vh.txtAction.setText(R.string.action_hang_live);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.red));
    refactorAction(vh);
  }

  private void setAddFriendStyle(SearchResultViewHolder vh) {
    vh.txtAction.setText(R.string.action_add_friend);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.blue_new));
    refactorAction(vh);
  }

  private void setUnblock(SearchResultViewHolder vh) {
    vh.txtAction.setText(R.string.action_unblock);
    refactorAction(vh);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.grey_unblock));
  }

  private void setUnhide(SearchResultViewHolder vh) {
    vh.txtAction.setText(R.string.action_unhide);
    refactorAction(vh);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.blue_new));
  }

  private void refactorAction(SearchResultViewHolder vh) {
    vh.txtAction.measure(0, 0);
    UIUtils.changeWidthOfView(vh.btnAdd, vh.txtAction.getMeasuredWidth() + (2 * marginSmall));
    setAppearance(vh.txtAction);
  }

  private void setAppearance(TextViewFont txt) {
    TextViewCompat.setTextAppearance(txt, R.style.Body_Two_White);
    txt.setCustomFont(context, "Roboto-Bold.ttf");
  }

  public Observable<View> onHangLive() {
    return onHangLive;
  }

  public class SearchResultViewHolder extends AddAnimationViewHolder {

    @BindView(R.id.imgAvatar) ImageView imgAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtUsername;

    @BindView(R.id.imgGhost) ImageView imgGhost;

    public SearchResultViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
