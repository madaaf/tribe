package com.tribe.app.presentation.view.adapter.delegate.pulltosearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.PTSEntity;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class LetterAdapterDelegate extends RxAdapterDelegate<List<PTSEntity>> {

    // VARIABLES
    protected LayoutInflater layoutInflater;
    private Context context;

    // RESOURCES

    public LetterAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public boolean isForViewType(@NonNull List<PTSEntity> items, int position) {
        return items.get(position).getType().equals(PTSEntity.LETTER);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new LetterViewHolder(layoutInflater.inflate(R.layout.item_pts_letter, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<PTSEntity> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        PTSEntity ptsEntity = items.get(position);
        LetterViewHolder letterViewHolder = (LetterViewHolder) holder;
        letterViewHolder.txtLetter.setText(ptsEntity.getLetter());
        letterViewHolder.txtLetter.setAlpha(0.25f);
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtLetter) public TextViewFont txtLetter;

        public LetterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
