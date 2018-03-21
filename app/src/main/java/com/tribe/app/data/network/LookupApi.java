package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.network.entity.LookupObject;
import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface LookupApi {

  @POST("/v2/lookup/{country_code}") Observable<List<LookupObject>> lookup(
      @Path("country_code") String countryCode, @Body List<LookupEntity> phoneList);

  @POST("/v2/lookup/{country_code}") Observable<List<LookupObject>> lookupFb(
      @Path("country_code") String countryCode, @Body List<LookupEntity> fbIds);
}