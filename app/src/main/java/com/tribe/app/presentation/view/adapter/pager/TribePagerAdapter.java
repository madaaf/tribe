package com.tribe.app.presentation.view.adapter.pager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.view.component.TribeComponentView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tiago on 15/06/2016.
 */
public class TribePagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<Tribe> tribeList;
    private int currentPosition;

    @Inject
    public TribePagerAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.tribeList = new ArrayList<>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.item_tribe, container, false);

        TribeComponentView tribeComponentView = (TribeComponentView) itemView.findViewById(R.id.viewTribe);
        tribeComponentView.setTag(position);
        //tribeComponentView.initPlayer();
        if (position == currentPosition) tribeComponentView.startPlayer();

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        container.removeView((View) obj);
    }

    @Override
    public int getCount() {
        return tribeList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public float getPageWidth(int position) {
        return (float) 1;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setItems(List<Tribe> tribeList) {
        this.tribeList.addAll(tribeList);
        notifyDataSetChanged();
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void releaseTribe(int position, TribeComponentView tribeComponentView) {
        tribeComponentView.release();
    }

    public void startTribe(int position, TribeComponentView tribeComponentView) {
        tribeComponentView.startPlayer();
    }
}
