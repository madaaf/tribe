package com.tribe.app.presentation.view.adapter.delegate.contact;

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
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class SearchResultGridAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private Context context;
  private LayoutInflater layoutInflater;

  // OBSERVABLES
  private PublishSubject<View> onClick = PublishSubject.create();

  public SearchResultGridAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    SearchResultGridViewHolder vh = new SearchResultGridViewHolder(
        layoutInflater.inflate(R.layout.item_search_result, parent, false));
    return vh;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof SearchResult);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    SearchResultGridViewHolder vh = (SearchResultGridViewHolder) holder;
    SearchResult searchResult = (SearchResult) items.get(position);

    String displayName = searchResult.getDisplayName(), username =
        StringUtils.isEmpty(searchResult.getUsername()) ? "" : "@" + searchResult.getUsername(),
        picture = searchResult.getPicture();

    if (StringUtils.isEmpty(displayName)) {
      vh.btnAdd.setVisibility(View.GONE);

      if (searchResult.isSearchDone()) {
        displayName = "No user found";
      } else {
        displayName = context.getString(R.string.search_searching);
      }
    } else {
      vh.btnAdd.setVisibility(View.VISIBLE);
    }

    vh.txtName.setText(displayName);
    vh.txtUsername.setText(username);
    vh.viewNewAvatar.load(picture);

    if (searchResult.getShortcut() != null) {
      vh.btnAdd.setImageResource(R.drawable.picto_added);
      vh.btnAdd.setOnClickListener(null);
    } else {
      vh.btnAdd.setImageResource(R.drawable.picto_add);
      vh.btnAdd.setOnClickListener(view -> onClick.onNext(vh.itemView));
    }
  }

  class SearchResultGridViewHolder extends RecyclerView.ViewHolder {

    public SearchResultGridViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @BindView(R.id.viewNewAvatar) public NewAvatarView viewNewAvatar;

    @BindView(R.id.txtName) public TextViewFont txtName;

    @BindView(R.id.txtUsername) public TextViewFont txtUsername;

    @BindView(R.id.btnAdd) public ImageView btnAdd;
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onClick() {
    return onClick;
  }
}
