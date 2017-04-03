package com.tribe.app.presentation.view.adapter.delegate.country;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Country;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class CountryAdapterDelegate extends RxAdapterDelegate<List<Country>> {

  protected LayoutInflater layoutInflater;

  // RX SUBSCRIPTIONS / SUBJECTS
  private final PublishSubject<View> clickCountryItem = PublishSubject.create();

  private Context context;

  public CountryAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
  }

  @Override public boolean isForViewType(@NonNull List<Country> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh =
        new CountryViewHolder(layoutInflater.inflate(R.layout.item_country, parent, false));

    subscriptions.add(RxView.clicks(vh.itemView)
        .takeUntil(RxView.detaches(parent))
        .map(country -> vh.itemView)
        .subscribe(clickCountryItem));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Country> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {

    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    CountryViewHolder vh = (CountryViewHolder) holder;
    Country country = items.get(position);
    String countryName =
        country.name + " (+" + phoneNumberUtil.getCountryCodeForRegion(country.code) + ")";
    vh.txtName.setText(countryName);


    try {
      vh.imageCountryFlag.setImageDrawable(context.getResources()
          .getDrawable(
              R.drawable.class.getField("picto_flag_" + country.code.toLowerCase()).getInt(null)));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      Log.e("country", country.name);
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Country> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  public Observable<View> clickCountryItem() {
    return clickCountryItem;
  }

  static class CountryViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtName) public TextViewFont txtName;
    @BindView(R.id.imageCountryFlag) public ImageView imageCountryFlag;

    public CountryViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
