package com.tribe.tribelivesdk.di;

import android.content.Context;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.WebRTCClient;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Created by tiago on 01/13/17.
 */

@Module public class LiveModule {

  @Provides @Singleton public WebRTCClient provideWebRTCClient(Context context) {
    return new WebRTCClient(context);
  }

  @Provides @Singleton public TribeLiveSDK provideTribeLiveSDK(WebRTCClient webRTCClient) {
    return new TribeLiveSDK(webRTCClient);
  }
}
