package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.presentation.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 27/01/2017.
 */

@Singleton public class JsonToModel {

  // VARIABLES
  private Gson gson;

  // OBSERABLES
  private PublishSubject<List<UserRealm>> onUserListUpdated = PublishSubject.create();
  private PublishSubject<List<GroupRealm>> onGroupListUpdated = PublishSubject.create();
  private PublishSubject<List<FriendshipRealm>> onFriendshipListUpdated = PublishSubject.create();
  private PublishSubject<String> onAddedOnline = PublishSubject.create();
  private PublishSubject<List<String>> onAddedListOnline = PublishSubject.create();
  private PublishSubject<List<String>> onRemovedListOnline = PublishSubject.create();
  private PublishSubject<List<String>> onAddedListLive = PublishSubject.create();
  private PublishSubject<List<String>> onRemovedListLive = PublishSubject.create();
  private PublishSubject<String> onRemovedOnline = PublishSubject.create();
  private PublishSubject<String> onAddedLive = PublishSubject.create();
  private PublishSubject<String> onRemovedLive = PublishSubject.create();
  private PublishSubject<String> onCreatedMembership = PublishSubject.create();
  private PublishSubject<String> onRemovedMembership = PublishSubject.create();
  private PublishSubject<FriendshipRealm> onCreatedFriendship = PublishSubject.create();
  private PublishSubject<FriendshipRealm> onRemovedFriendship = PublishSubject.create();
  private PublishSubject<Invite> onInviteCreated = PublishSubject.create();
  private PublishSubject<Invite> onInviteRemoved = PublishSubject.create();

  @Inject public JsonToModel(@Named("simpleGson") Gson gson) {
    this.gson = gson;
  }

