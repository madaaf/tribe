package com.tribe.app.presentation.view.activity;

import android.app.Application;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.DaggerApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerAvengersComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.CharacterListPresenter;
import com.tribe.app.presentation.mvp.view.CharacterListView;
import com.tribe.app.presentation.view.adapter.AvengersListAdapter;
import com.tribe.app.presentation.view.widget.RecyclerInsetsDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CharacterListActivity extends AppCompatActivity implements CharacterListView {

    public final static String EXTRA_CHARACTER_NAME         = "character_name";
    public final static String EXTRA_IMAGE_TRANSITION_NAME  = "transition_name";

    @BindView(R.id.activity_avengers_recycler)
    RecyclerView mAvengersRecycler;
    @BindView(R.id.activity_avengers_toolbar)
    Toolbar mAvengersToolbar;
    @BindView(R.id.activity_avengers_progress)
    ProgressBar mAvengersProgress;
    @BindView(R.id.activity_avengers_collapsing)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.activity_avengers_empty_indicator)
    View mEmptyIndicator;
    @BindView(R.id.activity_avengers_error_view)
    View mErrorView;

    @Inject
    CharacterListPresenter mAvengersListPresenter;

    private AvengersListAdapter mCharacterListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initializeToolbar();
        initializeRecyclerView();
        initializeDependencyInjector();
        initializePresenter();
    }

    private void initUi() {
        setContentView(R.layout.activity_avengers_list);
        ButterKnife.bind(this);
    }

    private void initializeToolbar() {
        mCollapsingToolbar.setTitle("");
    }

    @OnClick(R.id.view_error_retry_button)
    public void onRetryButtonClicked(View v) {
        mAvengersListPresenter.onErrorRetryRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAvengersListPresenter.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAvengersListPresenter.onPause();
    }

    private void initializePresenter() {
        mAvengersListPresenter.attachView(this);
        mAvengersListPresenter.onCreate();
    }

    private void initializeDependencyInjector() {
        AndroidApplication androidApplication = (AndroidApplication) getApplication();

        DaggerAvengersComponent.builder()
            .activityModule(new ActivityModule(this))
            .applicationComponent(androidApplication.getApplicationComponent())
            .build().inject(this);
    }

    private void initializeRecyclerView() {
        mAvengersRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAvengersRecycler.addItemDecoration(new RecyclerInsetsDecoration(this));
    }

    @Override
    public void bindCharacterList(List<MarvelCharacter> avengers) {
        mCharacterListAdapter = new AvengersListAdapter(avengers, this,
            (position, sharedView, characterImageView) -> {
                mAvengersListPresenter.onElementClick(position);
            });

        mAvengersRecycler.setAdapter(mCharacterListAdapter);
    }

    @Override
    public void showCharacterList() {
        if (mAvengersRecycler.getVisibility() == View.GONE ||
            mAvengersRecycler.getVisibility() == View.INVISIBLE)

            mAvengersRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateCharacterList(int charactersAdded) {
        mCharacterListAdapter.notifyItemRangeInserted(
                mCharacterListAdapter.getItemCount() + charactersAdded, charactersAdded);
    }

    @Override
    public void hideCharactersList() {
        mAvengersRecycler.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingMoreCharactersIndicator() {

    }

    @Override
    public void hideLoadingMoreCharactersIndicator() {

    }

    @Override
    public void hideLoadingIndicator() {

    }


    @Override
    public void showLoadingView() {
        mEmptyIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingView() {
        mEmptyIndicator.setVisibility(View.GONE);
    }

    @Override
    public void showLightError() {
        Toast.makeText(CharacterListActivity.this, "Error lol", Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideErrorView() {
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyIndicator() {
        mEmptyIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyIndicator() {
        mEmptyIndicator.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAvengersListPresenter.onStop();
    }

    @Override
    public void showConnectionErrorMessage() {
        TextView errorTextView = ButterKnife.findById(mErrorView, R.id.view_error_message);
        errorTextView.setText("Error connection");
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showServerErrorMessage() {
        TextView errorTextView = ButterKnife.findById(mErrorView, R.id.view_error_message);
        errorTextView.setText("Error Marvel Server");
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showUknownErrorMessage() {
        TextView errorTextView = ButterKnife.findById(mErrorView, R.id.view_error_message);
        errorTextView.setText("Uknown error");
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDetailScreen(String characterName, int characterId) {

    }
}