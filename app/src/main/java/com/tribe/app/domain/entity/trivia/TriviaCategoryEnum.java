package com.tribe.app.domain.entity.trivia;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by tiago on 12/12/2017.
 */

public enum TriviaCategoryEnum {

  MOVIES("movie", 11), MUSIC("music", 12), TV("tv", 14), CELEBS("celebs", 26), SPORTS("sports",
      21), GEEKS("geeks", 18), GENERAL("general", 9), WORLD("world", 22), GAMES("games", 15);

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
