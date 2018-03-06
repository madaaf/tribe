package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.TrophiesManager;

/**
 * Created by tiago on 05/03/2018.
 */

public class TrophyRequirement {

  @StringDef({
      FRIENDS, DAYS_USAGE, GAMES_PLAYED, MULTIPLAYER_SESSIONS, BEST_SCORES
  }) public @interface TrophyRequirementType {
  }

  private static final String FRIENDS = "FRIENDS";
  private static final String DAYS_USAGE = "DAYS_USAGE";
  private static final String GAMES_PLAYED = "GAMES_PLAYED";
  private static final String MULTIPLAYER_SESSIONS = "MULTIPLAYER_SESSIONS";
  private static final String BEST_SCORES = "BEST_SCORES";

  private String requirement;
  private int count;
  private TrophiesManager trophiesManager;

  public TrophyRequirement(@TrophyRequirementType String trophyRequirement, int count) {
    this.requirement = trophyRequirement;
    this.count = count;
    this.trophiesManager = TrophiesManager.getInstance(null);
  }

  public int description() {
    if (requirement.equals(FRIENDS)) {
      return R.string.trophy_requirement_friends_description;
    } else if (requirement.equals(DAYS_USAGE)) {
      return R.string.trophy_requirement_day_usage_description;
    } else if (requirement.equals(GAMES_PLAYED)) {
      return R.string.trophy_requirement_games_played_description;
    } else if (requirement.equals(MULTIPLAYER_SESSIONS)) {
      return R.string.trophy_requirement_multiplayer_sessions_description;
    } else {
      return R.string.trophy_requirement_best_scores_description;
    }
  }

  public int title() {
    if (requirement.equals(FRIENDS)) {
      return R.string.trophy_requirement_friends_title;
    } else if (requirement.equals(DAYS_USAGE)) {
      return R.string.trophy_requirement_day_usage_title;
    } else if (requirement.equals(GAMES_PLAYED)) {
      return R.string.trophy_requirement_games_played_title;
    } else if (requirement.equals(MULTIPLAYER_SESSIONS)) {
      return R.string.trophy_requirement_multiplayer_sessions_title;
    } else {
      return R.string.trophy_requirement_best_scores_title;
    }
  }

  public boolean isAchieved() {
    return achievedCount() >= totalCount();
  }

  public int achievedCount() {
    if (requirement.equals(FRIENDS)) {
      return trophiesManager.friendsCount();
    } else if (requirement.equals(DAYS_USAGE)) {
      return R.string.trophy_requirement_day_usage_title;
    } else if (requirement.equals(GAMES_PLAYED)) {
      return R.string.trophy_requirement_games_played_title;
    } else if (requirement.equals(MULTIPLAYER_SESSIONS)) {
      return R.string.trophy_requirement_multiplayer_sessions_title;
    } else {
      return R.string.trophy_requirement_best_scores_title;
    }
  }

  public int totalCount() {
    if (requirement.equals(FRIENDS)) {
      return R.string.trophy_requirement_day_usage_title;
    } else if (requirement.equals(DAYS_USAGE)) {
      return R.string.trophy_requirement_day_usage_title;
    } else if (requirement.equals(GAMES_PLAYED)) {
      return R.string.trophy_requirement_games_played_title;
    } else if (requirement.equals(MULTIPLAYER_SESSIONS)) {
      return R.string.trophy_requirement_multiplayer_sessions_title;
    } else {
      return R.string.trophy_requirement_best_scores_title;
    }
  }
}

