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
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.network.entity.LookupObject;
import com.tribe.app.data.network.entity.RegisterEntity;
import com.tribe.app.data.network.entity.UsernameEntity;
import com.tribe.app.data.network.util.LookupApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action1;

/**
 * {@link UserDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudUserDataStore implements UserDataStore {

  private static final int LOOKUP_LIMIT = 200;
  private static final String SEARCH_KEY = "search";

  private final TribeApi tribeApi;
  private final LoginApi loginApi;
  private final LookupApi lookupApi;
  private UserCache userCache = null;
  private LiveCache liveCache = null;
  private final ContactCache contactCache;
  private final RxContacts rxContacts;
  private final RxFacebook rxFacebook;
  private final Context context;
  private AccessToken accessToken = null;
  private Installation installation = null;
  private @LastSync Preference<Long> lastSync;
  private PhoneUtils phoneUtils;

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
      LookupApi lookupApi, AccessToken accessToken, Installation installation, Context context,
      @LastSync Preference<Long> lastSync, PhoneUtils phoneUtils) {
    this.userCache = userCache;
    this.contactCache = contactCache;
    this.rxContacts = rxContacts;
    this.rxFacebook = rxFacebook;
    this.tribeApi = tribeApi;
    this.loginApi = loginApi;
    this.lookupApi = lookupApi;
    this.context = context;
    this.accessToken = accessToken;
    this.installation = installation;
    this.liveCache = liveCache;
    this.lastSync = lastSync;
    this.phoneUtils = phoneUtils;
  }

  @Override public Observable<PinRealm> requestCode(String phoneNumber) {
    return this.loginApi.requestCode(new LoginEntity(phoneNumber));
  }

  @Override public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) {
    return this.loginApi.loginWithUsername(loginEntity).doOnNext(saveToCacheAccessToken);
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    RegisterEntity registerEntity = new RegisterEntity();
    registerEntity.setDisplayName(displayName);
    registerEntity.setUsername(username);
    registerEntity.setCountryCode(loginEntity.getCountryCode());
    registerEntity.setPassword(loginEntity.getPassword());
    registerEntity.setPhoneNumber(
        loginEntity.getPhoneNumber().replace(loginEntity.getCountryCode(), ""));
    registerEntity.setPinId(loginEntity.getPinId());

    return this.loginApi.register(registerEntity).doOnNext(saveToCacheAccessToken);
  }

  @Override public Observable<UserRealm> userInfos(String userId) {
    return this.tribeApi.getUserInfos(
        context.getString(R.string.user_infos, context.getString(R.string.userfragment_infos),
            context.getString(R.string.friendshipfragment_info),
            context.getString(R.string.groupfragment_info_members),
            context.getString(R.string.membershipfragment_info))).doOnNext(saveToCacheUser);
  }

  @Override public Observable<List<UserRealm>> userInfosList(List<String> userIdsList) {
    String userIdsListFormated = listToJson(userIdsList);
    return this.tribeApi.getUserListInfos(
        context.getString(R.string.lookup_userid, userIdsListFormated,
            context.getString(R.string.userfragment_infos)));
  }

  @Override public Observable<List<FriendshipRealm>> friendships() {
    return null;
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
      if (value.first.equals(UserRealm.TRIBE_SAVE)
          || value.first.equals(UserRealm.INVISIBLE_MODE)
          || value.first.equals(UserRealm.PUSH_NOTIF)) {
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

  @Override public Observable<List<ContactInterface>> contacts() {
    return Observable.zip(rxContacts.getContacts().toList(), rxFacebook.requestFriends(),
        (contactABRealmList, contactFBRealmList) -> {
          List<ContactInterface> contactList = new ArrayList<>();

          for (ContactABRealm contactABRealm : contactABRealmList) {
            contactList.add(contactABRealm);
          }

          for (ContactFBRealm contactFBRealm : contactFBRealmList) {
            contactList.add(contactFBRealm);
          }

          contactCache.updateFromDB(contactList);

          return contactList;
        }).flatMap(contactList -> {
      if (contactList == null || contactList.size() == 0) return Observable.empty();

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
              if (phoneRealm.getPhone().equals(currentUser.getPhone())) {
                shouldAdd = false;
              }
            }

            if (shouldAdd) {
              phones.add(contactI);
              ContactABRealm ab = (ContactABRealm) contactI;
              lookupPhones.add(new LookupEntity(ab.getPhones().get(0).getPhone(), null, null));
            }
          }
        }
      }

      String regionCode = phoneUtils.getRegionCodeForNumber(currentUser.getPhone());

      return lookupApi.lookup(regionCode, lookupPhones);
    }, (contactList, lookupList) -> {
      return new Pair<>(contactList, lookupList);
    }).flatMap(pairContactLookupResult -> {
      StringBuilder resultLookupUserIds = new StringBuilder();

      for (LookupObject lookupObject : pairContactLookupResult.second) {
        if (lookupObject != null && !StringUtils.isEmpty(lookupObject.getUserId())) {
          resultLookupUserIds.append("\"" + lookupObject.getUserId() + "\"");
          resultLookupUserIds.append(",");
        }
      }

      return this.tribeApi.getUserListInfos(context.getString(R.string.lookup_userid,
          resultLookupUserIds.length() > 0 ? resultLookupUserIds.substring(0,
              resultLookupUserIds.length() - 1) : "",
          context.getString(R.string.userfragment_infos)));
    }, (pairContactLookupResult, lookupUsers) -> {
      List<LookupObject> listLookup = pairContactLookupResult.second;
      for (int i = 0; i < listLookup.size(); i++) {
        LookupObject lookupObject = listLookup.get(i);
        if (lookupObject != null && !StringUtils.isEmpty(lookupObject.getUserId())) {
          for (UserRealm user : lookupUsers) {
            if (lookupObject.getUserId().equals(user.getId())) lookupObject.setUserRealm(user);
          }
        }

        if (lookupObject != null) {
          ContactInterface ci = pairContactLookupResult.first.get(i);

          if (lookupObject.getUserRealm() != null) {
            ci.addUser(lookupObject.getUserRealm());
            if (!ci.isNew() && lastSync.get() != null && lastSync.get() > 0) {
              ci.setNew(lookupObject.getUserRealm().getCreatedAt().getTime() > lastSync.get());
            }
          } else {
            ci.setHowManyFriends(lookupObject.getHowManyFriends());
          }

          ci.setPhone(lookupObject.getPhone());
        }
      }

      return pairContactLookupResult.first;
    }).doOnNext(saveToCacheContacts);
  }

  @Override public Observable<List<ContactInterface>> contactsFB() {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contactsOnApp() {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contactsToInvite() {
    return null;
  }

  @Override public Observable<Void> howManyFriends() {
    return contactCache.contactsThreadSafe().map(contactABRealmList -> {
      Map<String, ContactABRealm> phonesHowManyFriends = new HashMap<>();

      for (ContactABRealm contact : contactABRealmList) {
        if ((contact.getUsers() == null || contact.getUsers().size() == 0)
            && contact.getPhones() != null) {
          for (PhoneRealm phoneRealm : contact.getPhones()) {
            if (phoneRealm.isInternational()) {
              phonesHowManyFriends.put(phoneRealm.getPhone(), contact);
            }
          }
        }
      }

      return phonesHowManyFriends;
    }).flatMap(phonesHowManyFriends -> {
      List<String> requests = new ArrayList<>();

      if (phonesHowManyFriends.size() > 0) {
        StringBuilder resultHowManyFriends = new StringBuilder();

        int count = 0;
        for (String phone : phonesHowManyFriends.keySet()) {
          resultHowManyFriends.append("\"" + phone + "\"");
          resultHowManyFriends.append(",");
          count++;

          if (count % LOOKUP_LIMIT == 0) {
            String req = context.getString(R.string.howManyFriends_part, 0,
                resultHowManyFriends.length() > 0 ? resultHowManyFriends.substring(0,
                    resultHowManyFriends.length() - 1) : "");
            requests.add(context.getString(R.string.mutation, req));
            resultHowManyFriends = new StringBuilder();
          }
        }

        String req = context.getString(R.string.howManyFriends_part, 0,
            resultHowManyFriends.length() > 0 ? resultHowManyFriends.substring(0,
                resultHowManyFriends.length() - 1) : "");
        requests.add(context.getString(R.string.mutation, req));
      }

      if (requests.size() > 0) {
        return Observable.just(requests)
            .flatMap(strings -> Observable.from(strings))
            .flatMap(s -> tribeApi.howManyFriends(s))
            .toList()
            .map(howManyFriendsResult -> {
              for (List<Integer> howManyFriends : howManyFriendsResult) {
                int indexHowMany = 0;

                if (howManyFriends != null && howManyFriends.size() > 0) {
                  for (ContactInterface contactInterface : phonesHowManyFriends.values()) {
                    if (howManyFriends.size() > indexHowMany) {
                      contactInterface.setHowManyFriends(howManyFriends.get(indexHowMany));
                      indexHowMany++;
                    }
                  }
                }
              }

              contactCache.updateHowManyFriends(phonesHowManyFriends.values());

              return null;
            });
      }

      return Observable.just(new ArrayList<Integer>());
    }, (phonesHowManyFriends, howManyFriendsResult) -> null);
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
        FriendshipRealm fr = userCache.friendshipForUserId(searchResultRealm.getId());
        searchResultRealmRet.setFriendshipRealm(fr);
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

  @Override public Observable<FriendshipRealm> createFriendship(String userId) {
    StringBuffer buffer = new StringBuffer();
    String mutationCreateFriendship = null;

    buffer.append(context.getString(R.string.createFriendship_input, 0, userId,
        context.getString(R.string.friendships_infos)));
    mutationCreateFriendship = context.getString(R.string.friendship_mutation, buffer.toString(),
        context.getString(R.string.userfragment_infos));
    return this.tribeApi.createFriendship(mutationCreateFriendship)
        .onErrorResumeNext(Observable.just(null))
        .map(createFriendshipEntity -> {
          FriendshipRealm friendshipRealm = null;

          if (createFriendshipEntity != null
              && createFriendshipEntity.getNewFriendshipList() != null
              && createFriendshipEntity.getNewFriendshipList().size() > 0) {
            friendshipRealm = createFriendshipEntity.getNewFriendshipList().get(0);
            userCache.addFriendship(friendshipRealm);
          }

          return friendshipRealm;
        })
        .doOnNext(friendshipRealm -> {
          if (friendshipRealm != null) {
            contactCache.changeSearchResult(friendshipRealm.getFriend().getUsername(),
                friendshipRealm);
          }
        });
  }

  public Observable<Void> createFriendships(String... userIds) {
    StringBuffer buffer = new StringBuffer();
    String mutationCreateFriendship = null;

    int count = 0;
    for (String id : userIds) {
      buffer.append(context.getString(R.string.createFriendship_input, count, id,
          context.getString(R.string.friendships_infos)));
      count++;
    }

    mutationCreateFriendship = context.getString(R.string.friendship_mutation, buffer.toString(),
        context.getString(R.string.userfragment_infos));

    return (StringUtils.isEmpty(mutationCreateFriendship) ? Observable.just(
        new CreateFriendshipEntity())
        : tribeApi.createFriendship(mutationCreateFriendship)).onErrorResumeNext(
        Observable.just(null)).doOnNext(createFriendshipEntity -> {

      if (createFriendshipEntity != null
          && createFriendshipEntity.getNewFriendshipList() != null
          && createFriendshipEntity.getNewFriendshipList().size() > 0) {
        for (FriendshipRealm fr : createFriendshipEntity.getNewFriendshipList()) {
          userCache.addFriendship(fr);
        }
      }
    }).map(createFriendshipEntity -> null);
  }

  @Override public Observable<Void> removeFriendship(String friendshipId) {
    StringBuffer buffer = new StringBuffer();
    String mutationRemoveFriendship = null;

    buffer.append(context.getString(R.string.removeFriendship_input, 0, friendshipId,
        context.getString(R.string.friendships_infos)));
    mutationRemoveFriendship =
        context.getString(R.string.friendship_mutation, buffer.toString(), "");
    return this.tribeApi.removeFriendship(mutationRemoveFriendship)
        .onErrorResumeNext(throwable -> Observable.empty())
        .doOnNext(aVoid -> userCache.removeFriendship(friendshipId));
  }

  @Override public Observable<Void> notifyFBFriends() {
    return this.tribeApi.notifyFBFriends(
        context.getString(R.string.notify_facebook, context.getString(R.string.facebook_app_id),
            com.facebook.AccessToken.getCurrentAccessToken().getToken()));
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

      if (userRealm.getMemberships() != null) {
        for (MembershipRealm membershipRealm : userRealm.getMemberships()) {
          if (membershipRealm.getGroup().isLive()) {
            liveCache.putLive(membershipRealm.getSubId());
          } else {
            liveCache.removeLive(membershipRealm.getSubId());
          }
        }
      }

      if (userRealm.getFriendships() != null) {
        for (FriendshipRealm friendshipRealm : userRealm.getFriendships()) {
          if (friendshipRealm.isLive()) {
            liveCache.putLive(friendshipRealm.getId());
          } else {
            liveCache.removeLive(friendshipRealm.getId());
          }
        }
      }

      if (userRealm.getInvites() != null) {
        for (Invite newInvite : userRealm.getInvites()) {
          boolean shouldAdd = true;
          if (newInvite.getFriendships() != null) {
            for (Friendship friendship : newInvite.getFriendships()) {
              if (friendship.getSubId().equals(accessToken.getUserId())) {
                shouldAdd = false;
              }
            }
          }

          if (shouldAdd) {
            liveCache.putInvite(newInvite);
          }
        }
      }
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

  @Override public Observable<GroupRealm> getGroupMembers(String groupId) {
    String request = context.getString(R.string.get_group_members, groupId,
        context.getString(R.string.userfragment_infos),
        context.getString(R.string.groupfragment_info));
    return this.tribeApi.getGroupMembers(request).doOnNext(groupRealm -> {
      List<MembershipRealm> dbMemberships =
          userCache.userInfosNoObs(accessToken.getUserId()).getMemberships();
      GroupRealm dbGroup = null;

      for (MembershipRealm membershipRealm : dbMemberships) {
        if (membershipRealm.getGroup().getId().equals(groupId)) {
          dbGroup = membershipRealm.getGroup();
        }
      }

      dbGroup.setMembers(groupRealm.getMembers());
    });
  }

  @Override public Observable<GroupRealm> getGroupInfos(String groupId) {
    String request = context.getString(R.string.get_group_infos, groupId,
        context.getString(R.string.groupfragment_info_members),
        context.getString(R.string.userfragment_infos));

    return this.tribeApi.getGroupInfos(request)
        .doOnNext(groupRealm -> userCache.updateGroup(groupRealm, true));
  }

  @Override public Observable<MembershipRealm> getMembershipInfos(String membershipId) {
    return null;
  }

  @Override public Observable<MembershipRealm> createGroup(GroupEntity groupEntity) {
    String idList = listToJson(groupEntity.getMembersId());

    String members = "";

    if (groupEntity.getMembersId() != null && groupEntity.getMembersId().size() > 0) {
      members = context.getString(R.string.create_group_members, idList);
    }

    final String request = context.getString(R.string.create_group, groupEntity.getName(),
        !StringUtils.isEmpty(members) ? members : "",
        context.getString(R.string.groupfragment_info));

    return this.tribeApi.createGroup(request)
        .doOnNext(groupRealm -> userCache.insertGroup(groupRealm))
        .flatMap(groupRealm -> createMembership(groupRealm.getId()),
            (groupRealm, newMembership) -> newMembership);
  }

  @Override
  public Observable<GroupRealm> updateGroup(String groupId, List<Pair<String, String>> values) {
    String pictureUri = "";
    StringBuilder groupInputBuilder = new StringBuilder();

    for (Pair<String, String> value : values) {
      if (value.first.equals(GroupRealm.PICTURE)) {
        pictureUri = value.second;
      } else if (!StringUtils.isEmpty(value.second) && !value.second.equals("null")) {
        groupInputBuilder.append(value.first + ": \"" + value.second + "\"");
        groupInputBuilder.append(",");
      }
    }

    String groupInput = groupInputBuilder.length() > 0 ? groupInputBuilder.substring(0,
        groupInputBuilder.length() - 1) : "";

    String request = context.getString(R.string.update_group, groupId, groupInput,
        context.getString(R.string.groupfragment_info));

    if (StringUtils.isEmpty(pictureUri)) {
      if (!StringUtils.isEmpty(groupInput)) {
        return this.tribeApi.updateGroup(request)
            .doOnNext(groupRealm -> userCache.updateGroup(groupRealm, false));
      } else {
        return Observable.empty();
      }
    } else {
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

      RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
      MultipartBody.Part body =
          MultipartBody.Part.createFormData("group_pic", "group_pic.jpg", requestFile);

      return tribeApi.updateGroupMedia(query, body)
          .doOnNext(groupRealm -> userCache.updateGroup(groupRealm, false));
    }
  }

  @Override public Observable<MembershipRealm> updateMembership(String membershipId,
      List<Pair<String, String>> values) {
    StringBuilder membershipInputBuilder = new StringBuilder();

    for (Pair<String, String> value : values) {
      if (value.first.equals(MembershipRealm.MUTE)) {
        membershipInputBuilder.append(value.first + ": " + Boolean.valueOf(value.second));
        membershipInputBuilder.append(",");
      } else if (!StringUtils.isEmpty(value.second) && !value.second.equals("null")) {
        membershipInputBuilder.append(value.first + ": \"" + value.second + "\"");
        membershipInputBuilder.append(",");
      }
    }

    String membershipInput =
        membershipInputBuilder.length() > 0 ? membershipInputBuilder.substring(0,
            membershipInputBuilder.length() - 1) : "";

    String request = context.getString(R.string.update_membership, membershipId, membershipInput,
        context.getString(R.string.membershipfragment_info_light));

    if (!StringUtils.isEmpty(membershipInput)) {
      return this.tribeApi.updateMembership(request)
          .doOnNext(membershipRealm -> userCache.updateMembership(membershipRealm));
    } else {
      return Observable.empty();
    }
  }

  @Override public Observable<Void> addMembersToGroup(String groupId, List<String> memberIds) {
    StringBuffer buffer = new StringBuffer();
    String mutationCreateMembership = null;

    int count = 0;
    for (String id : memberIds) {
      buffer.append(context.getString(R.string.create_membership_user, count, groupId, id));
      count++;
    }

    mutationCreateMembership = context.getString(R.string.mutation, buffer.toString());

    return (StringUtils.isEmpty(mutationCreateMembership) ? Observable.empty()
        : tribeApi.createMembershipsForUsers(mutationCreateMembership)
            .doOnNext(aVoid -> userCache.addMembersToGroup(groupId, memberIds))
            .doOnNext(aVoid -> clearGroupAvatar(groupId)));
  }

  @Override public Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds) {
    String memberIdsJson = listToArrayReq(memberIds);
    String request = context.getString(R.string.remove_members_group, groupId, memberIdsJson);

    return this.tribeApi.removeMembersFromGroup(request)
        .doOnNext(aVoid -> userCache.removeMembersFromGroup(groupId, memberIds))
        .doOnNext(aVoid -> clearGroupAvatar(groupId));
  }

  @Override public Observable<Void> removeGroup(String groupId) {
    String request = context.getString(R.string.remove_group, groupId);

    return this.tribeApi.removeGroup(request)
        .doOnError(throwable -> throwable.printStackTrace())
        .doOnNext(aVoid -> userCache.removeGroup(groupId))
        .doOnNext(aVoid -> clearGroupAvatar(groupId));
  }

  @Override public Observable<Void> leaveGroup(String membershipId) {
    String request = context.getString(R.string.leave_group, membershipId);
    return this.tribeApi.leaveGroup(request)
        .doOnNext(aVoid -> userCache.removeGroupFromMembership(membershipId));
  }

  public String listToJson(List<String> list) {
    String json = "\"";
    for (int i = 0; i < list.size(); i++) {
      if (i == list.size() - 1) {
        json += list.get(i) + "\"";
      } else {
        json += list.get(i) + "\", \"";
      }
    }
    if (list.size() == 0) json += "\"";
    return json;
  }

  private String listToArrayReq(List<String> ids) {
    StringBuilder result = new StringBuilder();

    for (String string : ids) {
      result.append("\"" + string + "\"");
      result.append(",");
    }

    return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
  }

  @Override public Observable<FriendshipRealm> updateFriendship(String friendshipId,
      List<Pair<String, String>> values) {
    StringBuilder friendshipInputBuilder = new StringBuilder();

    for (Pair<String, String> value : values) {
      if (value.first.equals(FriendshipRealm.MUTE)) {
        friendshipInputBuilder.append(value.first + ": " + Boolean.valueOf(value.second));
        friendshipInputBuilder.append(",");
      } else if (value.first.equals(FriendshipRealm.STATUS)) {
        friendshipInputBuilder.append(value.first + ": " + value.second);
        friendshipInputBuilder.append(",");
      } else if (!StringUtils.isEmpty(value.second) && !value.second.equals("null")) {
        friendshipInputBuilder.append(value.first + ": \"" + value.second + "\"");
        friendshipInputBuilder.append(",");
      }
    }

    String friendshipInput =
        friendshipInputBuilder.length() > 0 ? friendshipInputBuilder.substring(0,
            friendshipInputBuilder.length() - 1) : "";

    String request = context.getString(R.string.update_friendship, friendshipId, friendshipInput);

    if (!StringUtils.isEmpty(friendshipInput)) {
      return this.tribeApi.updateFriendship(request)
          .doOnNext(friendshipRealm -> userCache.updateFriendship(friendshipRealm));
    } else {
      return Observable.empty();
    }
  }

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

  @Override public Observable<MembershipRealm> createMembership(String groupId) {
    final String requestCreateMembership = context.getString(R.string.create_membership, groupId,
        context.getString(R.string.membershipfragment_info),
        context.getString(R.string.groupfragment_info_members),
        context.getString(R.string.userfragment_infos));
    return this.tribeApi.createMembership(requestCreateMembership)
        .doOnNext(membershipRealm -> userCache.insertMembership(accessToken.getUserId(),
            membershipRealm));
  }

  @Override public Observable<RecipientRealmInterface> getRecipientInfos(String recipientId,
      boolean isToGroup) {
    return null;
  }

  private void clearGroupAvatar(String groupId) {
    File groupAvatarFile = FileUtils.getAvatarForGroupId(context, groupId, FileUtils.PHOTO);
    if (groupAvatarFile != null && groupAvatarFile.exists()) groupAvatarFile.delete();
  }

  @Override
  public Observable<RoomConfiguration> joinRoom(String id, boolean isGroup, String roomId) {
    String body;

    if (!StringUtils.isEmpty(roomId)) {
      body = context.getString(R.string.joinRoomWithId, roomId);
    } else {
      body = context.getString(isGroup ? R.string.joinRoomGroup : R.string.joinRoomFriendship, id);
    }

    final String request = context.getString(R.string.mutation, body) + "\n" + context.getString(
        R.string.roomFragment_infos);

    return this.tribeApi.joinRoom(request);
  }

  @Override public Observable<Boolean> inviteUserToRoom(String roomId, String userId) {
    final String request = context.getString(R.string.mutation,
        context.getString(R.string.inviteToRoom, roomId, userId));

    return this.tribeApi.inviteUserToRoom(request);
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.buzzRoom, roomId));

    return this.tribeApi.buzzRoom(request);
  }
}

