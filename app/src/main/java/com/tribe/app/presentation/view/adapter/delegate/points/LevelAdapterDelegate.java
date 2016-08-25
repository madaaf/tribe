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
public class LevelAdapterDelegate extends RxAdapterDelegate<List<ScoreUtils.Level>> {

    // VARIABLES
    protected LayoutInflater layoutInflater;
    private Context context;
    private int score;

    // RESOURCES
    private int marginVertical;

    public LevelAdapterDelegate(Context context, int score) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.marginVertical = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_small);
        this.score = score;
    }

    @Override
    public boolean isForViewType(@NonNull List<ScoreUtils.Level> items, int position) {
        ScoreUtils.Level level = items.get(position);
        return score >= level.getPoints();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new LevelViewHolder(layoutInflater.inflate(R.layout.item_level, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<ScoreUtils.Level> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        LevelViewHolder vh = (LevelViewHolder) holder;
        ScoreUtils.Level level = items.get(position);

        vh.txtPoints.setText(context.getString(R.string.points_suffix, level.getPoints() < 1000 ? "" + level.getPoints() : ScoreUtils.format(level.getPoints(), 0)));
        vh.imgLevel.setImageResource(level.getDrawableId());
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtPoints) public TextViewFont txtPoints;
        @BindView(R.id.imgLevel) public ImageView imgLevel;

        public LevelViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
