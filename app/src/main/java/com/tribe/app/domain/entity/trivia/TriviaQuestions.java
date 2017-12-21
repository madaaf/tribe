package com.tribe.app.domain.entity.trivia;

import java.util.List;

/**
 * Created by tiago on 21/12/2017.
 */

public class TriviaQuestions {

  private String id;
  private String question;
  private String answer;
  private List<String> alternativeAnswers;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public List<String> getAlternativeAnswers() {
    return alternativeAnswers;
  }

  public void setAlternativeAnswers(List<String> alternativeAnswers) {
    this.alternativeAnswers = alternativeAnswers;
  }
}
