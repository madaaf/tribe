package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupMemberRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

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
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();

            UserRealm userDB = obsRealm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();

            if (userDB != null) {
                updateFriendships(obsRealm, userRealm, userDB);

                userDB.setUsername(userRealm.getUsername());
                userDB.setScore(userRealm.getScore());
                userDB.setPhone(userRealm.getPhone());
                userDB.setDisplayName(userRealm.getDisplayName());
                userDB.setProfilePicture(userRealm.getProfilePicture());
                userDB.setFbid(userRealm.getFbid());
                userDB.setTribeSave(userRealm.isTribeSave());
                userDB.setInvisibleMode(userRealm.isInvisibleMode());

                if (userRealm.getLocation() != null) {
                    LocationRealm locationRealm = obsRealm.copyToRealmOrUpdate(userRealm.getLocation());
                    userDB.setLocation(locationRealm);
                } else {
                    userDB.setLocation(null);
                }
            } else {
                obsRealm.insertOrUpdate(userRealm);
                userDB = obsRealm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
                updateFriendships(obsRealm, userRealm, userDB);
            }

            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    private void updateFriendships(Realm obsRealm, UserRealm userRealm, UserRealm userDB) {
        if (userRealm.getMemberships() != null) {
            for (MembershipRealm membershipRealm : userRealm.getMemberships()) {
                MembershipRealm membershipDB = obsRealm.where(MembershipRealm.class).equalTo("id", membershipRealm.getId()).findFirst();

                if (membershipDB != null) {
                    membershipDB.getGroup().setName(membershipRealm.getGroup().getName());
                    membershipDB.getGroup().setPicture(membershipRealm.getGroup().getPicture());
                    membershipDB.getGroup().setLink(membershipRealm.getGroup().getLink());
                    membershipDB.setMute(membershipRealm.isMute());
                    refactorMembers(obsRealm, membershipRealm, membershipDB);
                    refactorAdmins(obsRealm, membershipRealm, membershipDB);
                } else {
                    membershipRealm.setUpdatedAt(new Date());
                    refactorMembers(obsRealm, membershipRealm, membershipRealm);
                    refactorAdmins(obsRealm, membershipRealm, membershipRealm);
                    obsRealm.insertOrUpdate(membershipRealm);
                    membershipDB = obsRealm.where(MembershipRealm.class).equalTo("id", membershipRealm.getId()).findFirst();
                    userDB.getMemberships().add(membershipDB);
                }

                boolean found = false;
                for (MembershipRealm membershipRealmDB : userDB.getMemberships()) {
                    if (membershipRealmDB.getId().equals(membershipRealm.getId()))
                        found = true;
                }

                if (!found) {
                    userDB.getMemberships().add(membershipDB);
                }
            }
        }

        if (userRealm.getGroups() != null) {
            for (GroupRealm groupRealm : userRealm.getGroups()) {
                GroupRealm groupRealmDB = obsRealm.where(GroupRealm.class).equalTo("id", groupRealm.getId()).findFirst();

                if (groupRealmDB != null) {
                    groupRealmDB.setLink(groupRealm.getLink());
                    groupRealmDB.setName(groupRealm.getName());
                    groupRealmDB.setPicture(groupRealm.getPicture());
                }
            }
        }

        if (userRealm.getFriendships() != null) {
            for (FriendshipRealm friendshipRealm : userRealm.getFriendships()) {
                FriendshipRealm friendshipDB = obsRealm.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();

                if (friendshipDB != null) {
                    friendshipDB.getFriend().setProfilePicture(friendshipRealm.getFriend().getProfilePicture());
                    friendshipDB.getFriend().setDisplayName(friendshipRealm.getFriend().getDisplayName());
                    friendshipDB.getFriend().setScore(friendshipRealm.getFriend().getScore());
                    friendshipDB.getFriend().setUsername(friendshipRealm.getFriend().getUsername());
                    friendshipDB.getFriend().setPhone(friendshipRealm.getFriend().getPhone());
                    friendshipDB.getFriend().setFbid(friendshipRealm.getFriend().getFbid());
                } else {
                    friendshipRealm.getFriend().setUpdatedAt(new Date());
                    FriendshipRealm addedFriendship = obsRealm.copyToRealmOrUpdate(friendshipRealm);
                    userDB.getFriendships().add(addedFriendship);
                }

                boolean found = false;
                for (FriendshipRealm friendshipRealmDB : userDB.getFriendships()) {
                    if (friendshipRealmDB.getId().equals(friendshipRealm.getId()))
                        found = true;
                }

                if (!found) {
                    userDB.getFriendships().add(friendshipRealm);
                }
            }
        }
    }

    private void refactorMembers(Realm realm, MembershipRealm from, MembershipRealm to) {
        RealmList<GroupMemberRealm> membersEnd = new RealmList<>();

        if (from.getGroup().getMembers() != null) {
            for (UserRealm member : from.getGroup().getMembers()) {
                UserRealm memberDB = realm.where(UserRealm.class).equalTo("id", member.getId()).findFirst();

                if (memberDB == null) {
                    realm.insertOrUpdate(member);
                }

                memberDB = realm.where(UserRealm.class).equalTo("id", member.getId()).findFirst();

                GroupMemberRealm groupMemberRealm = realm.where(GroupMemberRealm.class).equalTo("id", memberDB.getId()).equalTo("groupId", to.getSubId()).findFirst();

                if (groupMemberRealm == null) {
                    realm.insertOrUpdate(new GroupMemberRealm(memberDB.getId(), to.getSubId()));
                    groupMemberRealm = realm.where(GroupMemberRealm.class).equalTo("id", memberDB.getId()).equalTo("groupId", to.getSubId()).findFirst();
                }

                membersEnd.add(groupMemberRealm);
            }
        }

        to.getGroup().setMemberIdList(membersEnd);
    }

    private void refactorAdmins(Realm realm, MembershipRealm from, MembershipRealm to) {
        RealmList<GroupMemberRealm> adminsEnd = new RealmList<>();

        try {
            if (from.getGroup().getAdmins() != null) {
                for (UserRealm member : from.getGroup().getAdmins()) {
                    GroupMemberRealm groupMemberRealm = realm.where(GroupMemberRealm.class).equalTo("id", member.getId()).equalTo("groupId", to.getSubId()).findFirst();

                    if (groupMemberRealm == null) {
                        realm.insertOrUpdate(new GroupMemberRealm(member.getId(), to.getSubId()));
                        groupMemberRealm = realm.where(GroupMemberRealm.class).equalTo("id", member.getId()).equalTo("groupId", to.getSubId()).findFirst();
                    }

                    adminsEnd.add(groupMemberRealm);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        to.getGroup().setAdminIdList(adminsEnd);
    }

    public void put(AccessToken accessToken) {
        Realm otherRealm = Realm.getDefaultInstance();
        try {
            otherRealm.beginTransaction();
            otherRealm.delete(AccessToken.class);
            otherRealm.copyToRealm(accessToken);
            otherRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            otherRealm.close();
        }
    }

    public void put(Installation installation) {
        Realm otherRealm = Realm.getDefaultInstance();
        try {
            otherRealm.beginTransaction();
            otherRealm.delete(Installation.class);
            otherRealm.copyToRealm(installation);
            otherRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            otherRealm.close();
        }
    }

    // ALWAYS CALLED ON MAIN THREAD
    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return Observable.just(userId)
                .map(id -> realm.where(UserRealm.class).equalTo("id", id).findFirst())
                .map(o -> realm.copyFromRealm(o))
                .observeOn(Schedulers.io())
                .map(userRealm -> {
                    Realm newRealm = Realm.getDefaultInstance();
                    if (userRealm.getMemberships() != null) {
                        try {
                            for (MembershipRealm membershipRealm : userRealm.getMemberships()) {
                                mapMembers(newRealm, membershipRealm);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    return userRealm;
                });
    }

    private void mapMembers(Realm realm, MembershipRealm membershipRealm) {
        try {
            List<String> ids = new ArrayList<>();

            for (GroupMemberRealm member : membershipRealm.getGroup().getMemberIdList()) {
                ids.add(member.getId());
            }

            if (ids.size() > 0) {
                RealmResults<UserRealm> membersRealm = realm.where(UserRealm.class).in("id", ids.toArray(new String[ids.size()])).findAll();
                membershipRealm.getGroup().getMembers().addAll(realm.copyFromRealm(membersRealm));
            }

            ids.clear();

            for (GroupMemberRealm admin : membershipRealm.getGroup().getAdminIdList()) {
                ids.add(admin.getId());
            }

            if (ids.size() > 0) {
                RealmResults<UserRealm> adminsRealm = realm.where(UserRealm.class).in("id", ids.toArray(new String[ids.size()])).findAll();
                membershipRealm.getGroup().getAdmins().clear();
                membershipRealm.getGroup().getAdmins().addAll(realm.copyFromRealm(adminsRealm));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
    public MembershipRealm membershipForGroupId(String groupId) {
        Realm obsRealm = Realm.getDefaultInstance();
        MembershipRealm membershipRealm = obsRealm.where(MembershipRealm.class).equalTo("group.id", groupId).findFirst();
        final MembershipRealm results = membershipRealm == null ? null : obsRealm.copyFromRealm(membershipRealm);
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
        Realm otherRealm = Realm.getDefaultInstance();
        try {
            otherRealm.beginTransaction();
            FriendshipRealm friendshipRealm = otherRealm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
            if (friendshipRealm != null) friendshipRealm.deleteFromRealm();
            otherRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            otherRealm.close();
        }
    }

    @Override
    public void insertGroup(GroupRealm groupRealm) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(groupRealm);
            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void updateGroup(GroupRealm groupRealm) {
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.executeTransaction(realm1 -> {
                GroupRealm groupRealmDB = realm1.where(GroupRealm.class).equalTo("id", groupRealm.getId()).findFirst();
                groupRealmDB.setName(groupRealm.getName());
                groupRealmDB.setPicture(groupRealm.getPicture());
                groupRealmDB.setLink(groupRealm.getLink());
            });
        } finally {
            realm.close();
        }
    }

    @Override
    public void addMembersToGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();

            RealmList<UserRealm> usersToAdd = new RealmList<>();

            for (String memberId : memberIds) {
                usersToAdd.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            }

            GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
            for (UserRealm user : usersToAdd) {
                groupRealm.getMembers().add(user);
            }

            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void removeMembersFromGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();
            RealmList<UserRealm> usersToRemove = new RealmList<>();

            for (String memberId : memberIds) {
                usersToRemove.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            }

            GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
            for (UserRealm user : usersToRemove) {
                groupRealm.getMembers().remove(user);
            }

            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void addAdminsToGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();

            RealmList<UserRealm> usersToAdd = new RealmList<>();
            for (String memberId : memberIds) {
                usersToAdd.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            }

            GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
            for (UserRealm user : usersToAdd) {
                groupRealm.getAdmins().add(user);
            }

            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void removeAdminsFromGroup(String groupId, List<String> memberIds) {
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();
            RealmList<UserRealm> usersToRemove = new RealmList<>();

            for (String memberId : memberIds) {
                usersToRemove.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
            }

            GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
            for (UserRealm user : usersToRemove) {
                groupRealm.getMembers().remove(user);
            }

            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void removeGroup(String groupId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            MembershipRealm membershipRealm = realm.where(MembershipRealm.class).equalTo("group.id", groupId).findFirst();
            if (membershipRealm != null) membershipRealm.deleteFromRealm();
            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void removeGroupFromMembership(String membershipId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            MembershipRealm membershipRealm = realm.where(MembershipRealm.class).equalTo("id", membershipId).findFirst();
            if (membershipRealm != null) membershipRealm.deleteFromRealm();
            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void insertMembership(String userId, MembershipRealm membershipRealm) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();

            refactorMembers(realm, membershipRealm, membershipRealm);
            refactorAdmins(realm, membershipRealm, membershipRealm);

            MembershipRealm membershipRealmDB = realm.copyToRealmOrUpdate(membershipRealm);
            UserRealm user = realm.where(UserRealm.class).equalTo("id", userId).findFirst();

            boolean found = false;

            if (user.getMemberships() != null) {

                for (MembershipRealm dbMembership : user.getMemberships()) {
                    if (dbMembership.getGroup().getId().equals(membershipRealm.getGroup().getId())) {
                        found = true;
                    }
                }
            }

            if (!found) user.getMemberships().add(membershipRealmDB);

            realm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (realm.isInTransaction()) realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    @Override
    public void updateMembershipLink(String userId, String membershipId, MembershipRealm membershipRealm) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            MembershipRealm membershipRealmDb = realm.where(UserRealm.class).equalTo("id", userId).findFirst()
                    .getMemberships().where().equalTo("id", membershipId).findFirst();
            String privateLink = membershipRealm.getLink();
            String publicLink = membershipRealm.getGroup().getLink();
            Date privateLinkExpiration = membershipRealm.getLink_expires_at();
            if (privateLink != null) membershipRealmDb.setLink(privateLink);
            if (publicLink != null) membershipRealmDb.getGroup().setLink(publicLink);
            if (privateLinkExpiration != null)
                membershipRealmDb.setLink_expires_at(privateLinkExpiration);
            realm.commitTransaction();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            if (realm != null) realm.cancelTransaction();
        } finally {
            if (realm != null) realm.close();
        }
    }


    @Override
    public void updateScore(String userId, ScoreUtils.Point point) {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            UserRealm userDB = obsRealm.where(UserRealm.class).equalTo("id", userId).findFirst();
            if (userDB != null) {
                userDB.setScore(userDB.getScore() + point.getPoints());
            }
            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public void updateScore(String userId, int score) {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            UserRealm userDB = obsRealm.where(UserRealm.class).equalTo("id", userId).findFirst();
            if (userDB != null && score > userDB.getScore()) {
                userDB.setScore(score);
            }
            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public Observable<FriendshipRealm> updateFriendship(String friendshipId, @FriendshipRealm.FriendshipStatus String status) {
        return Observable.create((Observable.OnSubscribe<FriendshipRealm>) subscriber -> {
            Realm otherRealm = Realm.getDefaultInstance();
            try {
                otherRealm.beginTransaction();
                FriendshipRealm friendshipRealm = otherRealm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
                if (friendshipRealm != null) {
                    friendshipRealm.setStatus(status);
                }

                otherRealm.commitTransaction();

                if (subscriber != null && friendshipRealm != null) {
                    subscriber.onNext(otherRealm.copyFromRealm(friendshipRealm));
                    subscriber.onCompleted();
                }
            } catch (IllegalStateException ex) {
                if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
                ex.printStackTrace();
            } finally {
                otherRealm.close();
            }
        });
    }

    @Override
    public FriendshipRealm updateFriendshipNoObs(String friendshipId, @FriendshipRealm.FriendshipStatus String status) {
        Realm otherRealm = Realm.getDefaultInstance();

        try {
            otherRealm.beginTransaction();
            FriendshipRealm friendshipRealm = otherRealm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
            if (friendshipRealm != null) {
                friendshipRealm.setStatus(status);
            }

            otherRealm.commitTransaction();

            if (friendshipRealm != null) {
                return otherRealm.copyFromRealm(friendshipRealm);
            }
        } catch (IllegalStateException ex) {
            if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            otherRealm.close();
        }

        return null;
    }

    @Override
    public MembershipRealm membershipInfos(String membershipId) {
        Realm obsRealm = Realm.getDefaultInstance();
        MembershipRealm membershipRealm = obsRealm.where(MembershipRealm.class).equalTo("id", membershipId).findFirst();
        final MembershipRealm result = membershipRealm == null ? null : obsRealm.copyFromRealm(membershipRealm);
        if (membershipRealm != null) mapMembers(obsRealm, result);
        obsRealm.close();
        return result;
    }
}
