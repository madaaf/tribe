package com.tribe.app.data.realm.mapper;

import android.content.Context;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.Score;
import com.tribe.tribelivesdk.game.GameManager;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link ScoreRealm} (in the data layer)
 * to {@link Score} in the
 * domain layer.
 */
@Singleton public class ScoreRealmDataMapper {

  private UserRealmDataMapper userRealmDataMapper;
  private GameRealmDataMapper gameRealmDataMapper;
  private ScoreUserRealmDataMapper scoreUserRealmDataMapper;
  private GameManager gameManager;

  @Inject public ScoreRealmDataMapper(Context context, GameRealmDataMapper gameRealmDataMapper,
      ScoreUserRealmDataMapper scoreUserRealmDataMapper) {
    this.gameRealmDataMapper = gameRealmDataMapper;
    this.scoreUserRealmDataMapper = scoreUserRealmDataMapper;
    this.gameManager = GameManager.getInstance(context);
  }

  /**
   * Transform a {@link ScoreRealm} into an {@link Score}.
   *
   * @param scoreRealm Object to be transformed.
   * @return {@link Score} if valid {@link ScoreRealm} otherwise null.
   */
  public Score transform(ScoreRealm scoreRealm) {
    Score score = null;

    if (scoreRealm != null && gameManager.getGameById(scoreRealm.getGame_id()) != null) {
      score = new Score();
      score.setId(scoreRealm.getId());
      score.setValue(scoreRealm.getValue());
      score.setRanking(scoreRealm.getRanking());
      score.setGame(gameManager.getGameById(scoreRealm.getGame_id()));
      score.setUser(scoreUserRealmDataMapper.transform(scoreRealm.getUser()));
    }

    return score;
  }

  /**
   * Transform a {@link Score} into an {@link ScoreRealm}.
   *
   * @param score Object to be transformed.
   * @return {@link ScoreRealm} if valid {@link Score} otherwise null.
   */
  public ScoreRealm transform(Score score) {
    ScoreRealm scoreRealm = null;

    if (score != null) {
      scoreRealm = new ScoreRealm();
      scoreRealm.setId(score.getId());
      scoreRealm.setValue(score.getValue());
      scoreRealm.setRanking(score.getRanking());
      scoreRealm.setGame_id(score.getGame().getId());
      scoreRealm.setUser(scoreUserRealmDataMapper.transform(score.getUser()));
    }

    return scoreRealm;
  }

  public List<Score> transform(Collection<ScoreRealm> scoreRealmCollection) {
    List<Score> scoreList = new ArrayList<>();

    Score score;
    if (scoreRealmCollection != null) {
      for (ScoreRealm scoreRealm : scoreRealmCollection) {
        score = transform(scoreRealm);
        if (score != null) {
          scoreList.add(score);
        }
      }
    }

    return scoreList;
  }

  public RealmList<ScoreRealm> transformList(Collection<Score> scoreCollection) {
    RealmList<ScoreRealm> scoreRealmList = new RealmList<>();
    ScoreRealm scoreRealm;
    if (scoreCollection != null) {
      for (Score score : scoreCollection) {
        scoreRealm = transform(score);
        if (scoreRealm != null) {
          scoreRealmList.add(scoreRealm);
        }
      }
    }

    return scoreRealmList;
  }

  public void setUserRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
    this.userRealmDataMapper = userRealmDataMapper;
  }
}
