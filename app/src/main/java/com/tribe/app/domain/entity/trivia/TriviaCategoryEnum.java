package com.tribe.app.domain.entity.trivia;

import java.util.ArrayList;
import java.util.Collections;
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
  private List<TriviaQuestion> questions;

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

  public static List<TriviaQuestion> getRandomQuestions(int nb, TriviaCategoryEnum category) {
    List<TriviaQuestion> list = new ArrayList<>(category.getQuestions());
    Collections.shuffle(list);
    return list.subList(0, nb);
  }

  public int getId() {
    return id;
  }

  public String getCategory() {
    return category;
  }

  public void setQuestions(List<TriviaQuestion> questions) {
    this.questions = questions;
  }

  public List<TriviaQuestion> getQuestions() {
    return questions;
  }

  public static void setQuestionsForCategory(String key, List<TriviaQuestion> questions) {
    for (TriviaCategoryEnum category : getCategories()) {
      if (category.getCategory().equals(key)) category.setQuestions(questions);
    }
  }
}
