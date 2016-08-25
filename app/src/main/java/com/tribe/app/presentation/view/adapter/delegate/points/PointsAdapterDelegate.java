package com.tribe.app.presentation.view.adapter.delegate.points;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class PointsAdapterDelegate extends RxAdapterDelegate<List<ScoreUtils.Point>> {

    // VARIABLES
    protected LayoutInflater layoutInflater;
    private Context context;

    public PointsAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public boolean isForViewType(@NonNull List<ScoreUtils.Point> items, int position) {
        ScoreUtils.Point point = items.get(position);
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new LevelViewHolder(layoutInflater.inflate(R.layout.item_points, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<ScoreUtils.Point> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        LevelViewHolder vh = (LevelViewHolder) holder;
        ScoreUtils.Point point = items.get(position);

        vh.imgPicto.setImageResource(point.getDrawableId());
        vh.txtPoints.setText(point.getPoints() < 1000 ? "" + point.getPoints(): ScoreUtils.format(point.getPoints(), 0));
        vh.txtTitle.setText(point.getStringLabelId());
        vh.txtDescription.setText(point.getStringSubLabelId());
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgPicto) public ImageView imgPicto;
        @BindView(R.id.txtTitle) public TextViewFont txtTitle;
        @BindView(R.id.txtDescription) public TextViewFont txtDescription;
        @BindView(R.id.txtPoints) public TextViewFont txtPoints;

        public LevelViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
