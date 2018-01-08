package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TriviaQuestionsDeserializer implements JsonDeserializer<List<TriviaQuestion>> {

  @Override public List<TriviaQuestion> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonArray results = je.getAsJsonObject().getAsJsonArray("results");
    List<TriviaQuestion> triviaQuestionsList = new ArrayList<>();
    Gson gson = new Gson();
    Type listType = new TypeToken<List<String>>() {
    }.getType();

    if (results != null) {
      for (final JsonElement jsonElement : results) {
        if (!jsonElement.isJsonNull()) {
          JsonObject jo = jsonElement.getAsJsonObject();
          TriviaQuestion triviaQuestions = new TriviaQuestion();
          triviaQuestions.setAnswer(jo.get("correct_answer").getAsString());
          triviaQuestions.setAlternativeAnswers(
              gson.fromJson(jo.get("incorrect_answers"), listType));
          triviaQuestions.setQuestion(jo.get("question").getAsString());
          triviaQuestionsList.add(triviaQuestions);
        }
      }
    }

    return triviaQuestionsList;
  }
}