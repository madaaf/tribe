package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessagePoke;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import eightbitlab.com.blurview.BlurView;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessagePokeAdapterDelegate extends BaseMessageAdapterDelegate {

  protected GameManager gameManager;

  public MessagePokeAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    gameManager = GameManager.getInstance(context);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessagePoke;
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    return new MessagePokeViewHolder(
        layoutInflater.inflate(R.layout.item_message_poke, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessagePoke m = (MessagePoke) items.get(position);
    MessagePokeViewHolder vh = (MessagePokeViewHolder) holder;

    String message =
        context.getString(R.string.poke_chat_event_below, m.getAuthor().getDisplayName(),
            m.getGameId(), ":joy:", ":joy:");

    if (m.getAuthor() != null) {
      vh.pokeViewAvatar.load(m.getAuthor().getProfilePicture());
    }

    vh.pokeMessage.setText(message);
    Game game = gameManager.getGameById(m.getGameId());

    if (game.getFriendLeader() != null) {
     // UIUtils.changeHeightOfView(vh.layoutContent, screenUtils.dpToPx(80));
      TribeGuest leader = game.getFriendLeader();
      vh.layoutBestFriend.setVisibility(View.VISIBLE);
      vh.viewAvatar.load(leader.getPicture());
      vh.txtEmojiGame.setText(game.getEmoji());
      vh.txtName.setVisibility(View.GONE);
    }

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });
    gd.setCornerRadius(10);

    ViewCompat.setBackground(vh.viewBackground, gd);

    vh.txtBaseline.setText(context.getString(R.string.poke_chat_tap_to_play));
    //UIUtils.changeHeightOfView(vh.cardView, screenUtils.dpToPx(60));

    scale(vh.imgIcon, 0.5f);
    scale(vh.imgLogo, 0.5f);
    scale(vh.imgAnimation3, 0.5f);
    scale(vh.imgAnimation2, 0.5f);
    scale(vh.imgAnimation1, 0.5f);

    new GlideUtils.GameImageBuilder(context, screenUtils).url(game.getIcon())
        .hasBorder(true)
        .hasPlaceholder(true)
        .rounded(true)
        .target(vh.imgIcon)
        .load();

    Glide.with(context).load(game.getLogo()).into(vh.imgLogo);

    for (int i = 0; i < game.getAnimation_icons().size(); i++) {
      String url = game.getAnimation_icons().get(i);
      ImageView imageView = null;

      if (i == 0) {
        imageView = vh.imgAnimation1;
      } else if (i == 1) {
        imageView = vh.imgAnimation2;
      } else if (i == 2) {
        imageView = vh.imgAnimation3;
      }

      Glide.with(context).load(url).into(imageView);
    }

    //setPendingBehavior(m, vh.container);

  }

  private void scale(View v, float value) {
    //  v.setScaleX(value);
    //  v.setScaleY(value);
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MessagePokeViewHolder vh = (MessagePokeViewHolder) holder;
    MessagePoke m = (MessagePoke) items.get(position);
  }

  static class MessagePokeViewHolder extends BaseTextViewHolder {

    @BindView(R.id.pokeMessage) TextViewFont pokeMessage;
    @BindView(R.id.pokeViewAvatar) AvatarView pokeViewAvatar;
    @BindView(R.id.container) LinearLayout container;
    @BindView(R.id.viewBackground) View viewBackground;
    @BindView(R.id.imgIcon) ImageView imgIcon;
    @BindView(R.id.imgLogo) ImageView imgLogo;
    @BindView(R.id.cardView) CardView cardView;
    @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
    @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
    @BindView(R.id.imgAnimation3) ImageView imgAnimation3;
    @BindView(R.id.viewBlur) BlurView viewBlur;
    @BindView(R.id.layoutContent) FrameLayout layoutContent;
    @BindView(R.id.layoutBestFriend) RelativeLayout layoutBestFriend;
    @BindView(R.id.viewAvatar) AvatarView viewAvatar;
    @BindView(R.id.txtEmojiGame) TextViewFont txtEmojiGame;
    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtBaseline) TextViewFont txtBaseline;

    MessagePokeViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
