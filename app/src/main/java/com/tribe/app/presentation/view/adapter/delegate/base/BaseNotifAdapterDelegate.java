package com.tribe.app.presentation.view.adapter.delegate.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.BaseNotifViewHolder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import javax.inject.Inject;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 29/06/2017.
 */

public abstract class BaseNotifAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  @Inject ScreenUtils screenUtils;

  protected LayoutInflater layoutInflater;
  private Context context;
  private boolean callRoulette;
  private Set<String> reportedIds = new HashSet();
  private Game currentGame;
  private int partialHeight, fullHeight;

  // OBSERVABLES
  protected PublishSubject<BaseNotifViewHolder> onClickAdd = PublishSubject.create();
  protected PublishSubject<View> onUnblock = PublishSubject.create();
  protected PublishSubject<BaseNotifViewHolder> clickMore = PublishSubject.create();

  public BaseNotifAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    partialHeight =
        context.getResources().getDimensionPixelSize(R.dimen.user_infos_notif_live_partial_height);
    fullHeight =
        context.getResources().getDimensionPixelSize(R.dimen.user_infos_notif_live_full_height);
  }

  public void setCallRoulette(boolean callRoulette) {
    this.callRoulette = callRoulette;
  }

  public void setCurrentGame(Game currentGame) {
    this.currentGame = currentGame;
  }

  protected RecyclerView.ViewHolder onCreateViewHolderNotif(ViewGroup parent) {
    BaseNotifViewHolder vh = new BaseNotifViewHolder(
        layoutInflater.inflate(R.layout.item_base_list_notif, parent, false));
    return vh;
  }

  protected void onBindViewHolderForUnfriend(User friend, RecyclerView.ViewHolder holder,
      boolean isHidden, User user) {
    BaseNotifViewHolder vh = (BaseNotifViewHolder) holder;

    vh.txtName.setText(friend.getDisplayName());
    vh.txtDescription.setText("@" + friend.getUsername());
    vh.viewAvatar.load("");

    if (reportedIds.contains(friend.getId())) {
      vh.btnMore.setImageResource(R.drawable.picto_ban_active);
      vh.btnMore.setClickable(false);

    } else if (callRoulette) {
      vh.btnMore.setOnClickListener(v -> {
        reportedIds.add(friend.getId());
        clickMore.onNext(vh); // TODO MADA
        vh.progressView.setScaleX(0);
        vh.progressView.setScaleY(0);
        vh.progressView.setVisibility(View.VISIBLE);
        vh.btnMore.animate()
            .scaleX(0)
            .scaleY(0)
            .setDuration(300)
            .withEndAction(
                () -> vh.progressView.animate().scaleX(1).scaleY(1).setDuration(300).start())
            .start();
      });
    } else {
      vh.btnMore.setVisibility(View.GONE);
    }
    
    if (currentGame != null && currentGame.hasScores()) {
      UIUtils.changeHeightOfView(vh.layoutContent, fullHeight);
      vh.separator.setVisibility(View.VISIBLE);
      vh.layoutGame.setVisibility(View.VISIBLE);

      Score score = user.getScoreForGame(currentGame.getId());

      if (score == null) {
        score = new Score();
        score.setGame(currentGame);
      }

      new GlideUtils.GameImageBuilder(context, screenUtils).url(score.getGame().getIcon())
          .hasBorder(true)
          .hasPlaceholder(true)
          .rounded(true)
          .target(vh.imgIcon)
          .load();

      vh.txtPoints.setText("" + score.getValue());
      //vh.txtRanking.setText("#" + score.getRanking());
    } else {
      UIUtils.changeHeightOfView(vh.layoutContent, partialHeight);
      vh.separator.setVisibility(View.GONE);
      vh.layoutGame.setVisibility(View.GONE);
    }

    Shortcut s = ShortcutUtil.getShortcut(friend, user);
    if (s == null) {
      vh.btnAdd.setOnClickListener(v -> {
        vh.btnAdd.setImageResource(R.drawable.done_btn);
        onClickAdd.onNext(vh);
      });
    } else {
      vh.btnAdd.setImageResource(R.drawable.done_btn);
    }
  }
}
