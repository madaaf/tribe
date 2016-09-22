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
import com.tribe.app.data.network.entity.RegisterEntity;
import com.tribe.app.data.network.entity.UsernameEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.GroupRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final String SEARCH_KEY = "search";

    private final TribeApi tribeApi;
    private final LoginApi loginApi;
    private UserCache userCache = null;
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
    private Preference<String> lastUserRequest;
    private SimpleDateFormat utcSimpleDate = null;
    private GroupRealmDataMapper groupRealmDataMapper;
    private UserRealmDataMapper userRealmDataMapper;

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
                              Preference<String> lastMessageRequest, Preference<String> lastUserRequest, SimpleDateFormat utcSimpleDate,
                            GroupRealmDataMapper groupRealmDataMapper,
                              UserRealmDataMapper userRealmDataMapper) {
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
        this.lastUserRequest = lastUserRequest;
        this.utcSimpleDate = utcSimpleDate;
        this.groupRealmDataMapper = groupRealmDataMapper;
        this.userRealmDataMapper = userRealmDataMapper;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        return this.loginApi
                .requestCode(new LoginEntity(phoneNumber));
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) {
        return this.loginApi
                .loginWithUsername(loginEntity)
                .doOnNext(saveToCacheAccessToken);
    }

    @Override
    public Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity) {
        RegisterEntity registerEntity = new RegisterEntity();
        registerEntity.setDisplayName(displayName);
        registerEntity.setUsername(username);
        registerEntity.setCountryCode(loginEntity.getCountryCode());
        registerEntity.setPassword(loginEntity.getPassword());
        registerEntity.setPhoneNumber(loginEntity.getNationalNumber());
        registerEntity.setPinId(loginEntity.getPinId());

        return this.loginApi
                .register(registerEntity)
                .doOnNext(saveToCacheAccessToken);
    }

    @Override
    public Observable<UserRealm> userInfos(String userId, String filterRecipient) {
        String initRequest = utcSimpleDate.format(new Date());

        return Observable.zip(this.tribeApi.getUserInfos(context.getString(R.string.user_infos,
                !StringUtils.isEmpty(lastUserRequest.get()) ? context.getString(R.string.input_start, lastUserRequest.get()) : "",
                !StringUtils.isEmpty(lastUserRequest.get()) ? context.getString(R.string.input_start, lastUserRequest.get()) : "",
                context.getString(R.string.userfragment_infos))),
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
                })
                .doOnNext(user -> lastUserRequest.set(initRequest))
                .doOnNext(saveToCacheUser);
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

        if (user.getFriendships() != null) {
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
        }

        String req = context.getString(R.string.messages_infos,
                !StringUtils.isEmpty(lastMessageRequest.get()) ? context.getString(R.string.input_start, lastMessageRequest.get()) : "",
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
                    List<MessageRealmInterface> messageRealmListFinal = new ArrayList<MessageRealmInterface>();

                    if (userRealmList != null && userRealmList.size() > 0) {
                        for (MessageRealmInterface message : messageRealmInterfaceList) {
                            if (message.getFrom() != null && StringUtils.isEmpty(message.getFrom().getUsername())) {
                                for (UserRealm userRealm : userRealmList) {
                                    if (userRealm != null && message.getFrom().getId().equals(userRealm.getId())) {
                                        message.setFrom(userRealm);
                                        messageRealmListFinal.add(message);
                                    }
                                }
                            } else {
                                messageRealmListFinal.add(message);
                            }
                        }
                    }

                    return messageRealmInterfaceList;
                })
                .doOnNext(messages -> {
                    if (messages != null && messages.size() > 0) {
                        List<MessageRealmInterface> subMessages = new ArrayList<MessageRealmInterface>();

                        for (MessageRealmInterface message : messages) {
                            if (message.getRecordedAt() != null) subMessages.add(message);
                        }

                        Collections.sort(subMessages, (one, two) -> {
                            if (one == null ^ two == null) {
                                return (one == null) ? -1 : 1;
                            }

                            if (one == null && two == null) return 0;

                            if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
                                return (one.getUpdatedAt() == null) ? -1 : 1;
                            }

                            if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
                                return one.getRecordedAt().before(two.getRecordedAt()) ? -1 : 1;
                            }

                            return one.getUpdatedAt().before(two.getUpdatedAt()) ? -1 : 1;
                        });

                        if (subMessages != null && subMessages.size() > 0) {
                            Date date = new Date(subMessages.get(subMessages.size() - 1).getRecordedAt().getTime() + 60000);
                            this.lastMessageRequest.set(utcSimpleDate.format(date));
                        }
                    }
                })
                .doOnNext(saveToCacheMessages);
    }

    @Override
    public Observable<UserRealm> updateUser(List<Pair<String, String>> values) {
        String pictureUri = "";
        StringBuilder userInputBuilder = new StringBuilder();

        for (Pair<String, String> value : values) {
            if (value.first.equals(UserRealm.TRIBE_SAVE) || value.first.equals(UserRealm.INVISIBLE_MODE)) {
                userInputBuilder.append(value.first + ": " + Boolean.valueOf(value.second));
                userInputBuilder.append(",");
            } else {
                userInputBuilder.append(value.first + ": \"" + value.second + "\"");
                userInputBuilder.append(",");
            }

            if (value.first.equals(UserRealm.PROFILE_PICTURE)) {
                pictureUri = value.second;
            }
        }

        String userInput = userInputBuilder.length() > 0 ? userInputBuilder.substring(0, userInputBuilder.length() - 1) : "";

        if (StringUtils.isEmpty(pictureUri)) {
            String request = context.getString(R.string.user_mutate, userInput, context.getString(R.string.userfragment_infos));
            return this.tribeApi.updateUser(request)
                    .doOnNext(saveToCacheUpdateUser);
        } else {
            String request = context.getString(R.string.user_mutate, userInput, context.getString(R.string.userfragment_infos));
            RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

            File file = new File(Uri.parse(pictureUri).getPath());

            if (!(file != null && file.exists() && file.length() > 0)) {
                InputStream inputStream = null;
                file = FileUtils.getFile(FileUtils.generateIdForMessage(), FileUtils.PHOTO);
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
                    .doOnNext(saveToCacheUpdateUser);
        }
    }

    @Override
    public Observable<List<ContactInterface>> contacts() {
        return Observable.zip(
                rxContacts.getContacts().toList(),
                rxFacebook.requestFriends(),
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
            if (currentUser != null) {
                for (FriendshipRealm fr : currentUser.getFriendships()) {
                    contactList.remove(phones.get(fr.getFriend().getPhone()));
                    phones.remove(fr.getFriend().getPhone());
                    fbIds.remove(fr.getFriend().getFbid());
                }

                phones.remove(currentUser.getPhone());
            }

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
                }

                if (fbIds.size() > 0) {
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
                            if (!StringUtils.isEmpty(userRealm.getFbid()) && userRealm.getFbid().equals(fbId)) {
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
            LookupEntity lookupEntity = entityPair.second != null ? (LookupEntity) entityPair.second : null;

            if (lookupEntity != null && lookupEntity.getLookup() != null && lookupEntity.getLookup().size() > 0) {
                int count = 0;
                Set<String> phonesFound = new HashSet<>();

                for (UserRealm userRealmLookup : lookupEntity.getLookup()) {
                    if (!phonesFound.contains(userRealmLookup.getPhone())) {
                        phonesFound.add(userRealmLookup.getPhone());
                        buffer.append(context.getString(R.string.createFriendship_input, count, userRealmLookup.getId(), context.getString(R.string.friendships_infos)));
                        count++;
                    }
                }

                mutationCreateFriendship = context.getString(R.string.friendship_mutation, buffer.toString(), context.getString(R.string.userfragment_infos));
            }

            return (StringUtils.isEmpty(mutationCreateFriendship) ? Observable.just(new CreateFriendshipEntity()) : tribeApi.createFriendship(mutationCreateFriendship))
                    .map(createFriendshipEntity -> {
                        if (createFriendshipEntity != null && createFriendshipEntity.getNewFriendshipList() != null
                                && createFriendshipEntity.getNewFriendshipList().size() > 0) {
                            UserRealm currentUser = userCache.userInfosNoObs(accessToken.getUserId());
                            currentUser.getFriendships().addAll(createFriendshipEntity.getNewFriendshipList());
                            userCache.put(currentUser);
                        }

                        List<ContactInterface> interfaces = new ArrayList<>(contactList);
                        return interfaces;
                    });
        }).flatMap(listObservable -> listObservable).doOnNext(saveToCacheContacts);
    }

    @Override
    public Observable<Void> howManyFriends() {
        return contactCache.contactsThreadSafe()
                .map(contactABRealmList -> {
                    Map<String, ContactABRealm> phonesHowManyFriends = new HashMap<>();

                    for (ContactABRealm contact : contactABRealmList) {
                        if ((contact.getUsers() == null || contact.getUsers().size() == 0) && contact.getPhones() != null) {
                            for (PhoneRealm phoneRealm : contact.getPhones()) {
                                if (phoneRealm.isInternational()) {
                                    phonesHowManyFriends.put(phoneRealm.getPhone(), contact);
                                }
                            }
                        }
                    }

                    return phonesHowManyFriends;
                })
                .flatMap(phonesHowManyFriends -> {
                    String reqHowManyFriends = null;

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

                    return tribeApi.howManyFriends(reqHowManyFriends);
                }, (phonesHowManyFriends, howManyFriendsResult) -> {
                    int indexHowMany = 0;

                    for (ContactInterface contactInterface : phonesHowManyFriends.values()) {
                        contactInterface.setHowManyFriends(howManyFriendsResult.get(indexHowMany));
                        indexHowMany++;
                    }

                    contactCache.updateHowManyFriends(phonesHowManyFriends.values());

                    return null;
                });
    }

    @Override
    public Observable<SearchResultRealm> findByUsername(String username) {
        SearchResultRealm searchResultInit = new SearchResultRealm();
        searchResultInit.setUsername(username);
        contactCache.insertSearchResult(searchResultInit);

        return this.tribeApi
                .findByUsername(context.getString(R.string.lookup_username, username, context.getString(R.string.userfragment_infos)))
                .doOnError(throwable -> {
                    SearchResultRealm searchResultRealmRet = new SearchResultRealm();
                    searchResultRealmRet.setUsername(searchResultInit.getUsername());
                    searchResultRealmRet.setKey(SEARCH_KEY);
                    searchResultRealmRet.setSearchDone(true);
                    contactCache.insertSearchResult(searchResultRealmRet);
                })
                .map(searchResultRealm -> {
                    SearchResultRealm searchResultRealmRet = new SearchResultRealm();

                    if (searchResultRealm != null) {
                        FriendshipRealm fr = userCache.friendshipForUserId(searchResultRealm.getId());
                        searchResultRealmRet.setFriendshipRealm(fr);
                        searchResultRealmRet.setDisplayName(searchResultRealm.getDisplayName());
                        searchResultRealmRet.setPicture(searchResultRealm.getPicture());
                        searchResultRealmRet.setId(searchResultRealm.getId());
                        searchResultRealmRet.setUsername(searchResultRealm.getUsername());
                    }

                    searchResultRealmRet.setUsername(searchResultInit.getUsername());
                    searchResultRealmRet.setKey(SEARCH_KEY);
                    searchResultRealmRet.setSearchDone(true);
                    return searchResultRealmRet;
                })
                .doOnNext(saveToCacheSearchResult);
    }

    @Override
    public Observable<Boolean> lookupUsername(String username) {
        return loginApi.lookupUsername(new UsernameEntity("", username));
    }

    @Override
    public Observable<List<ContactABRealm>> findByValue(String username) {
        return null;
    }

    @Override
    public Observable<FriendshipRealm> createFriendship(String userId) {
        StringBuffer buffer = new StringBuffer();
        String mutationCreateFriendship = null;

        buffer.append(context.getString(R.string.createFriendship_input, 0, userId, context.getString(R.string.friendships_infos)));
        mutationCreateFriendship = context.getString(R.string.friendship_mutation, buffer.toString(), context.getString(R.string.userfragment_infos));
        return this.tribeApi
                .createFriendship(mutationCreateFriendship)
                .onErrorResumeNext(Observable.just(null))
                .map(createFriendshipEntity -> {
                    FriendshipRealm friendshipRealm = null;

                    if (createFriendshipEntity != null && createFriendshipEntity.getNewFriendshipList() != null
                            && createFriendshipEntity.getNewFriendshipList().size() > 0) {
                        UserRealm currentUser = userCache.userInfosNoObs(accessToken.getUserId());
                        currentUser.getFriendships().addAll(createFriendshipEntity.getNewFriendshipList());
                        userCache.put(currentUser);
                        friendshipRealm = createFriendshipEntity.getNewFriendshipList().get(0);
                    }

                    return friendshipRealm;
                }).doOnNext(friendshipRealm -> {
                    if (friendshipRealm != null) {
                        contactCache.changeSearchResult(friendshipRealm.getFriend().getUsername(), friendshipRealm);
                    }
                });
    }

    @Override
    public Observable<Void> removeFriendship(String friendshipId) {
        StringBuffer buffer = new StringBuffer();
        String mutationRemoveFriendship = null;

        buffer.append(context.getString(R.string.removeFriendship_input, 0, friendshipId, context.getString(R.string.friendships_infos)));
        mutationRemoveFriendship = context.getString(R.string.friendship_mutation, buffer.toString(), "");
        return this.tribeApi.removeFriendship(mutationRemoveFriendship).onErrorResumeNext(throwable -> Observable.empty())
                .doOnNext(aVoid -> userCache.removeFriendship(friendshipId));
    }

    @Override
    public Observable<Void> notifyFBFriends() {
        return this.tribeApi.notifyFBFriends("{ user { id } }");
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
        if (messageRealmList != null && messageRealmList.size() > 0) {
            List<TribeRealm> tribeRealmList = new ArrayList<>();
            List<ChatRealm> chatRealmList = new ArrayList<>();

            for (MessageRealmInterface message : messageRealmList) {
                if (message instanceof TribeRealm) tribeRealmList.add((TribeRealm) message);
                else if (message instanceof ChatRealm) chatRealmList.add((ChatRealm) message);
            }

            CloudUserDataStore.this.tribeCache.insert(tribeRealmList);
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

    private final Action1<SearchResultRealm> saveToCacheSearchResult = searchResultRealm -> {
        if (searchResultRealm != null) {
            CloudUserDataStore.this.contactCache.insertSearchResult(searchResultRealm);
        }
    };

    private final Action1<UserRealm> saveToCacheUpdateUser = userRealm -> {
        if (userRealm != null && userCache != null) {
            UserRealm dbUser = userCache.userInfosNoObs(accessToken.getUserId());
            dbUser.setProfilePicture(userRealm.getProfilePicture());
            dbUser.setUsername(userRealm.getUsername());
            user.setUsername(userRealm.getUsername());
            dbUser.setDisplayName(userRealm.getDisplayName());
            user.setDisplayName(userRealm.getDisplayName());
            userCache.put(dbUser);
            user.setProfilePicture(userRealm.getProfilePicture());
        }
    };

    @Override
    public Observable<GroupRealm> getGroupMembers(String groupId) {
        String request = context.getString(R.string.get_group_members, groupId, context.getString(R.string.userfragment_infos), context.getString(R.string.groupfragment_info));
        return this.tribeApi.getGroupMembers(request)
                .doOnNext(groupRealm -> {
                    List<GroupRealm> dbGroups = userCache.userInfosNoObs(accessToken.getUserId()).getGroups();
                    GroupRealm dbGroup = null;

                    for (GroupRealm group : dbGroups) {
                        if (group.getId().equals(groupId)) {
                            dbGroup = group;
                        }
                    }

                    dbGroup.setMembers(groupRealm.getMembers());
                });
    }


    @Override
    public Observable<GroupRealm> createGroup(String groupName, List<String> memberIds, Boolean isPrivate, String pictureUri) {
        String idList = listToJson(memberIds);
        String privateGroup = isPrivate ? "PRIVATE" : "PUBLIC";
        final String request = context.getString(R.string.create_group, groupName, privateGroup, idList, context.getString(R.string.groupfragment_info));
        if (pictureUri == null) {
            return this.tribeApi.createGroup(request)
                    .doOnNext(groupRealm -> {
                        userCache.createGroup(accessToken.getUserId(), groupRealm.getId(), groupName, memberIds, isPrivate, groupRealm.getPicture());
                    });
        } else {
            RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

            File file = new File(Uri.parse(pictureUri).getPath());

            if (!(file != null && file.exists() && file.length() > 0)) {
                InputStream inputStream = null;
                file = FileUtils.getFile(FileUtils.generateIdForMessage(), FileUtils.PHOTO);
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
            body = MultipartBody.Part.createFormData("group_pic", "group_pic.jpg", requestFile);

            return this.tribeApi.createGroupMedia(query, body)
                    .doOnNext(groupRealm -> {
                        userCache.createGroup(accessToken.getUserId(), groupRealm.getId(), groupName, memberIds, isPrivate, groupRealm.getPicture());
                    });
        }
    }

    @Override
    public Observable<GroupRealm> updateGroup(String groupId, String groupName, String pictureUri) {
        String request = context.getString(R.string.update_group, groupId, groupName, context.getString(R.string.groupfragment_info));
        if (pictureUri == null) {
            return this.tribeApi.updateGroup(request)
                    .doOnError(throwable -> {
                        throwable.printStackTrace();
                    })
                    .doOnNext(groupRealm -> {
                        userCache.updateGroup(groupId, groupName, pictureUri);
                    });
        } else {
            RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

            File file = new File(Uri.parse(pictureUri).getPath());

            if (!(file != null && file.exists() && file.length() > 0)) {
                InputStream inputStream = null;
                file = FileUtils.getFile(FileUtils.generateIdForMessage(), FileUtils.PHOTO);
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
            body = MultipartBody.Part.createFormData("group_pic", "group_pic.jpg", requestFile);

            return this.tribeApi.updateGroupMedia(query, body)
                    .doOnError(throwable -> {
                        throwable.printStackTrace();
                    })
                    .doOnNext(groupRealm -> {
                        userCache.updateGroup(groupId, groupName, pictureUri);
                    });
        }

    }

    @Override
    public Observable<Void> addMembersToGroup(String groupId, List<String> memberIds) {
        String memberIdsJson = listToJson(memberIds);
        String request = context.getString(R.string.add_members_group, groupId, memberIdsJson);
        return this.tribeApi.addMembersToGroup(request)
                .doOnNext(aVoid -> {
                    userCache.addMembersToGroup(groupId, memberIds);
                });
    }

    @Override
    public Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds) {
        String memberIdsJson = listToJson(memberIds);
        String request = context.getString(R.string.remove_members_group, groupId, memberIdsJson);
        return this.tribeApi.removeMembersFromGroup(request)
                .doOnNext(aVoid -> {
                    userCache.removeMembersFromGroup(groupId, memberIds);
                });
    }

    @Override
    public Observable<Void> addAdminsToGroup(String groupId, List<String> memberIds) {
        String memberIdsJson = listToJson(memberIds);
        String request = context.getString(R.string.add_admins_group, groupId, memberIdsJson);
        return this.tribeApi.addAdminsToGroup(request)
                .doOnNext(aVoid -> {
                    userCache.addAdminsToGroup(groupId, memberIds);
                });
    }

    @Override
    public Observable<Void> removeAdminsFromGroup(String groupId, List<String> memberIds) {
        String memberIdsJson = listToJson(memberIds);
        String request = context.getString(R.string.remove_admins_group, groupId, memberIdsJson);
        return this.tribeApi.removeAdminsFromGroup(request)
                .doOnNext(aVoid -> {
                    userCache.removeAdminsFromGroup(groupId, memberIds);
                });
    }

    @Override
    public Observable<Void> removeGroup(String groupId) {
        String request = context.getString(R.string.remove_group, groupId);
        return this.tribeApi.removeGroup(request)
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                })
                .doOnNext(aVoid -> {
                    userCache.removeGroup(groupId);
                });
    }

    @Override
    public Observable<Void> leaveGroup(String groupId) {
        String request = context.getString(R.string.leave_group, groupId);
        return this.tribeApi.leaveGroup(request)
                .doOnNext(aVoid -> {
                    userCache.removeGroup(groupId);
                });
    }

    public String listToJson(List<String> list) {
        String json = "";
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) json += list.get(i);
            else json += list.get(i) + ", ";
        }
        return json;
    }
}

