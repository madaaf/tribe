package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Group;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class UserCacheImpl implements UserCache {

    private Context context;
    private Realm realm;
    private UserRealm results;

    @Inject
    public UserCacheImpl(Context context, Realm realm) {
        this.context = context;
        this.realm = realm;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(int userId) {
        return false;
    }

    public void put(UserRealm userRealm) {
        try {
            Realm obsRealm = Realm.getDefaultInstance();
            obsRealm.beginTransaction();

            UserRealm userDB = obsRealm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();

            if (userDB != null) {
                for (GroupRealm groupRealm : userRealm.getGroups()) {
                    GroupRealm groupDB = obsRealm.where(GroupRealm.class).equalTo("id", groupRealm.getId()).findFirst();

                    if (groupDB != null) {
                        groupDB.setName(groupRealm.getName());
                        groupDB.setPicture(groupRealm.getPicture());
                        groupDB.setMembers(groupRealm.getMembers());
                    } else {
                        GroupRealm addedGroup = obsRealm.copyToRealmOrUpdate(groupRealm);
                        userDB.getGroups().add(addedGroup);
                    }

                    boolean found = false;
                    for (GroupRealm groupRealmDB : userDB.getGroups()) {
                        if (groupRealmDB.getId().equals(groupRealm.getId())) found = true;
                    }

                    if (!found) {
                        userDB.getGroups().add(groupRealm);
                    }
                }

                for (FriendshipRealm friendshipRealm : userRealm.getFriendships()) {
                    FriendshipRealm friendshipDB = obsRealm.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();

                    if (friendshipDB != null) {
                        friendshipDB.setBlocked(friendshipRealm.isBlocked());
                        friendshipDB.getFriend().setProfilePicture(friendshipRealm.getFriend().getProfilePicture());
                        friendshipDB.getFriend().setDisplayName(friendshipRealm.getFriend().getDisplayName());
                        friendshipDB.getFriend().setScore(friendshipRealm.getFriend().getScore());
                        friendshipDB.getFriend().setUsername(friendshipRealm.getFriend().getUsername());
                        friendshipDB.getFriend().setPhone(friendshipRealm.getFriend().getPhone());
                        friendshipDB.getFriend().setFbid(friendshipRealm.getFriend().getFbid());
                    } else {
                        FriendshipRealm addedFriendship = obsRealm.copyToRealmOrUpdate(friendshipRealm);
                        userDB.getFriendships().add(addedFriendship);
                    }

                    boolean found = false;
                    for (FriendshipRealm friendshipRealmDB : userDB.getFriendships()) {
                        if (friendshipRealmDB.getId().equals(friendshipRealm.getId())) found = true;
                    }

                    if (!found) {
                        userDB.getFriendships().add(friendshipRealm);
                    }
                }

                userDB.setUsername(userRealm.getUsername());
                userDB.setScore(userRealm.getScore());
                userDB.setPhone(userRealm.getPhone());
                userDB.setDisplayName(userRealm.getDisplayName());
                userDB.setProfilePicture(userRealm.getProfilePicture());
                userDB.setFbid(userRealm.getFbid());

                if (userRealm.getLocation() != null) {
                    LocationRealm locationRealm = obsRealm.copyToRealmOrUpdate(userRealm.getLocation());
                    userDB.setLocation(locationRealm);
                }
            } else {
                obsRealm.copyToRealmOrUpdate(userRealm);
            }

            obsRealm.commitTransaction();
            obsRealm.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void put(AccessToken accessToken) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(AccessToken.class);
        realm.copyToRealm(accessToken);
        realm.commitTransaction();
        realm.close();
    }

    public void put(Installation installation) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Installation.class);
        realm.copyToRealm(installation);
        realm.commitTransaction();
        realm.close();
    }

    // ALWAYS CALLED ON MAIN THREAD
    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return Observable.create(new Observable.OnSubscribe<UserRealm>() {
            @Override
            public void call(final Subscriber<? super UserRealm> subscriber) {
                results = realm.where(UserRealm.class).equalTo("id", userId).findFirst();
//                results.removeChangeListeners();
//                results.addChangeListener(element -> {
//                    if (results != null) {
//                        subscriber.onNext(realm.copyFromRealm(results));
//                    }
//                });

                if (results != null)
                    subscriber.onNext(realm.copyFromRealm(results));
            }
        });
    }

    @Override
    public UserRealm userInfosNoObs(String userId) {
        Realm obsRealm = Realm.getDefaultInstance();
        UserRealm userRealm = obsRealm.where(UserRealm.class).equalTo("id", userId).findFirst();
        final UserRealm results = userRealm == null ? null : obsRealm.copyFromRealm(userRealm);
        obsRealm.close();
        return results;
    }

    @Override
    public GroupRealm groupInfos(String groupId) {
        Realm obsRealm = Realm.getDefaultInstance();
        GroupRealm groupRealm = obsRealm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        final GroupRealm results = groupRealm == null ? null : obsRealm.copyFromRealm(groupRealm);
        obsRealm.close();
        return results;
    }

    @Override
    public FriendshipRealm friendshipForUserId(String userId) {
        Realm otherRealm = Realm.getDefaultInstance();
        FriendshipRealm friendshipRealm = otherRealm.where(FriendshipRealm.class).equalTo("friend.id", userId).findFirst();
        if (friendshipRealm != null)
            return otherRealm.copyFromRealm(friendshipRealm);
        else
            return null;
    }

    @Override
    public void removeFriendship(String friendshipId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        FriendshipRealm friendshipRealm = realm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
        if (friendshipRealm != null) friendshipRealm.deleteFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void createGroup(String userId, String groupId, String groupName, List<String> memberIds, Boolean isPrivate, String pictureUri) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        UserRealm userRealm = realm.where(UserRealm.class).equalTo("id", userId).findFirst();
        GroupRealm groupRealm = new GroupRealm();
        groupRealm.setName(groupName);
        groupRealm.setId(groupId);
        groupRealm.setPrivateGroup(isPrivate);
        if (pictureUri != null) groupRealm.setPicture(pictureUri);
        userRealm.getGroups().add(groupRealm);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void updateGroup(String groupId, String groupName, String pictureUri) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        if (groupName != null) groupRealm.setName(groupName);
        if (pictureUri != null) groupRealm.setPicture(pictureUri);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void addMembersToGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmList<UserRealm> usersToAdd = new RealmList<>();
        realm.commitTransaction();
        for (String memberId : memberIds) {
            realm.beginTransaction();
            usersToAdd.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            realm.commitTransaction();
        }
        realm.beginTransaction();
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        for (UserRealm user: usersToAdd) {
            groupRealm.getMembers().add(user);
        }
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void removeMembersFromGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmList<UserRealm> usersToRemove = new RealmList<>();
        realm.commitTransaction();
        for (String memberId : memberIds) {
            realm.beginTransaction();
            usersToRemove.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            realm.commitTransaction();
        }
        realm.beginTransaction();
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        for (UserRealm user: usersToRemove) {
            groupRealm.getMembers().remove(user);
        }
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void addAdminsToGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmList<UserRealm> usersToAdd = new RealmList<>();
        realm.commitTransaction();
        for (String memberId : memberIds) {
            realm.beginTransaction();
            usersToAdd.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            realm.commitTransaction();
        }
        realm.beginTransaction();
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        for (UserRealm user: usersToAdd) {
            groupRealm.getAdmins().add(user);
        }
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void removeAdminsFromGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmList<UserRealm> usersToRemove = new RealmList<>();
        realm.commitTransaction();
        for (String memberId : memberIds) {
            realm.beginTransaction();
            usersToRemove.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            realm.commitTransaction();
        }
        realm.beginTransaction();
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        for (UserRealm user: usersToRemove) {
            groupRealm.getMembers().remove(user);
        }
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void removeGroup(String groupId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
        if (groupRealm != null) groupRealm.deleteFromRealm();
        realm.commitTransaction();
        realm.close();
    }


}
