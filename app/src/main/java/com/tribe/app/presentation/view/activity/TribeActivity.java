package com.tribe.app.presentation.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.PaletteGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.subscriptions.CompositeSubscription;

public class TribeActivity extends BaseActivity implements TribeView {

    public static final String[] PERMISSIONS_LOCATION = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
    public static final String RECIPIENT = "RECIPIENT";
    public static final String POSITION = "POSITION";

    public static Intent getCallingIntent(Context context, int position, Recipient recipient) {
        Intent intent = new Intent(context, TribeActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(RECIPIENT, recipient);
        return intent;
    }

    @Inject
    TribePresenter tribePresenter;

    @Inject
    PaletteGrid paletteGrid;

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    @BindView(R.id.viewTribePager)
    TribePagerView viewTribePager;

    // VARIABLES
    private Recipient recipient;
    private int position;
    private User currentUser;
    private TribeMessage currentTribe;
    private BottomSheetDialog dialogMore;
    private LabelSheetAdapter moreTypeAdapter;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initDependencyInjector();
        initTribePagerView();
        initSubscriptions();
        initPresenter();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewTribePager.onResume();
        tribePresenter.onResume();
    }

    @Override
    protected void onPause() {
        viewTribePager.onPause();
        tribePresenter.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        tribePresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        tribePresenter.onDestroy();

        if (unbinder != null) unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initParams() {
        recipient = (Recipient) getIntent().getSerializableExtra(RECIPIENT);
        position = getIntent().getIntExtra(POSITION, 0);
        currentUser = getCurrentUser();

        Bundle bundle = new Bundle();
        bundle.putString(TagManagerConstants.TYPE, recipient instanceof Membership ? TagManagerConstants.TYPE_TRIBE_GROUP : TagManagerConstants.TYPE_TRIBE_USER);
        tagManager.trackEvent(TagManagerConstants.KPI_TRIBES_OPENED, bundle);
    }

    private void initUi() {
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initTribePagerView() {
        int color = PaletteGrid.get(position - 1);
        viewTribePager.setBackgroundColor(color);
        viewTribePager.setItems(recipient.getReceivedTribes(), color);
        viewTribePager.initWithInfo(recipient);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();

        subscriptions.add(
                viewTribePager
                .onDismissHorizontal()
                .doOnNext(aVoid -> tribePresenter.markTribeListAsRead(recipient, viewTribePager.getTribeListSeens()))
                .delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));

        subscriptions.add(
                viewTribePager
                .onDismissVertical()
                .doOnNext(aVoid -> tribePresenter.markTribeListAsRead(recipient, viewTribePager.getTribeListSeens()))
                .delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));

        subscriptions.add(viewTribePager.onRecordStart()
                .map(view -> tribePresenter.createTribe(currentUser, recipient, viewTribePager.getTribeMode()))
                .subscribe(tribe -> viewTribePager.startRecording(tribe.getLocalId())));

        subscriptions.add(
                viewTribePager.onRecordEnd()
                .subscribe(view -> {
                    viewTribePager.stopRecording();
                    viewTribePager.showTapToCancel(currentTribe);
                }));

        subscriptions.add(viewTribePager.onClickTapToCancel()
                .subscribe(friendship -> {
                    FileUtils.delete(context(), currentTribe.getLocalId(), FileUtils.VIDEO);
                    tribePresenter.deleteTribe(currentTribe);
                    currentTribe = null;
                }));

        subscriptions.add(viewTribePager.onNotCancel()
                .subscribe(friendship -> {
                    tribePresenter.sendTribe(currentTribe);
                    currentTribe = null;
                }));

        subscriptions.add(viewTribePager.onClickEnableLocation()
                .subscribe(view -> {
                    subscriptions.add(RxPermissions.getInstance(this)
                            .request(PERMISSIONS_LOCATION)
                            .subscribe(granted -> {
                                if (granted) {
                                    subscriptions.add(reactiveLocationProvider
                                            .getLastKnownLocation().subscribe(locationProvided -> {
                                                if (locationProvided != null) {
                                                    Location location = new Location(locationProvided.getLongitude(), locationProvided.getLatitude());
                                                    location.setLatitude(location.getLatitude());
                                                    location.setLongitude(location.getLongitude());
                                                    location.setHasLocation(true);
                                                    location.setId(currentUser.getId());
                                                    currentUser.setLocation(location);
                                                } else {
                                                    currentUser.setLocation(null);
                                                }

                                                updateCurrentView();
                                            }));
                                }
                            }));
                }));

        subscriptions.add(viewTribePager.onClickMore().subscribe(tribeMessage -> {
            setupBottomSheetMore(tribeMessage);
        }));

        subscriptions.add(viewTribePager.onErrorTribe()
                .subscribe(tribeMessage -> {
                    FileUtils.delete(context(), tribeMessage.getLocalId(), FileUtils.VIDEO);
                    tribeMessage.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                    tribePresenter.downloadMessages(tribeMessage);
                }));
    }

    private void initDependencyInjector() {
        DaggerTribeComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initPresenter() {
        tribePresenter.onStart();
        tribePresenter.attachView(this);
        tribePresenter.loadTribes(recipient.getSubId());
    }

    @Override
    public void setCurrentTribe(TribeMessage tribe) {
        currentTribe = tribe;
        recipient.setTribe(tribe);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

    @Override
    public void updateNewTribes(List<TribeMessage> tribeList) {
        for (TribeMessage tribe : tribeList) {
            if (tribe.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_TO_DOWNLOAD)) tribePresenter.downloadMessages(tribe);
        }

        viewTribePager.updateItems(tribeList);
    }

    private void updateCurrentView() {
        viewTribePager.updateCurrentView();
    }

    /**
     * Bottom sheet set-up
     */
    private void prepareBottomSheetCamera(TribeMessage tribe, List<LabelType> items) {
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_more, null);
        RecyclerView recyclerViewMore = (RecyclerView) view.findViewById(R.id.recyclerViewMore);
        recyclerViewMore.setHasFixedSize(true);
        recyclerViewMore.setLayoutManager(new LinearLayoutManager(this));
        moreTypeAdapter = new LabelSheetAdapter(this, items);
        moreTypeAdapter.setHasStableIds(true);
        recyclerViewMore.setAdapter(moreTypeAdapter);
        subscriptions.add(moreTypeAdapter.clickLabelItem()
                .map((View labelView) -> moreTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    MoreType moreType = (MoreType) labelType;

                    if (moreType.getMoreType().equals(MoreType.TRIBE_SAVE)) {
                        FileUtils.saveToMediaStore(this, FileUtils.getPathForId(this, tribe.getId(), FileUtils.VIDEO));
                    }

                    tribePresenter.markTribeAsSave(recipient, tribe);

                    dismissDialogSheetMore();
                }));

        dialogMore = new BottomSheetDialog(this);
        dialogMore.setContentView(view);
        dialogMore.show();
        dialogMore.setOnDismissListener(dialog -> {
            moreTypeAdapter.releaseSubscriptions();
            dialogMore = null;
        });
    }

    private void setupBottomSheetMore(TribeMessage tribe) {
        if (dismissDialogSheetMore()) {
            return;
        }

        List<LabelType> moreType = new ArrayList<>();
        moreType.add(new MoreType(getString(R.string.tribe_more_save), MoreType.TRIBE_SAVE));

        prepareBottomSheetCamera(tribe, moreType);
    }

    private boolean dismissDialogSheetMore() {
        if (dialogMore != null && dialogMore.isShowing()) {
            dialogMore.dismiss();
            return true;
        }

        return false;
    }
}