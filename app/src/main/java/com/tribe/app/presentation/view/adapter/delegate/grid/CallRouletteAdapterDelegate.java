package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import java.util.List;
import timber.log.Timber;

/**
 * Created by tiago on 01/18/2017.
 */
public class CallRouletteAdapterDelegate extends RecipientGridAdapterDelegate {

  public CallRouletteAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return  items.get(position).getSubId().equals(Recipient.ID_CALL_ROULETTE);
  }

  @Override protected int getLayoutId() {
    return R.layout.item_user_call_roulette;
  }
}
