package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.FilterEntity;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.FilterViewAdapter;
import com.tribe.app.presentation.view.adapter.decorator.GridDividerTopAllItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.FilterViewLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.SyncView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class FilterView extends LinearLayout {

    public static final String DIEZ = "#";
    public static final String EMOJI = "\uD83D\uDE03";
    private static final String[] ENTITIES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z", DIEZ, EMOJI };

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.recyclerViewFilter)
    RecyclerView recyclerViewFilter;

    @BindView(R.id.viewSyncAB)
    SyncView viewSyncAB;

    @BindView(R.id.viewSyncFB)
    SyncView viewSyncFB;

    // VARIABLES
    private FilterViewLayoutManager filterViewLayoutManager;
    private FilterViewAdapter filterViewAdapter;
    private static Map<String, FilterEntity> entityMap = new HashMap<>();
    private int emojiPosition;

    // RESOURCES
    private int marginTopDivider;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<String> letterSelected = PublishSubject.create();
    private PublishSubject<Void> onCloseClick = PublishSubject.create();

    public FilterView(Context context) {
        super(context);
        init(context, null);
    }

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
        }

        filterViewAdapter.releaseSubscriptions();

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_filter, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initUI();
        initRecyclerView();
        initItems();

        super.onFinishInflate();
    }

    private void init(Context context, AttributeSet attrs) {
        List<String> entities = Arrays.asList(ENTITIES);
        emojiPosition = entities.indexOf(EMOJI);
        setBackgroundResource(R.drawable.shape_rect_white_rounded_top_new);
        setOrientation(VERTICAL);
    }

    private void initUI() {

    }

    private void initResources() {
        marginTopDivider = getResources().getDimensionPixelSize(R.dimen.vertical_margin_small);
    }

    private void initRecyclerView() {
        filterViewLayoutManager = new FilterViewLayoutManager(getContext());
        recyclerViewFilter.setLayoutManager(filterViewLayoutManager);
        filterViewAdapter = new FilterViewAdapter(getContext());
        recyclerViewFilter.setAdapter(filterViewAdapter);
        recyclerViewFilter.setHasFixedSize(true);
        recyclerViewFilter.getRecycledViewPool().setMaxRecycledViews(0, 50);
        recyclerViewFilter.addItemDecoration(new GridDividerTopAllItemDecoration(0, filterViewLayoutManager.getSpanCount()));

        subscriptions.add(
                filterViewAdapter
                .onClickLetter()
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(viewFrom -> {
                    letterSelected.onNext(ENTITIES[(Integer) viewFrom.getTag(R.id.tag_position)]);
                }));
    }

    private void initItems() {
        List<FilterEntity> filterEntityList = new ArrayList<>();

        for (String str : ENTITIES) {
            FilterEntity filterEntity = new FilterEntity(FilterEntity.LETTER);
            filterEntity.setLetter(str);
            filterEntityList.add(filterEntity);
            entityMap.put(str, filterEntity);
        }

        filterViewAdapter.setItems(filterEntityList);
    }

    public void updateFilterList(List<Recipient> recipientList) {
        for (Recipient recipient : recipientList) {
            if (!Friendship.ID_EMPTY.equals(recipient.getId()) && !Friendship.ID_HEADER.equals(recipient.getId())) {
                if (isEmoji(recipient)) {
                    entityMap.get(EMOJI).setActivated(true);
                } else if (isLetter(recipient)) {
                    String firstCharacter = StringUtils.getFirstCharacter(recipient.getDisplayName());
                    FilterEntity entity = entityMap.get(firstCharacter.toUpperCase());
                    entity.setActivated(true);
                } else {
                    FilterEntity entitySpecialCharacters = entityMap.get(DIEZ);
                    entitySpecialCharacters.setActivated(true);
                }
            }
        }

        filterViewAdapter.notifyDataSetChanged();
    }

    public void clean() {
        if (recyclerViewFilter != null) {
            recyclerViewFilter.setAdapter(null);
            recyclerViewFilter.setLayoutManager(null);
            recyclerViewFilter.setAdapter(filterViewAdapter);
            recyclerViewFilter.setLayoutManager(filterViewLayoutManager);
            filterViewAdapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.btnClose)
    void close() {
        onCloseClick.onNext(null);
    }

    //////////////////////
    //     HELPERS      //
    //////////////////////

    public static boolean isLetter(Recipient recipient) {
        String firstCharacter = StringUtils.getFirstCharacter(recipient.getDisplayName());
        FilterEntity entity = entityMap.get(firstCharacter.toUpperCase());
        if (entity != null)
            return true;


        return false;
    }

    public static boolean isEmoji(Recipient recipient) {
        if (recipient.getDisplayName().length() > 5
                && recipient.getDisplayName().substring(0, 5) == ("/[\u2190-\u21FF] | [\u2600-\u26FF] | [\u2700-\u27BF] | [\u3000-\u303F] | [\u1F300-\u1F64F] | [\u1F680-\u1F6FF]/g")) {
            return true;
        }

        return false;
    }

    public static boolean shouldFilter(String filter, Recipient recipient) {
        if ((filter.equals(FilterView.EMOJI) && FilterView.isEmoji(recipient))
                || (filter.equals(FilterView.DIEZ) && !FilterView.isLetter(recipient))) {
            return true;
        } else if (FilterView.isLetter(recipient)) {
            String firstCharacter = StringUtils.getFirstCharacter(recipient.getDisplayName());
            if (!firstCharacter.isEmpty() && firstCharacter.equalsIgnoreCase(filter)) {
                return true;
            }
        }

        return false;
    }

    public void setFBSync(boolean success) {
        viewSyncFB.setActive(success, true);
    }

    public void setABSync(boolean success) {
        viewSyncAB.setActive(success, true);
    }

    public void updateSync() {
        viewSyncAB.updateSync();
        viewSyncFB.updateSync();
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////

    public Observable<String> onLetterSelected() {
        return letterSelected;
    }

    public Observable<Void> onCloseClick() {
        return onCloseClick;
    }

    public Observable<SyncView> onSyncFBClick() { return viewSyncFB.onClick(); }

    public Observable<SyncView> onSyncABClick() { return viewSyncAB.onClick(); }
}
