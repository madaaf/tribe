package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.PaletteGrid;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class EmptyGridAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

    private LayoutInflater layoutInflater;
    private Context context;

    public EmptyGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return items.get(position).getId().equals(Friendship.ID_EMPTY);
    }

    @NonNull
    @RxLogObservable
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new EmptyGridViewHolder(layoutInflater.inflate(R.layout.item_empty_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        EmptyGridViewHolder vh = (EmptyGridViewHolder) holder;
        vh.layoutContent.setBackgroundColor(PaletteGrid.get(position - 1));
    }


    static class EmptyGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutContent) public ViewGroup layoutContent;

        public EmptyGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
