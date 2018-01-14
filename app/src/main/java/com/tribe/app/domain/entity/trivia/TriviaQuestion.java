package com.tribe.app.domain.entity.trivia;

import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 21/12/2017.
 */

public class TriviaQuestion {

  private static final String ID_KEY = "id";
  private static final String QUESTION_KEY = "question";
  private static final String ANSWER_KEY = "answer";
  private static final String ALTERNATIVE_ANSWERS_KEY = "alternativeAnswers";

  private String id;
  private String question;
  private String answer;
  private List<String> alternativeAnswers;

  public TriviaQuestion() {
  }

  public TriviaQuestion(JSONObject json) {
    try {
      this.id = json.getString(ID_KEY);
      this.question = json.getString(QUESTION_KEY);
      this.answer = json.getString(ANSWER_KEY);
      this.alternativeAnswers = new ArrayList<>();

      JSONArray array = json.getJSONArray(ALTERNATIVE_ANSWERS_KEY);
      for (int i = 0; i < array.length(); i++) {
        this.alternativeAnswers.add(array.getString(i));
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

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

  public JSONObject asJSON() {
    JSONObject question = new JSONObject();
    JsonUtils.jsonPut(question, ID_KEY, id);
    JsonUtils.jsonPut(question, ANSWER_KEY, this.answer);
    JsonUtils.jsonPut(question, QUESTION_KEY, this.question);
    JSONArray alternativeAnswersArray = new JSONArray();
    for (String alternativeAnswer : this.alternativeAnswers) alternativeAnswersArray.put(alternativeAnswer);
    JsonUtils.jsonPut(question, ALTERNATIVE_ANSWERS_KEY, alternativeAnswersArray);
    return question;
  }
}
