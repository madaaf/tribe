package com.tribe.app.data.network.entity;

import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.List;

/**
 * Created by tiago on 21/12/2017.
 */

public class CategoryEntity {

  private String id;
  private List<TriviaQuestion> questions;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<TriviaQuestion> getQuestions() {
    return questions;
  }

  public void setQuestions(List<TriviaQuestion> questions) {
    this.questions = questions;
  }
}
