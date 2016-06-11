package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Country;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.adapter.CountryPhoneNumberAdapter;
import com.tribe.app.presentation.view.utils.Extras;
import com.tribe.app.presentation.view.utils.PhoneUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class CountryActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, CountryActivity.class);
    }

    @Inject
    CountryPhoneNumberAdapter countryAdapter;

    @BindView(R.id.recyclerViewCountry)
    RecyclerView recyclerViewCountry;

    private PhoneUtils phoneUtils;
    private LinearLayoutManager linearLayoutManager;

    private Unbinder unbinder;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phoneUtils = getApplicationComponent().phoneUtils();

        initUI();
        initDependencyInjector();
        initCountryList();
    }

    @Override
    protected void onDestroy() {
        countryAdapter.releaseSubscriptions();
        subscription.unsubscribe();
        super.onDestroy();
    }

    public void initUI() {
        setContentView(R.layout.activity_country);
        unbinder = ButterKnife.bind(this);
    }

    public void initCountryList() {
        List<String> listCodeCountry = new LinkedList<>(phoneUtils.getSupportedRegions());
        LinkedList<Country> listCountry = new LinkedList<>();

        for (String countryCode : listCodeCountry) {
            String countryName = (new Locale("", countryCode).getDisplayCountry());
            listCountry.add(new Country(countryCode, countryName));
        }

        Collections.sort(listCountry);

        countryAdapter.setItems(listCountry);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewCountry.setLayoutManager(linearLayoutManager);
        recyclerViewCountry.setAdapter(countryAdapter);

        subscription = countryAdapter.clickCountryItem().subscribe(view -> {
            Country country = countryAdapter.getItemAtPosition(recyclerViewCountry.getChildLayoutPosition(view));
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Extras.COUNTRY_CODE, country.code);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }
}
