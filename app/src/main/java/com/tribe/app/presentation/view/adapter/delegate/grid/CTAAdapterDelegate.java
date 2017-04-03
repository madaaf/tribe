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
 * Created by tiago on 03/29/2017
 */
public abstract class CTAAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  protected LayoutInflater layoutInflater;
  protected ScreenUtils screenUtils;
  protected boolean shouldRoundCorners = false;
  protected Context context;

  private PublishSubject<View> onClick = PublishSubject.create();

  public CTAAdapterDelegate(Context context, boolean shouldRoundCorners) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
    this.shouldRoundCorners = shouldRoundCorners;
  }

  protected abstract int getLayoutId();

  protected abstract int getAvatarResource();

  protected abstract String getTitle();

  protected abstract String getSubtitle();

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    CTAViewHolder vh = new CTAViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));
    int nbrColumn = context.getResources().getInteger(R.integer.columnNumber);
    int sizeTile =
        screenUtils.getWidthPx() / context.getResources().getInteger(R.integer.columnNumber);
    int sizeAvatar = (int) ((screenUtils.getWidthPx() / nbrColumn) * TileView.RATIO_AVATAR_TILE);
    vh.viewAvatar.changeSize(sizeAvatar, true);
    UIUtils.changeHeightOfView(vh.layoutSubtitle, sizeAvatar);

    int sizeLayoutName =
        (int) ((sizeTile - (sizeAvatar - (int) (sizeAvatar * vh.viewAvatar.getShadowRatio()))))
            >> 1;
    int sizeStatus = sizeLayoutName;
    UIUtils.changeHeightOfView(vh.layoutTitle, sizeLayoutName);
    UIUtils.changeHeightOfView(vh.layoutSubtitle, sizeStatus);
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    bind(holder, position);
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    bind(holder, position);
  }

  private void bind(@NonNull RecyclerView.ViewHolder holder, int position) {
    CTAViewHolder vh = (CTAViewHolder) holder;
    UIUtils.setBackgroundGrid(screenUtils, vh.layoutContent, position, shouldRoundCorners);
    vh.viewAvatar.load(getAvatarResource());
    vh.txtTitle.setText(getTitle());
    vh.txtSubtitle.setText(getSubtitle());
    vh.layoutContent.setOnClickListener(v -> onClick.onNext(vh.itemView));
  }

  static class CTAViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.layoutContent) public ViewGroup layoutContent;
    @BindView(R.id.layoutTitle) public ViewGroup layoutTitle;
    @BindView(R.id.txtTitle) public TextViewFont txtTitle;
    @BindView(R.id.layoutSubtitle) public ViewGroup layoutSubtitle;
    @BindView(R.id.txtSubtitle) public TextViewFont txtSubtitle;
    @BindView(R.id.avatar) public AvatarView viewAvatar;

    public CTAViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClick() {
    return onClick;
  }
}
