package com.tribe.app.presentation.view.adapter.pager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.view.component.TribeComponentView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 15/06/2016.
 */
public class TribePagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<TribeMessage> tribeList;
    private int currentPosition;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private final PublishSubject<View> clickEnableLocation = PublishSubject.create();

    @Inject
    public TribePagerAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.tribeList = new ArrayList<>();
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.item_tribe, container, false);

        TribeComponentView tribeComponentView = (TribeComponentView) itemView.findViewById(R.id.viewTribe);
        tribeComponentView.setTag(position);
        tribeComponentView.setTribe(tribeList.get(position));
        tribeComponentView.onClickEnableLocation().subscribe(clickEnableLocation);
        tribeComponentView.startPlayer();

        if (position == currentPosition) {
            tribeComponentView.play();
        }

        itemView.setTag(R.id.tag_tribe, position);

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

    public void setItems(List<TribeMessage> tribeList) {
        this.tribeList.addAll(tribeList);
        notifyDataSetChanged();
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void releaseTribe(TribeComponentView tribeComponentView) {
        tribeComponentView.releasePlayer();
    }

    public void startTribe(TribeComponentView tribeComponentView) {
        tribeComponentView.play();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    public Observable<View> onClickEnableLocation() {
        return clickEnableLocation;
    }
}
