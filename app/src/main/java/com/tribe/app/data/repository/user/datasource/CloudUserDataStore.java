package com.tribe.app.data.repository.user.datasource;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Pair;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.functions.Action1;

/**
 * {@link UserDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudUserDataStore implements UserDataStore {

    private static final int LOOKUP_LIMIT = 50;

    private final TribeApi tribeApi;
    private final LoginApi loginApi;
    private final UserCache userCache;
    private final TribeCache tribeCache;
    private final ChatCache chatCache;
    private final ContactCache contactCache;
    private final RxContacts rxContacts;
    private final RxFacebook rxFacebook;
    private final Context context;
    private AccessToken accessToken = null;
    private User user = null;
    private final Installation installation;
    private final ReactiveLocationProvider reactiveLocationProvider;
    private Preference<String> lastMessageRequest;
    private SimpleDateFormat utcSimpleDate = null;

    /**
     * Construct a {@link UserDataStore} based on connections to the api (Cloud).
     *
     * @param userCache   A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi    an implementation of the api
     * @param loginApi    an implementation of the login api
     * @param context     the context
     * @param accessToken the access token
     */
    public CloudUserDataStore(UserCache userCache, TribeCache tribeCache, ChatCache chatCache,
                              ContactCache contactCache, RxContacts rxContacts, RxFacebook rxFacebook,
                              TribeApi tribeApi, LoginApi loginApi, User user,
                              AccessToken accessToken, Installation installation,
                              ReactiveLocationProvider reactiveLocationProvider, Context context,
                              Preference<String> lastMessageRequest, SimpleDateFormat utcSimpleDate) {
        this.userCache = userCache;
        this.tribeCache = tribeCache;
        this.chatCache = chatCache;
        this.contactCache = contactCache;
        this.rxContacts = rxContacts;
        this.rxFacebook = rxFacebook;
        this.tribeApi = tribeApi;
        this.loginApi = loginApi;
        this.context = context;
        this.user = user;
        this.accessToken = accessToken;
        this.installation = installation;
        this.reactiveLocationProvider = reactiveLocationProvider;
        this.lastMessageRequest = lastMessageRequest;
        this.utcSimpleDate = utcSimpleDate;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        return this.loginApi
                .requestCode(new LoginEntity(phoneNumber));
        //.doOnError(throwable -> {
        //    AccessToken accessToken1 = new AccessToken();
        //    accessToken1.setAccessToken("DvEZQrxOZ5LgHQE9XjWYzCNMEcSmlCMVfvm27ZTLJ72KpRpVIY");
        //    accessToken1.setTokenType("Bearer");
        //    accessToken1.setUserId("BJgkS2rN");
        //    CloudUserDataStore.this.userCache.put(accessToken1);
        //});
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String pinId) {
        return this.loginApi
                .loginWithUsername(new LoginEntity(phoneNumber, code, pinId))
//                .doOnError(throwable -> {
//                    AccessToken accessToken1 = new AccessToken();
//                    accessToken1.setAccessToken("gI1J6AWUKIg6bTXOm9KNTCD3cI52o8qoQloGcGLD7RUj0i0RUx");
//                    accessToken1.setTokenType("Bearer");
//                    accessToken1.setUserId("BJAYhzzo");
//                    CloudUserDataStore.this.userCache.put(accessToken1);
//                })
                .doOnNext(saveToCacheAccessToken);
    }

    @Override
    public Observable<AccessToken> loginWithUsername(String username, String password) {
        return this.loginApi
                .loginWithUsername(new LoginEntity(username, password, ""))
                .doOnNext(saveToCacheAccessToken);
    }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return Observable.zip(this.tribeApi.getUserInfos(context.getString(R.string.user_infos, context.getString(R.string.userfragment_infos))),
                reactiveLocationProvider.getLastKnownLocation().onErrorReturn(throwable -> null).defaultIfEmpty(null),
                (userRealm, location) -> {
                    if (location != null) {
                        LocationRealm locationRealm = new LocationRealm();
                        locationRealm.setLatitude(location.getLatitude());
                        locationRealm.setLongitude(location.getLongitude());
                        locationRealm.setHasLocation(true);
                        locationRealm.setId(userRealm.getId());
                        userRealm.setLocation(locationRealm);
                    }

                    return userRealm;
                }).doOnNext(saveToCacheUser);
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        String operatorName = telephonyManager.getNetworkOperatorName();
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String base = context.getString(R.string.install_base,
                accessToken.getUserId(),
                token,
                "android",
                Build.VERSION.RELEASE,
                Build.MANUFACTURER,
                Build.MODEL,
                info != null ? info.versionName : "UNKNOWN",
                context.getPackageName(),
                context.getResources().getConfiguration().locale.toString(),
                operatorName
        );

        String req = installation == null || installation.getId() == null ? context.getString(R.string.install_create, base) : context.getString(R.string.install_update, installation.getId(), base);
        return this.tribeApi.createOrUpdateInstall(req).doOnNext(saveToCacheInstall);
    }

    @Override
    public Observable<Installation> removeInstall() {
        return this.tribeApi.removeInstall(context.getString(R.string.install_remove, installation.getId())).doOnNext(aVoid -> {
                    //                    accessToken.setAccessToken(null);
                    //                    installation.setId(null);
                    //                    userCache.put((UserRealm) null);
                    // TODO: remove all files and clear databasee

                }
        );
    }

    @Override
    public Observable<List<MessageRealmInterface>> messages() {
        StringBuffer idsTribes = new StringBuffer();

        Set<String> toIds = new HashSet<>();

        UserRealm user = userCache.userInfosNoObs(accessToken.getUserId());

        for (FriendshipRealm fr : user.getFriendships()) {
            toIds.add(fr.getFriend().getId());
        }

        List<TribeRealm> lastTribesSent = tribeCache.tribesSent(toIds);

        int countTribes = 0;
        for (TribeRealm tribeRealm : lastTribesSent) {
            if (!StringUtils.isEmpty(tribeRealm.getId())) {
                idsTribes.append((countTribes > 0 ? "," : "") + "\"" + tribeRealm.getId() + "\"");
                countTribes++;
            }
        }

        String req = context.getString(R.string.messages_infos,
                !StringUtils.isEmpty(lastMessageRequest.get()) ? context.getString(R.string.messages_start, lastMessageRequest.get()) : "",
                !StringUtils.isEmpty(idsTribes.toString()) ? context.getString(R.string.tribe_sent_infos, idsTribes) : "");

        return tribeApi.messages(req).flatMap(messageRealmInterfaceList -> {
                    Set<String> idsFrom = new HashSet<>();

                    for (MessageRealmInterface message : messageRealmInterfaceList) {
                        if (message.getFrom() != null) {
                            UserRealm userRealm = userCache.userInfosNoObs(message.getFrom().getId());

                            if (userRealm == null) {
                                idsFrom.add(message.getFrom().getId());
                            } else {
                                message.setFrom(userRealm);
                            }
                        }
                    }

                    if (idsFrom.size() > 0) {
                        StringBuilder result = new StringBuilder();

                        for (String string : idsFrom) {
                            result.append("\"" + string + "\"");
                            result.append(",");
                        }

                        String idsFromStr = result.length() > 0 ? result.substring(0, result.length() - 1) : "";

                        String reqUserList = context.getString(R.string.user_infos_list, idsFromStr, context.getString(R.string.userfragment_infos));
                        return tribeApi.getUserListInfos(reqUserList);
                    } else {
                        return Observable.just(new ArrayList<UserRealm>());
                    }
                },
                (messageRealmInterfaceList, userRealmList) -> {
                    if (userRealmList != null && userRealmList.size() > 0) {
                        for (MessageRealmInterface message : messageRealmInterfaceList) {
                            if (message.getFrom() != null && StringUtils.isEmpty(message.getFrom().getUsername())) {
                                for (UserRealm userRealm : userRealmList) {
                                    if (message.getFrom().getId().equals(userRealm.getId())) {
                                        message.setFrom(userRealm);
                                    }
                                }
                            }
                        }
                    }

                    return messageRealmInterfaceList;
                }).doOnNext(saveToCacheMessages);
    }

    @Override
    public Observable<UserRealm> updateUser(String username, String displayName, String pictureUri) {
        String usernameParam;
        String displayNameParam;

        if (username == null)
            usernameParam = userCache.userInfosNoObs(accessToken.getUserId()).getUsername();
        else usernameParam = username;

        if (displayName == null)
            displayNameParam = userCache.userInfosNoObs(accessToken.getUserId()).getDisplayName();
        else displayNameParam = displayName;


        if (pictureUri == null) {
            String request = context.getString(R.string.user_mutate_username, usernameParam, displayNameParam);

            return this.tribeApi.updateUser(request)
                    .doOnNext(userRealm -> {
                        UserRealm dbUser = userCache.userInfosNoObs(accessToken.getUserId());
                        dbUser.setUsername(userRealm.getUsername());
                        user.setUsername(userRealm.getUsername());
                        dbUser.setDisplayName(userRealm.getDisplayName());
                        user.setDisplayName(userRealm.getDisplayName());
                        userCache.put(dbUser);
                    });
        } else {
            String request = context.getString(R.string.user_mutate_username, usernameParam, displayNameParam);
            RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

            File file = new File(Uri.parse(pictureUri).getPath());

            if (!(file != null && file.exists() && file.length() > 0)) {
                InputStream inputStream = null;
                file = FileUtils.getFileEnd(FileUtils.generateIdForMessage());
                try {
                    inputStream = context.getContentResolver().openInputStream(Uri.parse(pictureUri));
                    FileUtils.copyInputStreamToFile(inputStream, file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            RequestBody requestFile = null;
            MultipartBody.Part body = null;

            requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            body = MultipartBody.Part.createFormData("user_pic", "user_pic.jpg", requestFile);

            return tribeApi.updateUserMedia(query, body)
                    .doOnNext(userRealm -> {
                        UserRealm dbUser = userCache.userInfosNoObs(accessToken.getUserId());
                        dbUser.setProfilePicture(userRealm.getProfilePicture());
                        dbUser.setUsername(userRealm.getUsername());
                        user.setUsername(userRealm.getUsername());
                        dbUser.setDisplayName(userRealm.getDisplayName());
                        user.setDisplayName(userRealm.getDisplayName());
                        userCache.put(dbUser);
                        user.setProfilePicture(userRealm.getProfilePicture());
                    });

        }
    }


    @Override
    public Observable<List<ContactInterface>> contacts() {
        return Observable.zip(
                rxContacts.getContacts().toList(),
                FacebookUtils.isLoggedIn() ? rxFacebook.requestFriends() : Observable.just(new ArrayList<ContactFBRealm>()),
                (contactABRealmList, contactFBRealmList) -> {
                    List<ContactInterface> contactList = new ArrayList<>();

                    for (ContactABRealm contactABRealm : contactABRealmList) {
                        contactList.add(contactABRealm);
                    }

                    for (ContactFBRealm contactFBRealm : contactFBRealmList) {
                        contactList.add(contactFBRealm);
                    }

                    return contactList;
                }
        ).flatMap(contactList -> {
            Map<String, ContactInterface> phones = new HashMap<>();
            Map<String, ContactInterface> fbIds = new HashMap<>();

            for (ContactInterface contactI : contactList) {
                if (contactI instanceof ContactABRealm) {
                    ContactABRealm contactABRealm = (ContactABRealm) contactI;
                    for (PhoneRealm phoneRealm : contactABRealm.getPhones()) {
                        if (phoneRealm.isInternational())
                            phones.put(phoneRealm.getPhone(), contactI);
                    }
                } else if (contactI instanceof ContactFBRealm) {
                    ContactFBRealm contactFBRealm = (ContactFBRealm) contactI;
                    fbIds.put(contactFBRealm.getId(), contactI);
                }
            }

            UserRealm currentUser = userCache.userInfosNoObs(accessToken.getUserId());

            // WE REMOVE ALL THE PHONE NUMBERS THAT WE'RE ALREADY FRIENDS WITH
            for (FriendshipRealm fr : currentUser.getFriendships()) {
                contactList.remove(phones.get(fr.getFriend().getPhone()));
                phones.remove(fr.getFriend().getPhone());
                fbIds.remove(fr.getFriend().getFbid());
            }

            phones.remove(currentUser.getPhone());

            if (phones.size() > 0 || fbIds.size() > 0) {
                StringBuffer buffer = new StringBuffer();

                if (phones.size() > 0) {
                    StringBuilder result = new StringBuilder();

                    int count = 0;
                    int loopCount = 0;
                    for (String phone : phones.keySet()) {
                        result.append("\"" + phone + "\"");
                        result.append(",");
                        count++;

                        if (count % LOOKUP_LIMIT == 0) {
                            buffer.append(context.getString(R.string.lookup_phone, loopCount, result.length() > 0 ? result.substring(0, result.length() - 1) : ""));
                            loopCount++;
                            result = new StringBuilder();
                        }
                    }

                    buffer.append(context.getString(R.string.lookup_phone, loopCount, result.length() > 0 ? result.substring(0, result.length() - 1) : ""));
                } else if (fbIds.size() > 0) {
                    StringBuilder result = new StringBuilder();

                    int count = 0;
                    int loopCount = 0;
                    for (String fbid : fbIds.keySet()) {
                        result.append("\"" + fbid + "\"");
                        result.append(",");
                        count++;

                        if (count % LOOKUP_LIMIT == 0) {
                            buffer.append(context.getString(R.string.lookup_facebook, loopCount, result.length() > 0 ? result.substring(0, result.length() - 1) : ""));
                            loopCount++;
                            result = new StringBuilder();
                        }
                    }

                    buffer.append(context.getString(R.string.lookup_facebook, loopCount, result.length() > 0 ? result.substring(0, result.length() - 1) : ""));
                }

                String reqLookup = context.getString(R.string.lookup, buffer.toString(), "", context.getString(R.string.userfragment_infos));

                return tribeApi.lookup(reqLookup).map(lookupEntity -> {
                    for (UserRealm userRealm : lookupEntity.getLookup()) {
                        for (String phone : phones.keySet()) {
                            if (userRealm.getPhone().equals(phone)) {
                                phones.get(phone).addUser(userRealm);
                            }
                        }

                        for (String fbId : fbIds.keySet()) {
                            if (userRealm.getFbid().equals(fbId)) {
                                fbIds.get(fbId).addUser(userRealm);
                            }
                        }
                    }
                    return Pair.create(phones, lookupEntity);
                });
            }

            return Observable.just(Pair.create(phones, null));
        }, (contactList, entityPair) -> {
            StringBuffer buffer = new StringBuffer();
            String mutationCreateFriendship = null;
            String reqHowManyFriends = null;
            LookupEntity lookupEntity = entityPair.second != null ? (LookupEntity) entityPair.second : null;
            Map<String, ContactInterface> phonesHowManyFriends = new HashMap<>(entityPair.first);
            Map<String, ContactInterface> phonesNewFriendships = new HashMap<>(entityPair.first);

            if (lookupEntity != null && lookupEntity.getLookup() != null && lookupEntity.getLookup().size() > 0) {
                int count = 0;
                Set<String> phonesFound = new HashSet<>();

                for (UserRealm userRealmLookup : lookupEntity.getLookup()) {
                    phonesFound.add(userRealmLookup.getPhone());
                    buffer.append(context.getString(R.string.createFriendship_input, count, userRealmLookup.getId(), context.getString(R.string.friendships_infos)));
                }

                phonesHowManyFriends.keySet().removeAll(phonesFound);
                phonesNewFriendships.keySet().removeAll(phonesHowManyFriends.keySet());

                mutationCreateFriendship = context.getString(R.string.createFriendship_mutation, buffer.toString(), context.getString(R.string.userfragment_infos));
            }

            if (phonesHowManyFriends.size() > 0) {
                StringBuffer bufferHowManyFriends = new StringBuffer();
                StringBuilder resultHowManyFriends = new StringBuilder();

                int count = 0;
                int loopCount = 0;
                for (String phone : phonesHowManyFriends.keySet()) {
                    resultHowManyFriends.append("\"" + phone + "\"");
                    resultHowManyFriends.append(",");
                    count++;

                    if (count % LOOKUP_LIMIT == 0) {
                        bufferHowManyFriends.append(context.getString(R.string.howManyFriends_part, loopCount,
                                resultHowManyFriends.length() > 0 ? resultHowManyFriends.substring(0, resultHowManyFriends.length() - 1) : ""));
                        loopCount++;
                        resultHowManyFriends = new StringBuilder();
                    }
                }

                bufferHowManyFriends.append(context.getString(R.string.howManyFriends_part, loopCount,
                        resultHowManyFriends.length() > 0 ? resultHowManyFriends.substring(0, resultHowManyFriends.length() - 1) : ""));

                reqHowManyFriends = context.getString(R.string.mutation, bufferHowManyFriends.toString());
            }

            return Observable.zip(
                    tribeApi.howManyFriends(reqHowManyFriends),
                    StringUtils.isEmpty(mutationCreateFriendship) ? Observable.just(new CreateFriendshipEntity()) : tribeApi.createFriendship(mutationCreateFriendship),
                    (howManyFriendsResult, createFriendshipEntity) -> {
                        int indexHowMany = 0;

                        for (ContactInterface contactInterface : phonesHowManyFriends.values()) {
                            contactInterface.setHowManyFriends(howManyFriendsResult.get(indexHowMany));
                            indexHowMany++;
                        }

                        if (createFriendshipEntity != null && createFriendshipEntity.getNewFriendshipList() != null
                                && createFriendshipEntity.getNewFriendshipList().size() > 0) {
                            UserRealm currentUser = userCache.userInfosNoObs(accessToken.getUserId());
                            currentUser.getFriendships().addAll(createFriendshipEntity.getNewFriendshipList());
                            userCache.put(currentUser);
                        }

                        List<ContactInterface> interfaces = new ArrayList<ContactInterface>(contactList);
                        return interfaces;
                    });
        }).flatMap(listObservable -> listObservable).doOnNext(saveToCacheContacts);
    }

    private final Action1<AccessToken> saveToCacheAccessToken = accessToken -> {
        if (accessToken != null && accessToken.getAccessToken() != null) {
            if (this.accessToken == null) {
                this.accessToken = new AccessToken();
            }

            this.accessToken.setAccessToken(accessToken.getAccessToken());
            this.accessToken.setRefreshToken(accessToken.getRefreshToken());
            this.accessToken.setTokenType(accessToken.getTokenType());
            this.accessToken.setUserId(accessToken.getUserId());

            CloudUserDataStore.this.userCache.put(accessToken);
        }
    };

    private final Action1<UserRealm> saveToCacheUser = userRealm -> {
        if (userRealm != null) {
            CloudUserDataStore.this.userCache.put(userRealm);
        }
    };

    private final Action1<Installation> saveToCacheInstall = installRealm -> {
        if (installRealm != null) {
            CloudUserDataStore.this.userCache.put(installRealm);
        }
    };

    private final Action1<List<MessageRealmInterface>> saveToCacheMessages = messageRealmList -> {
        this.lastMessageRequest.set(utcSimpleDate.format(new Date()));

        if (messageRealmList != null && messageRealmList.size() > 0) {
            List<TribeRealm> tribeRealmList = new ArrayList<>();
            List<ChatRealm> chatRealmList = new ArrayList<>();

            for (MessageRealmInterface message : messageRealmList) {
                if (message instanceof TribeRealm) tribeRealmList.add((TribeRealm) message);
                else if (message instanceof ChatRealm) chatRealmList.add((ChatRealm) message);
            }

            CloudUserDataStore.this.tribeCache.put(tribeRealmList);
            CloudUserDataStore.this.chatCache.put(chatRealmList);
        }
    };

    private final Action1<List<ContactInterface>> saveToCacheContacts = contactRealmList -> {
        if (contactRealmList != null && contactRealmList.size() > 0) {
            List<ContactFBRealm> contactFBRealm = new ArrayList<>();
            List<ContactABRealm> contactABRealm = new ArrayList<>();

            for (ContactInterface contactI : contactRealmList) {
                if (contactI instanceof ContactABRealm) {
                    contactABRealm.add((ContactABRealm) contactI);
                } else if (contactI instanceof ContactFBRealm) {
                    contactFBRealm.add((ContactFBRealm) contactI);
                }
            }

            CloudUserDataStore.this.contactCache.insertAddressBook(contactABRealm);
            CloudUserDataStore.this.contactCache.insertFBContactList(contactFBRealm);
        }
    };
}
