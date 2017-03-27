package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 03/21/2017
 */
public class MoreFriendsAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  private LayoutInflater layoutInflater;
  private ScreenUtils screenUtils;
  private boolean shouldRoundCorners = false;
  private Context context;

  private PublishSubject<View> onClick = PublishSubject.create();

  public MoreFriendsAdapterDelegate(Context context, boolean shouldRoundCorners) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
    this.shouldRoundCorners = shouldRoundCorners;
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return items.get(position).getSubId().equals(Recipient.ID_MORE);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    MoreFriendsViewHolder vh = new MoreFriendsViewHolder(
        layoutInflater.inflate(R.layout.item_add_friends_grid, parent, false));
    int nbrColumn = context.getResources().getInteger(R.integer.columnNumber);
    int sizeAvatar = (int) ((screenUtils.getWidthPx() / nbrColumn) * TileView.RATION_AVATAR_TILE);
    vh.viewAvatar.changeSize(sizeAvatar, true);
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    MoreFriendsViewHolder vh = (MoreFriendsViewHolder) holder;
    UIUtils.setBackgroundGrid(screenUtils, vh.layoutContent, position, shouldRoundCorners);
    vh.viewAvatar.load(R.drawable.picto_more_friends);
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MoreFriendsViewHolder vh = (MoreFriendsViewHolder) holder;
    UIUtils.setBackgroundGrid(screenUtils, vh.layoutContent, position, shouldRoundCorners);
    vh.viewAvatar.load(R.drawable.picto_more_friends);
    vh.layoutContent.setOnClickListener(v -> onClick.onNext(vh.itemView));
  }

  static class MoreFriendsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.layoutContent) public ViewGroup layoutContent;
    @BindView(R.id.txtStatus) public TextViewFont txtStatus;
    @BindView(R.id.avatar) public AvatarView viewAvatar;
    @BindView(R.id.txtName) public TextViewFont txtName;

    public MoreFriendsViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return onClick;
  }
}
