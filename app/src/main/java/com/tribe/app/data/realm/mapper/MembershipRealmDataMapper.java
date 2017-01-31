package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.domain.entity.Membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.RealmList;

/**
 * Mapper class used to transform {@link com.tribe.app.data.realm.MembershipRealm} (in the data
 * layer) to {@link com.tribe.app.domain.entity.Membership} in the
 * domain layer.
 */
@Singleton public class MembershipRealmDataMapper {

  private GroupRealmDataMapper groupRealmDataMapper;

  @Inject public MembershipRealmDataMapper(GroupRealmDataMapper groupRealmDataMapper) {
    this.groupRealmDataMapper = groupRealmDataMapper;
  }

  /**
   * Transform a {@link com.tribe.app.data.realm.MembershipRealm} into an {@link Membership}.
   *
   * @param membershipRealm Object to be transformed.
   * @return {@link com.tribe.app.domain.entity.Membership} if valid {@link
   * com.tribe.app.data.realm.MembershipRealm} otherwise null.
   */
  public Membership transform(MembershipRealm membershipRealm) {
    Membership membership = null;

    if (membershipRealm != null) {
      membership = new Membership(membershipRealm.getId());
      membership.setGroup(groupRealmDataMapper.transform(membershipRealm.getGroup()));
      membership.setMute(membershipRealm.isMute());
      membership.setCreatedAt(membershipRealm.getCreatedAt());
      membership.setUpdatedAt(membershipRealm.getUpdatedAt());
    }

    return membership;
  }

  /**
   * Transform a List of {@link MembershipRealm} into a Collection of {@link Membership}.
   *
   * @param membershipRealmCollection Object Collection to be transformed.
   * @return {@link Membership} if valid {@link MembershipRealm} otherwise null.
   */
  public List<Membership> transform(Collection<MembershipRealm> membershipRealmCollection) {
    List<Membership> membershipList = new ArrayList<>();
    Membership membership;

    for (MembershipRealm membershipRealm : membershipRealmCollection) {
      membership = transform(membershipRealm);
      if (membership != null) {
        membershipList.add(membership);
      }
    }

    return membershipList;
  }

  /**
   * Transform a {@link Membership} into an {@link MembershipRealm}.
   *
   * @param membership Object to be transformed.
   * @return {@link MembershipRealm} if valid {@link Membership} otherwise null.
   */
  public MembershipRealm transform(Membership membership) {
    MembershipRealm membershipRealm = null;

    if (membership != null) {
      membershipRealm = new MembershipRealm();
      membershipRealm.setId(membership.getId());
      membershipRealm.setGroup(groupRealmDataMapper.transform(membership.getGroup()));
      membershipRealm.setMute(membership.isMute());
      membershipRealm.setCreatedAt(membership.getCreatedAt());
      membershipRealm.setUpdatedAt(membership.getUpdatedAt());
    }

    return membershipRealm;
  }

  /**
   * Transform a List of {@link Membership} into a Collection of {@link MembershipRealm}.
   *
   * @param membershipCollection Object Collection to be transformed.
   * @return {@link MembershipRealm} if valid {@link MembershipRealm} otherwise null.
   */
  public RealmList<MembershipRealm> transformMemberships(
      Collection<Membership> membershipCollection) {
    RealmList<MembershipRealm> membershipRealmList = new RealmList<>();
    MembershipRealm membershipRealm;

    for (Membership membership : membershipCollection) {
      membershipRealm = transform(membership);
      if (membership != null) {
        membershipRealmList.add(membershipRealm);
      }
    }

    return membershipRealmList;
  }
}