  public void convertToSubscriptionResponse(String json) {
    if (!StringUtils.isEmpty(json) && json.contains("data")) {
      JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonObject results = jsonObject.getAsJsonObject("data");

      List<UserRealm> updatedUserList = new ArrayList<>();
      List<GroupRealm> updatedGroupList = new ArrayList<>();
      List<FriendshipRealm> updatedFriendshipList = new ArrayList<>();

      boolean shouldBulkLiveStatus = results.entrySet().size() > 5;

      for (Map.Entry<String, JsonElement> entry : results.entrySet()) {
        if (!entry.getValue().isJsonNull()) {
          String payload = entry.getValue().toString();
          JsonParser jsonParser = new JsonParser();
          JsonElement element = jsonParser.parse(payload);

          if (element != null && element.isJsonObject()) {
            JsonObject jo = element.getAsJsonObject();

            if (entry.getKey().contains(WSService.USER_SUFFIX)) {
              boolean shouldUpdateOnlineStatus = false;

              UserRealm userRealm = gson.fromJson(entry.getValue().toString(), UserRealm.class);
              userRealm.setJsonPayloadUpdate(jo);

              if (jo.has("is_online")) shouldUpdateOnlineStatus = true;

              if (shouldUpdateOnlineStatus) {
                if (userRealm.isOnline()) {
                  onAddedOnline.onNext(userRealm.getId());
                } else {
                  onRemovedOnline.onNext(userRealm.getId());
                }
              }

              updatedUserList.add(userRealm);
            } else if (entry.getKey().contains(WSService.GROUP_SUFFIX)) {
              GroupRealm groupRealm = gson.fromJson(entry.getValue().toString(), GroupRealm.class);
              groupRealm.setJsonPayloadUpdate(jo);
              updatedGroupList.add(groupRealm);

              boolean shouldUpdateLiveStatus = false;

              if (jo.has("is_live")) shouldUpdateLiveStatus = true;

              if (shouldUpdateLiveStatus) {
                if (groupRealm.isLive()) {
                  onAddedLive.onNext(groupRealm.getId());
                } else {
                  onRemovedLive.onNext(groupRealm.getId());
                }
              }
            } else if (entry.getKey().contains(WSService.FRIENDSHIP_UDPATED_SUFFIX)) {
              boolean shouldUpdateLiveStatus = false;

              if (jo.has("is_live")) shouldUpdateLiveStatus = true;

              FriendshipRealm friendshipRealm =
                  gson.fromJson(entry.getValue().toString(), FriendshipRealm.class);

              if (shouldUpdateLiveStatus && !StringUtils.isEmpty(friendshipRealm.getId())) {
                if (friendshipRealm.isLive()) {
                  onAddedLive.onNext(friendshipRealm.getId());
                } else {
                  onRemovedLive.onNext(friendshipRealm.getId());
                }
              }
            } else if (entry.getKey().contains(WSService.INVITE_CREATED_SUFFIX)) {
              Timber.d("Invite created : " + entry.getValue().toString());
              Invite invite = gson.fromJson(entry.getValue().toString(), Invite.class);
              onInviteCreated.onNext(invite);
            } else if (entry.getKey().contains(WSService.INVITE_REMOVED_SUFFIX)) {
              Timber.d("Invite removed : " + entry.getValue().toString());
              Invite invite = gson.fromJson(entry.getValue().toString(), Invite.class);
              onInviteRemoved.onNext(invite);
            } else if (entry.getKey().contains(WSService.FRIENDSHIP_CREATED_SUFFIX)) {
              Timber.d("Friendship created : " + entry.getValue().toString());
              FriendshipRealm friendshipRealm =
                  gson.fromJson(entry.getValue().toString(), FriendshipRealm.class);
              //onCreatedFriendship.onNext(friendshipRealm);
            } else if (entry.getKey().contains(WSService.FRIENDSHIP_REMOVED_SUFFIX)) {
              Timber.d("Friendship removed : " + entry.getValue().toString());
              FriendshipRealm friendshipRealm =
                  gson.fromJson(entry.getValue().toString(), FriendshipRealm.class);
              onRemovedFriendship.onNext(friendshipRealm);
            } else if (entry.getKey().contains(WSService.MEMBERSHIP_CREATED_SUFFIX)) {
              Timber.d("Membership created : " + entry.getValue().toString());
              //onCreatedMembership.onNext(entry.getValue().getAsJsonObject().get("group_id").getAsString());
            } else if (entry.getKey().contains(WSService.MEMBERSHIP_REMOVED_SUFFIX)) {
              Timber.d("Membership removed : " + entry.getValue().toString());
              onRemovedMembership.onNext(
                  entry.getValue().getAsJsonObject().get("id").getAsString());
            }
          }
        }
      }

      if (updatedGroupList.size() > 0) onGroupListUpdated.onNext(updatedGroupList);
      if (updatedUserList.size() > 0) onUserListUpdated.onNext(updatedUserList);
      if (updatedFriendshipList.size() > 0) onFriendshipListUpdated.onNext(updatedFriendshipList);
    }
  }

  public Observable<List<UserRealm>> onUserListUpdated() {
    return onUserListUpdated;
  }

  public Observable<List<GroupRealm>> onGroupListUpdated() {
    return onGroupListUpdated;
  }

  public Observable<List<FriendshipRealm>> onFriendshipListUpdated() {
    return onFriendshipListUpdated;
  }

  public Observable<String> onAddedOnline() {
    return onAddedOnline;
  }

  public Observable<String> onRemovedOnline() {
    return onRemovedOnline;
  }

  public Observable<String> onAddedLive() {
    return onAddedLive;
  }

  public Observable<String> onRemovedLive() {
    return onRemovedLive;
  }

  public Observable<String> onCreatedMembership() {
    return onCreatedMembership;
  }

  public Observable<String> onRemovedMembership() {
    return onRemovedMembership;
  }

  public Observable<FriendshipRealm> onCreatedFriendship() {
    return onCreatedFriendship;
  }

  public Observable<FriendshipRealm> onRemovedFriendship() {
    return onRemovedFriendship;
  }

  public Observable<Invite> onInviteCreated() {
    return onInviteCreated;
  }

  public Observable<Invite> onInviteRemoved() {
    return onInviteRemoved;
  }

  public Observable<List<String>> onAddedListLive() {
    return onAddedListLive;
  }

  public Observable<List<String>> onRemovedListLive() {
    return onRemovedListLive;
  }

  public Observable<List<String>> onAddedListOnline() {
    return onAddedListOnline;
  }

  public Observable<List<String>> onRemovedListOnline() {
    return onRemovedListOnline;
  }
}
