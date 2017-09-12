package com.tribe.app.data.cache;

import android.content.Context;
import javax.inject.Inject;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class ChatCacheImpl implements ChatCache {

  private Context context;

  @Inject public ChatCacheImpl(Context context) {
    this.context = context;
  }
}
