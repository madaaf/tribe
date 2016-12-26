package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.animation.AnimatorSet;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class SearchResultGridAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

    private static final int DURATION = 300;
    private static final float OVERSHOOT = 0.75f;

    public static final String ACTION_ADD = "action_add";

    @Inject
    ScreenUtils screenUtils;

    // VARIABLES
    private int avatarSize;
    private Map<SearchResultViewHolder, AnimatorSet> animations = new HashMap<>();

    // OBSERVABLES
    private PublishSubject<View> clickAdd = PublishSubject.create();
    private PublishSubject<View> clickRemove = PublishSubject.create();

    public SearchResultGridAdapterDelegate(Context context) {
        super(context);
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
        this.screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().screenUtils();
    }

    @Override
    public boolean isForViewType(@NonNull List<Object> items, int position) {
        return (items.get(position) instanceof SearchResult);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        SearchResultViewHolder vh = new SearchResultViewHolder(layoutInflater.inflate(R.layout.item_search, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        SearchResultViewHolder vh = (SearchResultViewHolder) holder;
        SearchResult searchResult = (SearchResult) items.get(position);

        if (animations.containsKey(holder)) {
            animations.get(holder).cancel();
        }

        if (searchResult.isShouldAnimateAdd()) {
            animateAddSuccessful(vh);
        } else {
            if (searchResult.getFriendship() != null && !searchResult.getFriendship().isBlockedOrHidden()) {
                vh.imgPicto.setVisibility(View.VISIBLE);
                vh.imgPicto.setImageResource(R.drawable.picto_done_white);
                vh.btnAddBG.setVisibility(View.VISIBLE);
                vh.progressBarAdd.setVisibility(View.GONE);
            } else {
                vh.imgPicto.setVisibility(View.VISIBLE);
                vh.imgPicto.setImageResource(R.drawable.picto_add);
                vh.btnAddBG.setVisibility(View.GONE);
                vh.progressBarAdd.setVisibility(View.GONE);
            }
        }

        if (!StringUtils.isEmpty(searchResult.getDisplayName())) {
            if (searchResult.isInvisibleMode()) {
                vh.btnAdd.setVisibility(View.GONE);
                vh.imgGhost.setVisibility(View.VISIBLE);
            } else {
                vh.btnAdd.setVisibility(View.VISIBLE);
                vh.imgGhost.setVisibility(View.GONE);
            }

            vh.txtName.setText(searchResult.getDisplayName());
            vh.txtUsername.setText("@" + searchResult.getUsername());

            if (!StringUtils.isEmpty(searchResult.getPicture())) {
                Glide.with(context).load(searchResult.getPicture())
                        .thumbnail(0.25f)
                        .override(avatarSize, avatarSize)
                        .bitmapTransform(new CropCircleTransformation(context))
                        .crossFade()
                        .into(vh.imgAvatar);
            }
        } else {
            vh.btnAdd.setVisibility(View.GONE);
            vh.imgGhost.setVisibility(View.GONE);

            if (searchResult.isSearchDone()) {
                vh.txtName.setText("No user found");
            } else {
                vh.txtName.setText(context.getString(R.string.contacts_section_search_searching));
            }
            vh.txtUsername.setText("@" + searchResult.getUsername());

            vh.imgAvatar.setImageDrawable(null);
            Glide.clear(vh.imgAvatar);
        }

        setClicks(vh, searchResult);
    }

    protected void setClicks(SearchResultViewHolder vh, SearchResult searchResult) {
        if (!searchResult.isInvisibleMode() && !searchResult.isMyself() && (searchResult.getFriendship() == null || searchResult.getFriendship().isBlockedOrHidden())) {
            vh.btnAdd.setOnClickListener(v -> onClick(vh));
        } else {
            vh.btnAdd.setOnClickListener(null);
        }
    }

    public class SearchResultViewHolder extends AddAnimationViewHolder {

        @BindView(R.id.imgAvatar)
        public ImageView imgAvatar;

        @BindView(R.id.txtName)
        public TextViewFont txtName;

        @BindView(R.id.txtUsername)
        public TextViewFont txtUsername;

        @BindView(R.id.imgGhost)
        public ImageView imgGhost;

        public SearchResultViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
