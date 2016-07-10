package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.presentation.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;

import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action1;

/**
 * {@link TribeDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudTribeDataStore implements TribeDataStore {

    private final TribeApi tribeApi;
    private final TribeCache tribeCache;
    private final Context context;
    private final AccessToken accessToken;
    private final SimpleDateFormat simpleDateFormat;

    /**
     * Construct a {@link TribeDataStore} based on connections to the api (Cloud).
     * @param tribeCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param context the context
     * @param accessToken the access token
     */
    public CloudTribeDataStore(TribeCache tribeCache, TribeApi tribeApi, AccessToken accessToken, Context context, SimpleDateFormat simpleDateFormat) {
        this.tribeCache = tribeCache;
        this.tribeApi = tribeApi;
        this.context = context;
        this.accessToken = accessToken;
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public Observable<Void> deleteTribe(TribeRealm tribeRealm) {
        return null;
    }

    @Override
    public Observable<TribeRealm> sendTribe(TribeRealm tribeRealm) {
        String request = context.getString(R.string.tribe_send,
                tribeRealm.getFrom().getId(),
                tribeRealm.getToGroup() == null ? tribeRealm.getToUser().getId() : tribeRealm.getToGroup().getId(),
                tribeRealm.getToGroup() != null,
                tribeRealm.getType(),
                simpleDateFormat.format(tribeRealm.getRecordedAt()),
                0.0,
                0.0
        );

        System.out.println("REQUEST : " + request);

        RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

        File file = new File(FileUtils.getPathForId(tribeRealm.getLocalId()));
        RequestBody requestFile = RequestBody.create(MediaType.parse("video/mp4"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("tribe", file.getName(), requestFile);

        return tribeApi.uploadTribe(query, body).map(tribeServer -> tribeCache.updateLocalWithServerRealm(tribeRealm, tribeServer));
    }

    @Override
    public Observable<RealmList<TribeRealm>> tribes() {
        return tribeApi.tribes(context.getString(R.string.tribe_infos))
                .doOnNext(saveToCacheTribes);
    }

    private final Action1<RealmList<TribeRealm>> saveToCacheTribes = tribeRealmList -> {
        if (tribeRealmList != null && tribeRealmList.size() > 0) {
            CloudTribeDataStore.this.tribeCache.put(tribeRealmList);
        }
    };
}