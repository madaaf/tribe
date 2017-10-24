package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.ShortcutLastSeenRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.ShortcutLastSeen;
import com.tribe.app.domain.entity.Shortcut;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link ShortcutRealm} (in the data layer)
 * to {@link Shortcut} in the
 * domain layer.
 */
@Singleton public class ShortcutRealmDataMapper {

  private UserRealmDataMapper userRealmDataMapper;

  @Inject public ShortcutRealmDataMapper() {
  }

  /**
   * Transform a {@link ShortcutRealm} into an {@link Shortcut}.
   *
   * @param shortcutRealm Object to be transformed.
   * @return {@link Shortcut} if valid {@link ShortcutRealm} otherwise null.
   */
  public Shortcut transform(ShortcutRealm shortcutRealm) {
    Shortcut shortcut = null;

    if (shortcutRealm != null) {
      shortcut = new Shortcut(shortcutRealm.getId());
      shortcut.setName(shortcutRealm.getName());
      shortcut.setPicture(shortcutRealm.getPicture());
      shortcut.setOnline(shortcutRealm.isOnline());
      shortcut.setLive(shortcutRealm.isLive());
      shortcut.setPinned(shortcutRealm.isPinned());
      shortcut.setRead(shortcutRealm.isRead());
      shortcut.setSingle(shortcutRealm.isSingle());
      shortcut.setMute(shortcutRealm.isMute());
      shortcut.setStatus(shortcutRealm.getStatus());
      shortcut.setLastMessage(shortcutRealm.getLastMessage());
      shortcut.setMembers(userRealmDataMapper.transform(shortcutRealm.getMembers()));
      shortcut.setLastActivityAt(shortcutRealm.getLastActivityAt());
      shortcut.setCreatedAt(shortcutRealm.getCreatedAt());
      shortcut.setLeaveOnlineUntil(shortcutRealm.getLeaveOnlineUntil());
      shortcut.setMembersHash(shortcutRealm.getMembersHash());
      shortcut.setShortcutLastSeen(transformListShortcutLastSeenRealm(shortcutRealm.getLastSeen()));
    }

    return shortcut;
  }

  /**
   * Transform a {@link Shortcut} into an {@link ShortcutRealm}.
   *
   * @param shortcut Object to be transformed.
   * @return {@link ShortcutRealm} if valid {@link Shortcut} otherwise null.
   */
  public ShortcutRealm transform(Shortcut shortcut) {
    ShortcutRealm shortcutRealm = null;

    if (shortcut != null) {
      shortcutRealm = new ShortcutRealm();
      shortcutRealm.setId(shortcut.getId());
      shortcutRealm.setName(shortcut.getName());
      shortcutRealm.setPicture(shortcut.getProfilePicture());
      shortcutRealm.setOnline(shortcut.isOnline());
      shortcutRealm.setLive(shortcut.isLive());
      shortcutRealm.setPinned(shortcut.isPinned());
      shortcutRealm.setRead(shortcut.isRead());
      shortcutRealm.setMute(shortcut.isMute());
      shortcutRealm.setSingle(shortcut.isSingle());
      shortcutRealm.setStatus(shortcut.getStatus());
      shortcutRealm.setLastMessage(shortcut.getLastMessage());
      shortcutRealm.setMembers(userRealmDataMapper.transformList(shortcut.getMembers()));
      shortcutRealm.setLastActivityAt(shortcut.getLastActivityAt());
      shortcutRealm.setCreatedAt(shortcut.getCreatedAt());
      shortcutRealm.setLeaveOnlineUntil(shortcut.getLeaveOnlineUntil());
      shortcutRealm.setMembersHash(shortcut.getMembersHash());
      shortcutRealm.setLastSeen(transformListShortcutLastSeen(shortcut.getShortcutLastSeen()));
    }

    return shortcutRealm;
  }

  public ShortcutLastSeenRealm transform(ShortcutLastSeen shortcutLastSeen) {
    ShortcutLastSeenRealm shortcutLastSeenRealm = null;

    if (shortcutLastSeen != null) {
      shortcutLastSeenRealm = new ShortcutLastSeenRealm();
      shortcutLastSeenRealm.setDate(shortcutLastSeen.getDate());
      shortcutLastSeen.setUserId(shortcutLastSeen.getUserId());
    }

    return shortcutLastSeenRealm;
  }

  public List<ShortcutLastSeen> transformListShortcutLastSeenRealm(
      Collection<ShortcutLastSeenRealm> shortcutCollection) {
    List<ShortcutLastSeen> list = new ArrayList<>();

    ShortcutLastSeen shortcut;
    if (shortcutCollection != null) {
      for (ShortcutLastSeenRealm shortcutRealm : shortcutCollection) {
        shortcut = transform(shortcutRealm);
        if (shortcut != null) {
          list.add(shortcut);
        }
      }
    }

    return list;
  }


  public RealmList<ShortcutLastSeenRealm> transformListShortcutLastSeen(
      Collection<ShortcutLastSeen> shortcutCollection) {
    RealmList<ShortcutLastSeenRealm> shortcutRealmList = new RealmList<>();
    ShortcutLastSeenRealm shortcutRealm;
    if (shortcutCollection != null) {
      for (ShortcutLastSeen shortcut : shortcutCollection) {
        shortcutRealm = transform(shortcut);
        if (shortcutRealm != null) {
          shortcutRealmList.add(shortcutRealm);
        }
      }
    }

    return shortcutRealmList;
  }

  public ShortcutLastSeen transform(ShortcutLastSeenRealm shortcutLastSeenRealm) {
    ShortcutLastSeen shortcutLastSeen = null;

    if (shortcutLastSeenRealm != null) {
      shortcutLastSeen = new ShortcutLastSeen();
      shortcutLastSeen.setUserId(shortcutLastSeenRealm.getUserId());
      shortcutLastSeen.setDate(shortcutLastSeenRealm.getDate());
    }
    return shortcutLastSeen;
  }

  public List<Shortcut> transform(Collection<ShortcutRealm> shortcutRealmCollection) {
    List<Shortcut> shortcutList = new ArrayList<>();

    Shortcut shortcut;
    if (shortcutRealmCollection != null) {
      for (ShortcutRealm shortcutRealm : shortcutRealmCollection) {
        shortcut = transform(shortcutRealm);
        if (shortcut != null) {
          shortcutList.add(shortcut);
        }
      }
    }

    return shortcutList;
  }

  public RealmList<ShortcutRealm> transformList(Collection<Shortcut> shortcutCollection) {
    RealmList<ShortcutRealm> shortcutRealmList = new RealmList<>();
    ShortcutRealm shortcutRealm;
    if (shortcutCollection != null) {
      for (Shortcut shortcut : shortcutCollection) {
        shortcutRealm = transform(shortcut);
        if (shortcutRealm != null) {
          shortcutRealmList.add(shortcutRealm);
        }
      }
    }

    return shortcutRealmList;
  }

  public void setUserRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
    this.userRealmDataMapper = userRealmDataMapper;
  }
}
