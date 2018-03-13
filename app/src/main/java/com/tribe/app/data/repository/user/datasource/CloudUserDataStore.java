package com.tribe.app.data.repository.user.datasource;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Pair;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.GrowthApi;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.LookupApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LinkIdResult;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.network.entity.LookupHolder;
import com.tribe.app.data.network.entity.LookupObject;
import com.tribe.app.data.network.entity.RegisterEntity;
import com.tribe.app.data.network.entity.UsernameEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LookupResult;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.tribelivesdk.model.TribeSession;
import io.realm.RealmList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * {@link UserDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudUserDataStore implements UserDataStore {

  private static final int LOOKUP_LIMIT = 200;
  private static final String SEARCH_KEY = "search";

  private final TribeApi tribeApi;
  private final LoginApi loginApi;
  private final LookupApi lookupApi;
  private final GrowthApi growthApi;
  private UserCache userCache = null;
  private LiveCache liveCache = null;
  private final ContactCache contactCache;
  private final RxContacts rxContacts;
  private final RxFacebook rxFacebook;
  private Context context = null;
  private AccessToken accessToken = null;
  private Installation installation = null;
  private @LastSync Preference<Long> lastSync;
  private PhoneUtils phoneUtils;
  private @LookupResult Preference<String> lookupResult;
  private UserRealmDataMapper userRealmDataMapper;

  /**
   * Construct a {@link UserDataStore} based on connections to the api (Cloud).
   *
   * @param userCache A {@link UserCache} to cache data retrieved from the api.
   * @param tribeApi an implementation of the api
   * @param loginApi an implementation of the login api
   * @param context the context
   * @param accessToken the access token
   */
  public CloudUserDataStore(UserCache userCache, ContactCache contactCache, LiveCache liveCache,
      RxContacts rxContacts, RxFacebook rxFacebook, TribeApi tribeApi, LoginApi loginApi,
      LookupApi lookupApi, GrowthApi growthApi, AccessToken accessToken, Installation installation,
      Context context, @LastSync Preference<Long> lastSync, PhoneUtils phoneUtils,
      @LookupResult Preference<String> lookupResult, UserRealmDataMapper userRealmDataMapper) {
    this.userCache = userCache;
    this.contactCache = contactCache;
    this.rxContacts = rxContacts;
    this.rxFacebook = rxFacebook;
    this.tribeApi = tribeApi;
    this.loginApi = loginApi;
    this.lookupApi = lookupApi;
    this.growthApi = growthApi;
    this.context = context;
    this.accessToken = accessToken;
    this.installation = installation;
    this.liveCache = liveCache;
    this.lastSync = lastSync;
    this.phoneUtils = phoneUtils;
    this.lookupResult = lookupResult;
    this.userRealmDataMapper = userRealmDataMapper;
  }

  @Override public Observable<PinRealm> requestCode(String phoneNumber, boolean shouldCall) {
    return this.loginApi.requestCode(new LoginEntity(phoneNumber, shouldCall));
  }

  @Override public Observable<AccessToken> login(LoginEntity loginEntity) {
    if (loginEntity.getFbAccessToken() != null) {
      return loginApi.loginWithFacebook(loginEntity.getFbAccessToken())
          .doOnNext(saveToCacheAccessToken);
    } else if (loginEntity.getPhoneNumber() == null) {
      return loginApi.loginWithAnonymous().doOnNext(saveToCacheAccessToken);
    } else if (loginEntity.getAccessToken() != null) {
      return loginApi.loginWithUsername(loginEntity.getAccessToken(), loginEntity)
          .doOnNext(saveToCacheAccessToken);
    } else {
      return loginApi.loginWithUsername(loginEntity).doOnNext(saveToCacheAccessToken);
    }
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    RegisterEntity registerEntity = new RegisterEntity();
    registerEntity.setDisplayName(displayName);
    registerEntity.setUsername(username);
    registerEntity.setCountryCode(loginEntity.getCountryCode());
    registerEntity.setPassword(loginEntity.getPassword());
    registerEntity.setPhoneNumber(
        loginEntity.getPhoneNumber() != null ? loginEntity.getPhoneNumber()
            .replace(loginEntity.getCountryCode(), "") : null);
    registerEntity.setPinId(loginEntity.getPinId());

    if (loginEntity.getFbAccessToken() != null) {
      return loginApi.registerWithFacebook(loginEntity.getFbAccessToken(), registerEntity)
          .doOnNext(saveToCacheAccessToken);
    } else if (loginEntity.getAccessToken() != null) {
      return loginApi.register(loginEntity.getAccessToken(), registerEntity)
          .doOnNext(saveToCacheAccessToken);
    } else {
      return loginApi.register(registerEntity).doOnNext(saveToCacheAccessToken);
    }
  }

  @Override public Observable<UserRealm> userInfos(String userId) {
    UserRealm userRealm = userCache.userInfosNoObs(accessToken.getUserId());
    String countShortcutsToUpdate = "";

    if (userRealm != null) {
      countShortcutsToUpdate =
          (userRealm.getShortcuts() == null || userRealm.getShortcuts().size() == 0) ? ""
              : "(max : 10)";
    }

    return this.tribeApi.getUserInfos(context.getString(R.string.user_infos, countShortcutsToUpdate,
        context.getString(R.string.userfragment_infos_with_games),
        context.getString(R.string.shortcutFragment_infos),
        context.getString(R.string.roomFragment_infos))).doOnNext(saveToCacheUser);
  }

  @Override public Observable<List<UserRealm>> userInfosList(List<String> userIdsList) {
    String userIdsListFormated = StringUtils.listToJson(userIdsList);
    return this.tribeApi.getUserListInfos(
        context.getString(R.string.lookup_userid, userIdsListFormated,
            context.getString(R.string.userfragment_infos_light)));
  }

  @Override public Observable<Installation> createOrUpdateInstall(String token) {
    if (installation == null || StringUtils.isEmpty(installation.getToken())) {
      String req = context.getString(R.string.installs);
      return this.tribeApi.getInstallList(req).flatMap(installations -> {
        Installation installation = null;

        for (Installation install : installations) {
          if (install.getToken().equals(token)) {
            installation = install;
          }
        }

        return Observable.just(installation);
      }).flatMap(installation -> {
        if (installation == null) {
          return createInstallation(token, null);
        }

        return Observable.just(installation).doOnNext(saveToCacheInstall);
      });
    } else {
      return createInstallation(token, installation);
    }
  }

  private Observable<Installation> createInstallation(String token, Installation installation) {
    TelephonyManager telephonyManager =
        ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
    String operatorName = telephonyManager.getNetworkOperatorName();

    if (!StringUtils.isEmpty(operatorName)) {
      operatorName = operatorName.replaceAll("[^a-zA-Z0-9]+", "");
    }

    String model = Build.MODEL;

    if (!StringUtils.isEmpty(model)) {
      model = model.replaceAll("[^a-zA-Z0-9]+", "").replace("\\\"", "");
    }

    String base = context.getString(R.string.install_base, token, "android", Build.VERSION.RELEASE,
        Build.MANUFACTURER, model, DeviceUtils.getVersionName(context), context.getPackageName(),
        context.getResources().getConfiguration().locale.toString(), operatorName);

    String req =
        installation == null || StringUtils.isEmpty(installation.getToken()) ? context.getString(
            R.string.install_create, base)
            : context.getString(R.string.install_update, installation.getId(), base);
    return this.tribeApi.createOrUpdateInstall(req).onErrorResumeNext(throwable -> {
      this.installation.setToken("");
      this.installation.setId("");
      return createOrUpdateInstall(token);
    }).flatMap(installationRecent -> {
      if (installationRecent == null && this.installation != null && !StringUtils.isEmpty(
          this.installation.getId())) {
        this.installation.setToken("");
        this.installation.setId("");
        return createInstallation(token, this.installation);
      }

      return Observable.just(installationRecent);
    }).doOnNext(installation1 -> installation1.setToken(token)).doOnNext(saveToCacheInstall);
  }

  @Override public Observable<Installation> removeInstall() {
    return this.tribeApi.removeInstall(
        context.getString(R.string.install_remove, installation.getId()));
  }

  @Override public Observable<UserRealm> updateUser(List<Pair<String, String>> values) {
    String pictureUri = "";
    StringBuilder userInputBuilder = new StringBuilder();

    for (Pair<String, String> value : values) {
      if (value.first.equals(UserRealm.TROPHY)) {
        userInputBuilder.append(value.first + ": " + value.second);
        userInputBuilder.append(",");
      } else if (value.first.equals(UserRealm.TRIBE_SAVE)
          || value.first.equals(UserRealm.INVISIBLE_MODE)
          || value.first.equals(UserRealm.PUSH_NOTIF)
          || value.first.equals(UserRealm.MUTE_ONLINE_NOTIF)) {
        userInputBuilder.append(value.first + ": " + Boolean.valueOf(value.second));
        userInputBuilder.append(",");
      } else if (!value.first.equals(UserRealm.FBID) || (!StringUtils.isEmpty(value.second)
          && !value.second.equals("null"))) {
        userInputBuilder.append(value.first + ": \"" + value.second + "\"");
        userInputBuilder.append(",");
      }

      if (value.first.equals(UserRealm.PROFILE_PICTURE)) {
        pictureUri = value.second;
      }
    }

    String userInput =
        userInputBuilder.length() > 0 ? userInputBuilder.substring(0, userInputBuilder.length() - 1)
            : "";

    if (StringUtils.isEmpty(pictureUri)) {
      if (!StringUtils.isEmpty(userInput)) {
        String request = context.getString(R.string.user_mutate, userInput,
            context.getString(R.string.userfragment_infos));
        return this.tribeApi.updateUser(request).doOnNext(saveToCacheUpdateUser);
      } else {
        return Observable.empty();
      }
    } else {
      String request = context.getString(R.string.user_mutate, userInput,
          context.getString(R.string.userfragment_infos));
      RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

      File file = new File(Uri.parse(pictureUri).getPath());

      if (!(file != null && file.exists() && file.length() > 0)) {
        InputStream inputStream = null;
        file = FileUtils.getFile(context, FileUtils.generateIdForMessage(), FileUtils.PHOTO);
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

      return tribeApi.updateUserMedia(query, body).doOnNext(saveToCacheUpdateUser);
    }
  }

  @Override public Observable<LinkIdResult> updateUserFacebook(String accessToken) {
    return accessToken != null ? this.loginApi.linkFacebook(accessToken)
        : this.loginApi.unlinkAuthId(LoginApi.AUTH_ID_FACEBOOK);
  }

  @Override
  public Observable<LinkIdResult> updateUserPhoneNumber(String accessToken, String phoneNumber) {
    if (accessToken != null) {
      String countryCode = "+" + phoneUtils.getCountryCode(phoneNumber);

      return this.loginApi.linkPhoneNumber(accessToken, countryCode,
          phoneNumber.replace(countryCode, ""));
    } else {
      return this.loginApi.unlinkAuthId(LoginApi.AUTH_ID_PHONE_NUMBER);
    }
  }

  @Override public Observable<Void> incrUserTimeInCall(String userId, Long timeInCall) {
    return this.tribeApi.incrUserTimeInCall(
        context.getString(R.string.user_incrUserTimeInCall, Long.toString(timeInCall)))
        .doOnNext(aVoid -> {

          if (userCache != null) {
            userCache.incrUserTimeInCall(userId, timeInCall);
          }
        });
  }

  @Override public Observable<List<ContactInterface>> contacts() {
    return Observable.zip(rxContacts.getContacts(), rxFacebook.requestFriends(),
        rxFacebook.requestInvitableFriends(), this.tribeApi.getUserInfos(
            context.getString(R.string.user_infos_sync,
                context.getString(R.string.userfragment_infos),
                context.getString(R.string.shortcutFragment_infos))).doOnNext(saveToCacheUser),
        (contactABRealmList, contactFBRealmList, contactFBInvitableRealmList, userRealm) -> {
          List<ContactInterface> contactList = new ArrayList<>();

          for (ContactABRealm contactABRealm : contactABRealmList) {
            contactList.add(contactABRealm);
          }

          for (ContactFBRealm contactFBRealm : contactFBRealmList) {
            contactList.add(contactFBRealm);
          }

          for (ContactFBRealm contactFBRealm : contactFBInvitableRealmList) {
            contactList.add(contactFBRealm);
          }

          contactCache.updateFromDB(contactList);

          return contactList;
        }).flatMap(contactList -> {
      if (contactList == null || contactList.size() == 0) return Observable.just(null);

      List<ContactInterface> phones = new ArrayList<>();
      List<ContactInterface> fbIds = new ArrayList<>();

      UserRealm currentUser = userCache.userInfosNoObs(accessToken.getUserId());

      List<LookupEntity> lookupPhones = new ArrayList<>();

      if (contactList.size() > 0) {
        for (ContactInterface contactI : contactList) {
          if (contactI instanceof ContactABRealm) {
            ContactABRealm contactABRealm = (ContactABRealm) contactI;
            boolean shouldAdd = true;
            for (PhoneRealm phoneRealm : contactABRealm.getPhones()) {
              if (phoneRealm.getPhone().trim().replace(" ", "").equals(currentUser.getPhone())) {
                shouldAdd = false;
              }
            }

            if (shouldAdd) {
              phones.add(contactI);
              ContactABRealm ab = (ContactABRealm) contactI;
              lookupPhones.add(new LookupEntity(ab.getPhones().get(0).getPhone(), ab.getFirstName(),
                  ab.getLastName(),
                  (ab.getEmails() != null && ab.getEmails().size() > 0) ? ab.getEmails().get(0)
                      : null));
            }
          } else if (contactI instanceof ContactFBRealm) {
            ContactFBRealm contactFBRealm = (ContactFBRealm) contactI;
            fbIds.add(contactFBRealm);
          }
        }
      }

      StringBuffer buffer = new StringBuffer();

      if (fbIds.size() > 0) {
        StringBuilder result = new StringBuilder();

        int count = 0, loopCount = 0;
        for (ContactInterface ci : fbIds) {
          ContactFBRealm fbRealm = (ContactFBRealm) ci;
          result.append("\"" + fbRealm.getId() + "\"");
          result.append(",");
          count++;

          if (count % LOOKUP_LIMIT == 0) {
            buffer.append(context.getString(R.string.lookup_facebook, loopCount,
                result.length() > 0 ? result.substring(0, result.length() - 1) : ""));
            loopCount++;
            result = new StringBuilder();
          }
        }

        buffer.append(context.getString(R.string.lookup_facebook, loopCount,
            result.length() > 0 ? result.substring(0, result.length() - 1) : ""));
      }

      String regionCode = phoneUtils.getRegionCodeForNumber(currentUser.getPhone());
      if (StringUtils.isEmpty(regionCode)) {
        regionCode = Locale.getDefault().getCountry();
      }

      String fbRequests = buffer.toString();
      String reqLookup = context.getString(R.string.lookup, buffer.toString(),
          context.getString(R.string.userfragment_infos));

      return Observable.zip(lookupApi.lookup(regionCode, lookupPhones)
              .doOnNext(
                  lookupObjects -> PreferencesUtils.saveLookupAsJson(lookupObjects, lookupResult)),
          StringUtils.isEmpty(fbRequests) ? Observable.just(null)
              : tribeApi.lookupFacebook(reqLookup), (lookupObjects, lookupFBResult) -> {
            LookupHolder lookupHolder = new LookupHolder();
            lookupHolder.setContactPhoneList(phones);
            lookupHolder.setLookupObjectList(lookupObjects);
            lookupObjects.get(3).getFriends(); // SOEF

            if (lookupFBResult != null && lookupFBResult.getLookup() != null) {
              for (int i = 0; i < lookupFBResult.getLookup().size(); i++) {
                UserRealm user = lookupFBResult.getLookup().get(i);
                ContactInterface ci = fbIds.get(i);

                if (user != null) {
                  ci.addUser(user);
                  if (!ci.isNew() && lastSync.get() != null && lastSync.get() > 0) {
                    ci.setNew(user.getCreatedAt().getTime() > lastSync.get());
                  }
                }
              }
            }

            lookupHolder.setContactFBList(fbIds);

            return lookupHolder;
          });
    }, (contactList, lookupHolder) -> lookupHolder).flatMap(lookupHolder -> {
      StringBuilder resultLookupUserIds = new StringBuilder();

      if (lookupHolder != null && lookupHolder.getLookupObjectList() != null) {
        Set<String> userIdSet = new HashSet<>();
        for (LookupObject lookupObject : lookupHolder.getLookupObjectList()) {
          if (lookupObject != null
              && !StringUtils.isEmpty(lookupObject.getUserId())
              && lookupObject.getHowManyFriends() == 0) {
            if (!userIdSet.contains(lookupObject.getUserId())) {
              resultLookupUserIds.append("\"" + lookupObject.getUserId() + "\"");
              resultLookupUserIds.append(",");
              userIdSet.add(lookupObject.getUserId());
            }
          }
        }
      }

      if (resultLookupUserIds.length() == 0) {
        return Observable.just(new ArrayList<UserRealm>());
      } else {
        return this.tribeApi.getUserListInfos(context.getString(R.string.lookup_userid,
            resultLookupUserIds.substring(0, resultLookupUserIds.length() - 1),
            context.getString(R.string.userfragment_infos)));
      }
    }, (lookupHolder, lookupUsers) -> {
      if (lookupHolder != null && lookupUsers != null && lookupHolder.getLookupObjectList() != null) {
        List<LookupObject> listLookup = lookupHolder.getLookupObjectList();
        for (int i = 0; i < listLookup.size(); i++) {
          LookupObject lookupObject = listLookup.get(i);
          if (lookupObject != null && !StringUtils.isEmpty(lookupObject.getUserId())) {
            for (UserRealm user : lookupUsers) {
              if (user != null && lookupObject.getUserId().equals(user.getId())) {
                lookupObject.setUserRealm(user);
              }
            }
          }

          if (lookupObject != null) {
            ContactInterface ci = lookupHolder.getContactPhoneList().get(i);

            if (lookupObject.getUserRealm() != null) {
              ci.addUser(lookupObject.getUserRealm());
              if (!ci.isNew() && lastSync.get() != null && lastSync.get() > 0) {
                ci.setNew(lookupObject.getUserRealm().getCreatedAt().getTime() > lastSync.get());
              }
            } else {
              ci.setHowManyFriends(lookupObject.getHowManyFriends());
            }
            ci.setFriends(lookupObject.getFriends()); // SOEF
            ci.setPhone(lookupObject.getPhone());
          }
        }

        return lookupHolder.getContactAllList();
      }

      return new ArrayList<ContactInterface>();
    }).doOnNext(saveToCacheContacts).doOnError(throwable -> Timber.d(throwable));
  }

  @Override public Observable<List<ContactInterface>> contactsFB() {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contactsFBInvite() {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contactsOnApp() {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contactsToInvite() {
    return null;
  }

  @Override public Observable<SearchResultRealm> findByUsername(String username) {
    SearchResultRealm searchResultInit = new SearchResultRealm();
    searchResultInit.setUsername(username);
    contactCache.insertSearchResult(searchResultInit);

    return this.tribeApi.findByUsername(context.getString(R.string.lookup_username, username,
        context.getString(R.string.userfragment_infos))).doOnError(throwable -> {
      SearchResultRealm searchResultRealmRet = new SearchResultRealm();
      searchResultRealmRet.setUsername(searchResultInit.getUsername());
      searchResultRealmRet.setKey(SEARCH_KEY);
      searchResultRealmRet.setSearchDone(true);
      contactCache.insertSearchResult(searchResultRealmRet);
    }).map(searchResultRealm -> {
      SearchResultRealm searchResultRealmRet = new SearchResultRealm();

      if (searchResultRealm != null) {
        ShortcutRealm sh = userCache.shortcutForUserId(searchResultRealm.getId());
        searchResultRealmRet.setShortcutRealm(sh);
        searchResultRealmRet.setDisplayName(searchResultRealm.getDisplayName());
        searchResultRealmRet.setPicture(searchResultRealm.getPicture());
        searchResultRealmRet.setId(searchResultRealm.getId());
        searchResultRealmRet.setUsername(searchResultRealm.getUsername());
        searchResultRealmRet.setInvisibleMode(searchResultRealm.isInvisibleMode());
      }

      searchResultRealmRet.setUsername(searchResultInit.getUsername());
      searchResultRealmRet.setKey(SEARCH_KEY);
      searchResultRealmRet.setSearchDone(true);
      return searchResultRealmRet;
    }).doOnNext(saveToCacheSearchResult);
  }

  @Override public Observable<Boolean> lookupUsername(String username) {
    return loginApi.lookupUsername(new UsernameEntity("", username));
  }

  @Override public Observable<List<ContactABRealm>> findByValue(String username) {
    return null;
  }

  @Override public Observable<Void> notifyFBFriends() {
    return null;
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
      this.accessToken.setAccessExpiresAt(accessToken.getAccessExpiresAt());

      CloudUserDataStore.this.userCache.put(accessToken);
    }
  };

  private final Action1<UserRealm> saveToCacheUser = userRealm -> {
    if (userRealm != null) {
      liveCache.clearLive();

      CloudUserDataStore.this.userCache.put(userRealm);
      CloudUserDataStore.this.userCache.putShortcuts(userRealm.getShortcuts());

      if (userRealm.getShortcuts() != null) {
        for (ShortcutRealm shortcutRealm : userRealm.getShortcuts()) {
          if (shortcutRealm.isOnline()) {
            liveCache.putOnline(shortcutRealm.getId());
          } else {
            liveCache.removeOnline(shortcutRealm.getId());
          }

          if (shortcutRealm.isSingle()) {
            UserRealm member = shortcutRealm.getSingleFriend();
            if (member.isPlaying() != null && member.isPlaying().getGame_id() != null) {
              liveCache.putPlaying(member.getId(), member.isPlaying());
            } else {
              liveCache.removePlaying(member.getId());
            }
          }
        }
      }

      if (userRealm.getInvites() != null) {
        for (Invite newInvite : userRealm.getInvites()) {
          boolean shouldAdd = true;

          if (newInvite.getRoom() == null) shouldAdd = false;

          if (shouldAdd) {
            List<String> roomUserIds = newInvite.getRoom().getUserIds();
            roomUserIds.remove(accessToken.getUserId());
            Shortcut shortcut = null;

            ShortcutRealm shortcutRealm = userCache.shortcutForUserIdsNoObs(
                roomUserIds.toArray(new String[roomUserIds.size()]));

            if (shortcutRealm != null) {
              shortcut = userRealmDataMapper.getShortcutRealmDataMapper().transform(shortcutRealm);
              newInvite.setShortcut(shortcut);
              liveCache.putLive(shortcut.getId());
            }

            for (User user : newInvite.getRoom().getLiveUsers()) {
              liveCache.putLive(user.getId());
            }

            liveCache.putInvite(newInvite);
          }
        }
      }
    }
  };

  private final Action1<List<ShortcutRealm>> saveToCacheShortcuts = shortcutRealmList -> {
    if (shortcutRealmList != null) {
      CloudUserDataStore.this.userCache.putShortcuts(shortcutRealmList);
    }
  };

  private final Action1<Installation> saveToCacheInstall = installRealm -> {
    if (installRealm != null) {
      if (this.installation == null) {
        this.installation = new Installation();
      }

      this.installation.setId(installRealm.getId());
      this.installation.setToken(installRealm.getToken());
      CloudUserDataStore.this.userCache.put(installRealm);
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
      userCache.updateCurrentUser(userRealm);
    }
  };

  @Override public Observable<String> getHeadDeepLink(String url) {
    return tribeApi.getHeadDeepLink(url).flatMap(response -> {
      if (response != null
          && response.raw() != null
          && response.raw().priorResponse() != null
          && response.raw().priorResponse().networkResponse() != null
          && response.raw().priorResponse().networkResponse().request() != null) {
        String result = response.raw().priorResponse().networkResponse().request().url().toString();
        return Observable.just(result);
      }

      return Observable.just("");
    });
  }

  @Override public Observable<RecipientRealmInterface> getRecipientInfos(String recipientId) {
    return null;
  }

  private void clearGroupAvatar(String groupId) {
    File groupAvatarFile = FileUtils.getAvatarForGroupId(context, groupId, FileUtils.PHOTO);
    if (groupAvatarFile != null && groupAvatarFile.exists()) groupAvatarFile.delete();
  }

  @Override public Observable<Void> sendInvitations() {
    return growthApi.sendInvitations(PreferencesUtils.getLookup(lookupResult))
        .doOnNext(aVoid -> lookupResult.set(""));
  }

  @Override public Observable<Boolean> reportUser(String userId, String imageUrl) {
    final String request = context.getString(R.string.mutation,
        context.getString(R.string.reportUser, userId, imageUrl));

    return this.tribeApi.reportUser(request);
  }

  @Override public Observable<ShortcutRealm> createShortcut(String[] userIds) {
    String params = "";

    List<String> filteredUserIds = new ArrayList<>();

    if (userIds.length > 0) {
      for (String id : userIds) {
        if (!id.equals(accessToken.getUserId()) && !id.startsWith(TribeSession.WEB_ID)) {
          filteredUserIds.add(id);
        }
      }
    }

    if (filteredUserIds.size() == 0) return Observable.empty();

    params += params.length() == 0 ? "( " : "";
    params += context.getString(R.string.createShortcut_userIds,
        StringUtils.arrayToJson(filteredUserIds.toArray(new String[filteredUserIds.size()])));

    if (params.length() > 0) params += " )";

    String body = context.getString(R.string.createShortcut, params);

    final String request = context.getString(R.string.mutation, body) + "\n" + context.getString(
        R.string.shortcutFragment_infos) + "\n" + context.getString(
        R.string.userfragment_infos_light);
    return this.tribeApi.createShortcut(request).doOnNext(shortcutRealm -> {
      if (shortcutRealm != null) {
        userCache.addShortcut(shortcutRealm);
        contactCache.changeSearchResult(shortcutRealm.getSingleFriend().getUsername(),
            shortcutRealm);
      }
    });
  }

  @Override public Observable<ShortcutRealm> updateShortcut(String shortcutId,
      List<Pair<String, String>> values) {
    String pictureUri = "";
    StringBuilder shortcutInputBuilder = new StringBuilder();

    if (values.size() == 1 && values.get(0).first.equals(ShortcutRealm.LEAVE_ONLINE_UNTIL)) {
      Date date = new Date(Long.parseLong(values.get(0).second));
      userCache.updateShortcutLeaveOnlineUntil(shortcutId, date);
      return Observable.empty();
    }

    for (Pair<String, String> value : values) {
      if (value.first.equals(ShortcutRealm.READ)) userCache.decrementBadge();
      if (ShortcutRealm.isKeyABool(value.first)) {
        shortcutInputBuilder.append(value.first + ": " + Boolean.valueOf(value.second));
        shortcutInputBuilder.append(",");
      } else if (ShortcutRealm.isKeyEnum(value.first)) {
        shortcutInputBuilder.append(value.first + ": " + value.second);
        shortcutInputBuilder.append(",");
      } else if (!StringUtils.isEmpty(value.second) && !value.second.equals("null")) {
        shortcutInputBuilder.append(value.first + ": \"" + value.second + "\"");
        shortcutInputBuilder.append(",");
      }

      if (value.first.equals(ShortcutRealm.PICTURE)) {
        pictureUri = value.second;
      }
    }

    String shortcutInput = shortcutInputBuilder.length() > 0 ? shortcutInputBuilder.substring(0,
        shortcutInputBuilder.length() - 1) : "";

    if (StringUtils.isEmpty(pictureUri)) {
      if (!StringUtils.isEmpty(shortcutInput)) {
        String requestBody = context.getString(R.string.updateShortcut, shortcutId, shortcutInput);

        final String request =
            context.getString(R.string.mutation, requestBody) + "\n" + context.getString(
                R.string.shortcutFragment_infos) + "\n" + context.getString(
                R.string.userfragment_infos_light);

        return this.tribeApi.updateShortcut(request)
            .doOnNext(shortcutRealm -> userCache.updateShortcut(shortcutRealm));
      } else {
        return Observable.empty();
      }
    } else {
      String requestBody = context.getString(R.string.updateShortcut, shortcutId, shortcutInput);

      final String request =
          context.getString(R.string.mutation, requestBody) + "\n" + context.getString(
              R.string.shortcutFragment_infos) + "\n" + context.getString(
              R.string.userfragment_infos_light);

      RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

      File file = new File(Uri.parse(pictureUri).getPath());

      if (!(file != null && file.exists() && file.length() > 0)) {
        InputStream inputStream = null;
        file = FileUtils.getFile(context, FileUtils.generateIdForMessage(), FileUtils.PHOTO);
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
      body = MultipartBody.Part.createFormData("shortcut_pic", "picture.jpg", requestFile);

      return tribeApi.updateShortcutMedia(query, body)
          .doOnNext(shortcutRealm -> userCache.updateShortcut(shortcutRealm));
    }
  }

  @Override public Observable<Void> removeShortcut(String shortcutId) {
    String body = context.getString(R.string.removeShortcut, shortcutId);

    final String request = context.getString(R.string.mutation, body);

    return this.tribeApi.removeShortcut(request)
        .doOnNext(aVoid -> userCache.removeShortcut(shortcutId));
  }

  @Override public Observable<List<ShortcutRealm>> singleShortcuts() {
    return null;
  }

  @Override public Observable<List<ShortcutRealm>> shortcuts() {
    return null;
  }

  @Override public Observable<ShortcutRealm> shortcutForUserIds(String... userIds) {
    return null;
  }

  @Override public Observable<List<ShortcutRealm>> blockedShortcuts() {
    return this.tribeApi.getUserInfos(context.getString(R.string.shortcuts_blocked,
        context.getString(R.string.userfragment_infos),
        context.getString(R.string.shortcutFragment_infos))).map(userRealm -> {
      if (userRealm != null) {
        return userRealm.getShortcuts();
      } else {
        return new RealmList<ShortcutRealm>();
      }
    }).doOnNext(saveToCacheShortcuts);
  }

  @Override public List<Invite> invites() {
    return null;
  }

  @Override public Observable<String> getRandomBannedUntil() {
    return null;
  }
}
