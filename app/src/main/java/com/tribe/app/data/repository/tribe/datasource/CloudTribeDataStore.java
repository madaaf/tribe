package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

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
    private final UserCache userCache;
    private final Context context;
    private final AccessToken accessToken;
    private final SimpleDateFormat simpleDateFormat;
    private final UserRealmDataMapper userRealmDataMapper;

    /**
     * Construct a {@link TribeDataStore} based on connections to the api (Cloud).
     * @param tribeCache A {@link TribeCache} to cache data retrieved from the api.
     * @param userCache A {@link UserCache} to cache / retrieve data from the api.
     * @param tribeApi an implementation of the api
     * @param context the context
     * @param accessToken the access token
     */
    public CloudTribeDataStore(TribeCache tribeCache, UserCache userCache, TribeApi tribeApi, AccessToken accessToken,
                               Context context, SimpleDateFormat simpleDateFormat, UserRealmDataMapper userRealmDataMapper) {
        this.tribeCache = tribeCache;
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.context = context;
        this.accessToken = accessToken;
        this.simpleDateFormat = simpleDateFormat;
        this.userRealmDataMapper = userRealmDataMapper;
    }

    @Override
    public Observable<Void> deleteTribe(TribeRealm tribeRealm) {
        return null;
    }

    @Override
    public Observable<TribeRealm> sendTribe(TribeRealm tribeRealm) {
        String request = context.getString(R.string.tribe_send,
                tribeRealm.getFrom().getId(),
                tribeRealm.isToGroup() ?  tribeRealm.getGroup().getId() : tribeRealm.getFriendshipRealm().getId(),
                tribeRealm.isToGroup(),
                tribeRealm.getType(),
                simpleDateFormat.format(tribeRealm.getRecordedAt()),
                0.0,
                0.0
        );

        RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

        File file = new File(FileUtils.getPathForId(tribeRealm.getLocalId()));
        RequestBody requestFile = RequestBody.create(MediaType.parse("video/mp4"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("tribe", file.getName(), requestFile);

        return tribeApi.uploadTribe(query, body).map(tribeServer -> {
            tribeServer.setMessageStatus(MessageStatus.STATUS_SENT);
            return tribeCache.updateLocalWithServerRealm(tribeRealm, tribeServer);
        });
    }

    @Override
    public Observable<List<TribeRealm>> tribes() {
        StringBuffer ids = new StringBuffer();

        for (TribeRealm tribeRealm : tribeCache.tribesSent()) {
            ids.append("\"" + tribeRealm.getId() + "\",");
        }

        return tribeApi.tribes(context.getString(R.string.tribe_infos, ids))
                .map(tribeRealmList -> {
                    //for (TribeRealm tribeRealm : tribeRealmList) {
                        //if (tribeRealm.getFrom() != null)
                        //    tribeRealm.setFrom(userCache.userInfosNoObs(tribeRealm.getFrom().getId()));
                    //}

                    return tribeRealmList;
                })
                .doOnNext(saveToCacheTribes);

//        return tribeApi.tribes(context.getString(R.string.tribe_infos))
//                .flatMap(new Func1<List<TribeRealm>, Observable<List<TribeRealm>>>() {
//                    @Override
//                    public Observable<List<TribeRealm>> call(List<TribeRealm> messages) {
//                        return Observable.from(messages).flatMap(new Func1<TribeRealm, Observable<TribeRealm>>() {
//                            @Override
//                            public Observable<TribeRealm> call(TribeRealm tribeRealm) {
//                                return userCache.userInfos(tribeRealm.getFrom().getId())
//                                        .onErrorResumeNext(
//                                                tribeApi.getUserInfos(
//                                                        context.getString(R.string.user_infos_tribe, tribeRealm.getFrom().getId())
//                                                ).doOnNext(saveToCacheUser)
//                                        ).map(userRealm -> {
//                                            TribeRealm tribeRealmClone = tribeRealm.cloneTribeRealm(tribeRealm);
//                                            tribeRealmClone.setFrom(userRealmDataMapper.transformToUserTribe(userRealm));
//                                            System.out.println("TribeMessage : " + tribeRealmClone.getId());
//                                            return tribeRealmClone;
//                                        });
//                            }
//                        }).reduce(new ArrayList<TribeRealm>(), (list, s) -> {
//                            list.add(s);
//                            return list;
//                        });
//                    }
//                }).doOnNext(saveToCacheTribes);


//    return tribeApi.tribes(context.getString(R.string.tribe_infos))
//        .flatMapIterable(tribes -> tribes)
//                .flatMap(tribeRealm -> userCache.userInfos(tribeRealm.getFrom().getId())
//                        .onErrorResumeNext(
//                                this.tribeApi.getUserInfos(
//                                        context.getString(R.string.user_infos_tribe, tribeRealm.getFrom().getId())
//                                ).doOnNext(saveToCacheUser)
//                        )
//                        .map(userRealm -> {
//                            TribeRealm tribeRealmClone = tribeRealm.cloneTribeRealm(tribeRealm);
//                            tribeRealmClone.setFrom(userRealmDataMapper.transformToUserTribe(userRealm));
//                            return tribeRealmClone;
//                        }).doOnError(throwable -> System.out.println("ERROR 1")).doOnEach(notification -> System.out.println("LOL EACH 1")).doOnCompleted(() -> System.out.println("Completed 1"))
//                        .toList().doOnError(throwable -> System.out.println("ERROR")).doOnEach(notification -> System.out.println("LOL EACH")).doOnCompleted(() -> System.out.println("Completed"))
//                ).doOnError(throwable -> System.out.println("ERROR 2")).doOnEach(notification -> System.out.println("LOL EACH 2")).doOnCompleted(() -> System.out.println("Completed 2")).doOnNext(saveToCacheTribes);
    }

    @Override
    public Observable<List<TribeRealm>> tribesPending() {
        return null;
    }

    @Override
    public Observable<List<TribeRealm>> markTribeListAsRead(List<TribeRealm> tribeRealmList) {
        StringBuffer buffer = new StringBuffer();

        int count = 0;
        for (TribeRealm tribeRealm : tribeRealmList) {
            FileUtils.deleteTribe(tribeRealm.getId());
            buffer.append(context.getString(R.string.tribe_markAsSeen_item, "tribe" + tribeRealm.getId(), tribeRealm.getId()) + (count < tribeRealmList.size() - 1 ? "," : ""));
            count++;
        }

        String req = context.getString(R.string.tribe_markAsSeen, buffer.toString());
        return this.tribeApi.markTribeListAsSeen(req);
    }

    private final Action1<List<TribeRealm>> saveToCacheTribes = tribeRealmList -> {
        if (tribeRealmList != null && tribeRealmList.size() > 0) {
            CloudTribeDataStore.this.tribeCache.put(tribeRealmList);
        }
    };

    private final Action1<UserRealm> saveToCacheUser = userRealm -> {
        if (userRealm != null) {
            CloudTribeDataStore.this.userCache.put(userRealm);
        }
    };
}
