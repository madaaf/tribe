package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

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
        String coordInput = "";

        if (tribeRealm.getLocationRealm() != null && tribeRealm.getLocationRealm().hasLocation()) {
             coordInput = context.getString(R.string.tribe_coord_input,
                    tribeRealm.getLocationRealm().getLatitude(), tribeRealm.getLocationRealm().getLongitude());
        }

        String tribeInput = context.getString(R.string.tribe_input,
                tribeRealm.isToGroup() ? tribeRealm.getMembershipRealm().getGroup().getId() : tribeRealm.getFriendshipRealm().getFriend().getId(),
                tribeRealm.getType(),
                simpleDateFormat.format(tribeRealm.getRecordedAt()),
                !StringUtils.isEmpty(coordInput) ? coordInput : ""
        );

        String request;

        if (tribeRealm.isToGroup())
            request = context.getString(R.string.tribe_send_group, tribeInput);
        else
            request = context.getString(R.string.tribe_send_solo, tribeInput);

        System.out.println("REQUEST : " + request);

        RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

        File file = new File(FileUtils.getPathForId(context, tribeRealm.getLocalId(), FileUtils.VIDEO));
        RequestBody requestFile = RequestBody.create(MediaType.parse("video/mp4"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("tribe", file.getName(), requestFile);

        return tribeApi.uploadTribe(query, body).map(tribeServer -> {
            tribeServer.setMessageSendingStatus(MessageSendingStatus.STATUS_SENT);
            return tribeCache.updateLocalWithServerRealm(tribeRealm, tribeServer);
        });
    }

    @Override
    public Observable<List<TribeRealm>> tribesNotSeen(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<TribeRealm>> tribesReceived(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<TribeRealm>> tribesForARecipient(String recipientId) {
        return null;
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
            FileUtils.delete(context, tribeRealm.getId(), FileUtils.VIDEO);
            buffer.append(context.getString(R.string.tribe_markAsSeen_item, "tribe" + tribeRealm.getId(), tribeRealm.getId()) + (count < tribeRealmList.size() - 1 ? "," : ""));
            count++;
        }

        String req = context.getString(R.string.mutation, buffer.toString());
        return this.tribeApi.markTribeListAsSeen(req);
    }

    @Override
    public Observable<Void> markTribeAsSave(TribeRealm tribeRealm) {
        String markAsSaved = context.getString(R.string.tribe_markAsSave_item, "tribe" + tribeRealm.getId(), tribeRealm.getId());

        String req = context.getString(R.string.mutation, markAsSaved);
        return this.tribeApi.markTribeAsSave(req);
    }
}
