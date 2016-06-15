package com.tribe.app.presentation.view.adapter.pager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tiago on 15/06/2016.
 */
public class TribePagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<Tribe> tribes;

    @Inject
    public TribePagerAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.tribes = new ArrayList<>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.view_tribe, container, false);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        container.removeView((View) obj);
    }

    @Override
    public int getCount() {
        return tribes.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public float getPageWidth(int position) {
        return (float) 1;
    }
}
