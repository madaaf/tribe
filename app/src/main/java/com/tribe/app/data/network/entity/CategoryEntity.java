package com.tribe.app.data.network.entity;

import com.tribe.app.domain.entity.trivia.TriviaQuestions;
import java.util.List;

/**
 * Created by tiago on 21/12/2017.
 */

public class CategoryEntity {

  private String id;
  private List<TriviaQuestions> questions;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<TriviaQuestions> getQuestions() {
    return questions;
  }

  public void setQuestions(List<TriviaQuestions> questions) {
    this.questions = questions;
  }
}
