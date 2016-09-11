package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.ButtonPoints;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.ButtonPointsView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class ButtonPointsAdapterDelegate extends RxAdapterDelegate<List<Object>> {

    protected LayoutInflater layoutInflater;
    private Context context;

    // OBSERVABLES
    private final PublishSubject<View> clickButton = PublishSubject.create();
    private final PublishSubject<View> syncFBDone = PublishSubject.create();
    private final PublishSubject<View> notifyDone = PublishSubject.create();

    public ButtonPointsAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<Object> items, int position) {
        return (items.get(position) instanceof ButtonPoints);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        ContactViewHolder vh = new ContactViewHolder(layoutInflater.inflate(R.layout.item_button_points, parent, false));

        vh.buttonPointsView.onClick().map(view -> (View) view.getParent()).subscribe(clickButton);
        vh.buttonPointsView.onFBSyncDone().map(view -> (View) view.getParent()).subscribe(syncFBDone);
        vh.buttonPointsView.onNotifyDone().map(view -> (View) view.getParent()).subscribe(notifyDone);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        ContactViewHolder vh = (ContactViewHolder) holder;
        ButtonPoints buttonPoints = (ButtonPoints) items.get(position);

        vh.buttonPointsView.setType(buttonPoints.getType());
        vh.buttonPointsView.setDrawableResource(buttonPoints.getDrawable());
        vh.buttonPointsView.setDrawable(buttonPoints.getUrlImg());
        vh.buttonPointsView.setLabel(buttonPoints.getLabel());
        vh.buttonPointsView.setPoints(buttonPoints.getPoints());
        vh.buttonPointsView.setSubLabel(buttonPoints.getSubLabel());

        if (buttonPoints.isAnimate()) vh.buttonPointsView.animateProgress();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.buttonPoints)
        ButtonPointsView buttonPointsView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    // OBSERVABLES
    public Observable<View> onClick() {
        return clickButton;
    }

    public Observable<View> onFBSyncDone() {
        return syncFBDone;
    }

    public Observable<View> onNotifyDone() {
        return notifyDone;
    }
}
