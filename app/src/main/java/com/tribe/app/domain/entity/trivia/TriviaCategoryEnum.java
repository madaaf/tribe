package com.tribe.app.domain.entity.trivia;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by tiago on 12/12/2017.
 */

public enum TriviaCategoryEnum {

  MOVIES(11, "movie"), MUSIC(12, "music"), TV(14, "tv"), CELEBS(26, "celebs"), SPORTS(21,
      "sports"), GEEKS(18, "geeks"), GENERAL(19, "general"), WORLD(22, "world"), GAMES(15, "games");

  private int id;
  private String category;
  private List<TriviaQuestions> questions;

  TriviaCategoryEnum(int id, String category) {
    this.id = id;
    this.category = category;
  }

  public static List<TriviaCategoryEnum> getCategories() {
    return new ArrayList<>(EnumSet.allOf(TriviaCategoryEnum.class));
  }

  public static TriviaCategoryEnum getCategory(String category) {
    for (TriviaCategoryEnum cat : getCategories()) {
      if (cat.getCategory().equals(category)) return cat;
    }

    return null;
  }

  public int getId() {
    return id;
  }

  public String getCategory() {
    return category;
  }

  public void setQuestions(List<TriviaQuestions> questions) {
    this.questions = questions;
  }

  public List<TriviaQuestions> getQuestions() {
    return questions;
  }
}
