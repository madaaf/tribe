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

    List<TriviaQuestion> triviaQuestionsList = new ArrayList<>();
    Gson gson = new Gson();
    Type listType = new TypeToken<List<String>>() {
    }.getType();

    if (je.isJsonObject()) {
      JsonArray results = je.getAsJsonObject().getAsJsonArray("results");

      if (results != null) {
        for (final JsonElement jsonElement : results) {
          if (!jsonElement.isJsonNull()) {
            JsonObject jo = jsonElement.getAsJsonObject();
            TriviaQuestion triviaQuestion = new TriviaQuestion();
            triviaQuestion.setAnswer(jo.get("correct_answer").getAsString());
            List<String> alternativeAnswers = gson.fromJson(jo.get("incorrect_answers"), listType);
            alternativeAnswers.add(triviaQuestion.getAnswer());
            triviaQuestion.setAlternativeAnswers(alternativeAnswers);
            triviaQuestion.setQuestion(jo.get("question").getAsString());
            triviaQuestionsList.add(triviaQuestion);
          }
        }
      }
    } else {
      JsonArray jsonArray = je.getAsJsonArray();

      for (JsonElement triviaQuestionElement : jsonArray) {
        if (triviaQuestionElement != null &&
            !triviaQuestionElement.isJsonNull() &&
            triviaQuestionElement.isJsonObject()) {
          JsonObject obj = triviaQuestionElement.getAsJsonObject();
          TriviaQuestion triviaQuestion = new TriviaQuestion();
          triviaQuestion.setId(obj.get("id").getAsString());
          triviaQuestion.setAnswer(obj.get("answer").getAsString());
          triviaQuestion.setQuestion(obj.get("question").getAsString());
          List<String> alternativeAnswers = gson.fromJson(obj.get("alternativeAnswers"), listType);
          alternativeAnswers.add(triviaQuestion.getAnswer());
          triviaQuestion.setAlternativeAnswers(alternativeAnswers);
          triviaQuestionsList.add(triviaQuestion);
        }
      }
    }

    return triviaQuestionsList;
  }
}