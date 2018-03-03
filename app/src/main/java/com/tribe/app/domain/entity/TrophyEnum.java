package com.tribe.app.domain.entity;

import com.tribe.app.R;
import com.tribe.app.data.realm.UserRealm;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Resource;

/**
 * Created by tiago on 02/03/2018.
 */

public enum TrophyEnum {
  NOOB(UserRealm.NOOB), EXPERT(UserRealm.EXPERT), PRO(UserRealm.PRO), MASTER(UserRealm.MASTER), GOD(
      UserRealm.GOD);

  private String trophy;

  TrophyEnum(String trophy) {
    this.trophy = trophy;
  }

  public static List<TrophyEnum> getTrophies() {
    return new ArrayList<>(EnumSet.allOf(TrophyEnum.class));
  }

  public @Resource int getTitle() {
    if (trophy.equals(UserRealm.NOOB)) {
      return R.string.trophy_noob_title;
    } else if (trophy.equals(UserRealm.EXPERT)) {
      return R.string.trophy_expert_title;
    } else if (trophy.equals(UserRealm.PRO)) {
      return R.string.trophy_pro_title;
    } else if (trophy.equals(UserRealm.MASTER)) {
      return R.string.trophy_master_title;
    } else if (trophy.equals(UserRealm.GOD)) {
      return R.string.trophy_god_title;
    }

    return NOOB.getTitle();
  }

  public @Resource int getIcon() {
    if (trophy.equals(UserRealm.NOOB)) {
      return R.drawable.picto_trophy_noob;
    } else if (trophy.equals(UserRealm.EXPERT)) {
      return R.drawable.picto_trophy_expert;
    } else if (trophy.equals(UserRealm.PRO)) {
      return R.drawable.picto_trophy_pro;
    } else if (trophy.equals(UserRealm.MASTER)) {
      return R.drawable.picto_trophy_master;
    } else if (trophy.equals(UserRealm.GOD)) {
      return R.drawable.picto_trophy_god;
    }

    return NOOB.getIcon();
  }

  public @Resource int getIconLocked() {
    if (trophy.equals(UserRealm.NOOB)) {
      return R.drawable.picto_trophy_noob_locked;
    } else if (trophy.equals(UserRealm.EXPERT)) {
      return R.drawable.picto_trophy_expert_locked;
    } else if (trophy.equals(UserRealm.PRO)) {
      return R.drawable.picto_trophy_pro_locked;
    } else if (trophy.equals(UserRealm.MASTER)) {
      return R.drawable.picto_trophy_master_locked;
    } else if (trophy.equals(UserRealm.GOD)) {
      return R.drawable.picto_trophy_god_locked;
    }

    return NOOB.getIconLocked();
  }

  public @Resource int getPrimaryColor() {
    if (trophy.equals(UserRealm.NOOB)) {
      return R.color.trophy_noob_primary;
    } else if (trophy.equals(UserRealm.EXPERT)) {
      return R.color.trophy_expert_primary;
    } else if (trophy.equals(UserRealm.PRO)) {
      return R.color.trophy_pro_primary;
    } else if (trophy.equals(UserRealm.MASTER)) {
      return R.color.trophy_master_primary;
    } else if (trophy.equals(UserRealm.GOD)) {
      return R.color.trophy_god_primary;
    }

    return NOOB.getPrimaryColor();
  }

  public @Resource int getSecondaryColor() {
    if (trophy.equals(UserRealm.NOOB)) {
      return R.color.trophy_noob_secondary;
    } else if (trophy.equals(UserRealm.EXPERT)) {
      return R.color.trophy_expert_secondary;
    } else if (trophy.equals(UserRealm.PRO)) {
      return R.color.trophy_pro_secondary;
    } else if (trophy.equals(UserRealm.MASTER)) {
      return R.color.trophy_master_secondary;
    } else if (trophy.equals(UserRealm.GOD)) {
      return R.color.trophy_god_secondary;
    }

    return NOOB.getSecondaryColor();
  }
}

