package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.PTSEntity;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.PullToSearchAdapter;
import com.tribe.app.presentation.view.adapter.manager.PullToSearchLayoutManager;
import com.tribe.app.presentation.view.decorator.GridDividerTopAllItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class PullToSearchView extends FrameLayout {

    private static final String[] ENTITIES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z", "#", "\uD83D\uDE03", "\uD83D\uDD82", "\uD83C\uDFE0" };

    @BindView(R.id.recyclerViewPTS)
    RecyclerView recyclerViewPTS;

    // VARIABLES
    private PullToSearchLayoutManager ptsLayoutManager;
    private PullToSearchAdapter ptsAdapter;

    // RESOURCES
    private int marginTopDivider;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public PullToSearchView(Context context) {
        super(context);
        init(context, null);
    }

    public PullToSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_pull_to_search, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initRecyclerView();
        initItems();

        super.onFinishInflate();
    }

    private void init(Context context, AttributeSet attrs) {

    }

    private void initResources() {
        marginTopDivider = getContext().getResources().getDimensionPixelSize(R.dimen.vertical_margin_mid);
    }

    private void initRecyclerView() {
        ptsLayoutManager = new PullToSearchLayoutManager(getContext());
        recyclerViewPTS.setLayoutManager(ptsLayoutManager);
        ptsAdapter = new PullToSearchAdapter(getContext());
        recyclerViewPTS.setAdapter(ptsAdapter);
        recyclerViewPTS.setHasFixedSize(true);
        recyclerViewPTS.addItemDecoration(new GridDividerTopAllItemDecoration(marginTopDivider, ptsLayoutManager.getSpanCount()));
    }

    private void initItems() {
        List<PTSEntity> ptsEntityList = new ArrayList<>();

        for (String str : ENTITIES) {
            PTSEntity ptsEntity = new PTSEntity(PTSEntity.LETTER);
            ptsEntity.setLetter(str);
            ptsEntityList.add(ptsEntity);
        }

        ptsAdapter.setItems(ptsEntityList);
    }

    public void updatePTSList(List<Recipient> recipientList) {

    }
}
