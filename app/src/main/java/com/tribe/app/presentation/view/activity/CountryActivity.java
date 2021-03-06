package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Country;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.view.adapter.CountryPhoneNumberAdapter;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 * Last Modified by Horatio
 * Activity used for a user to select their country when inputting their phone number in the
 * AuthViewFragment.
 */
public class CountryActivity extends BaseActivity {

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, CountryActivity.class);
  }

  /**
   * Globals
   */

  @Inject CountryPhoneNumberAdapter countryAdapter;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.recyclerViewCountry) RecyclerView recyclerViewCountry;

  @BindView(R.id.countrySearchView) EditTextFont countrySearchView;

  private PhoneUtils phoneUtils;
  private LinearLayoutManager linearLayoutManager;

  private List<Country> listCountry;
  private List<Country> listCountryCopy;

  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  /**
   * Lifecycle methods
   */

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    phoneUtils = getApplicationComponent().phoneUtils();
    initUI();
    initSearchView();
    initDependencyInjector();
    initCountryList();
  }

  @Override protected void onPause() {
    screenUtils.hideKeyboard(this);
    super.onPause();
  }

  @Override protected void onDestroy() {
    countryAdapter.releaseSubscriptions();
    subscriptions.unsubscribe();
    super.onDestroy();
  }

  public void initUI() {
    setContentView(R.layout.activity_country);
    unbinder = ButterKnife.bind(this);
  }

  /**
   * Initialize view methods
   */

  public void initCountryList() {
    listCountry = new ArrayList<>();
    listCountryCopy = new ArrayList<>();

    subscriptions.add(Observable.just(new ArrayList<>(phoneUtils.getSupportedRegions()))
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(listCodeCountry -> {

          for (String countryCode : listCodeCountry) {
            String countryName = (new Locale("", countryCode).getDisplayCountry());
            listCountry.add(new Country(countryCode, countryName));
          }

          Collections.sort(listCountry);
          listCountryCopy.addAll(listCountry);
          countryAdapter.setItems(listCountry);
          linearLayoutManager = new LinearLayoutManager(this);
          recyclerViewCountry.setLayoutManager(linearLayoutManager);
          recyclerViewCountry.setAdapter(countryAdapter);

          subscriptions.add(countryAdapter.clickCountryItem().subscribe(view -> {
            Country country =
                countryAdapter.getItemAtPosition(recyclerViewCountry.getChildLayoutPosition(view));
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Extras.COUNTRY_CODE, country.code);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
          }));
        }));
  }

  private void initSearchView() {
    countrySearchView.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override public void afterTextChanged(Editable s) {
        filter(s.toString());
      }
    });
  }

  /**
   * Method used for search. Takes in a string parameter and updates recycler view accordingly.
   */

  private void filter(String text) {
    if (text.isEmpty()) {
      listCountry.clear();
      listCountry.addAll(listCountryCopy);
    } else {
      ArrayList<Country> result = new ArrayList<>();
      text = text.toLowerCase();

      for (Country item : listCountryCopy) {
        if (item.name.toLowerCase().contains(text) || item.name.toLowerCase().contains(text)) {
          result.add(item);
        }
      }

      listCountry.clear();
      listCountry.addAll(result);
    }
    countryAdapter.setItems(listCountry);
  }

  /**
   * Dagger Setup
   */

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, R.anim.slide_in_down);
  }
}
