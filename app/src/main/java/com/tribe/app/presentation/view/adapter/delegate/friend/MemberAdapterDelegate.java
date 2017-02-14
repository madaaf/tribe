package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.GlideUtils;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/22/16.
 */
public class MemberAdapterDelegate extends RxAdapterDelegate<List<GroupMember>> {

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  private int avatarSize;
  private Context context;
  private LayoutInflater layoutInflater;

  private PublishSubject<View> click = PublishSubject.create();

  public MemberAdapterDelegate(Context context) {
    this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<GroupMember> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh =
        new MemberViewHolder(layoutInflater.inflate(R.layout.item_member, parent, false));

    vh.itemView.setOnClickListener(v -> click.onNext(vh.itemView));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<GroupMember> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    MemberViewHolder vh = (MemberViewHolder) holder;
    GroupMember groupMember = items.get(position);

    if (!groupMember.isOgMember()) vh.viewRing.setVisibility(View.VISIBLE);

    if (!StringUtils.isEmpty(groupMember.getUser().getProfilePicture())) {
      GlideUtils.load(context, groupMember.getUser().getProfilePicture(), avatarSize, vh.imgAvatar);
    }
  }

  @Override public void onBindViewHolder(@NonNull List<GroupMember> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  static class MemberViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgAvatar) ImageView imgAvatar;

    @BindView(R.id.viewRing) View viewRing;

    public MemberViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return click;
  }
}
