package com.tribe.app.presentation.view.adapter.delegate.filterview;

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
import com.tribe.app.domain.entity.FilterEntity;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class IconAdapterDelegate extends RxAdapterDelegate<List<FilterEntity>> {

    // VARIABLES
    protected LayoutInflater layoutInflater;
    private Context context;

    // RESOURCES

    public IconAdapterDelegate(Context context) {
        this.layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public boolean isForViewType(@NonNull List<FilterEntity> items, int position) {
        return items.get(position).getType().equals(FilterEntity.ICON);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh =
                new IconViewHolder(layoutInflater.inflate(R.layout.item_pts_icon, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<FilterEntity> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
        FilterEntity filterEntity = items.get(position);
        IconViewHolder iconViewHolder = (IconViewHolder) holder;
        iconViewHolder.imgIcon.setImageResource(filterEntity.getDrawable());
    }

    @Override
    public void onBindViewHolder(@NonNull List<FilterEntity> items,
                                 @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

    }

    static class IconViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgIcon)
        public ImageView imgIcon;

        public IconViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
