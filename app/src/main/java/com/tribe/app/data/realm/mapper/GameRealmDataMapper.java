package com.tribe.app.data.realm.mapper;

import android.content.Context;
import com.tribe.app.data.realm.AnimationIconRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameDraw;
import com.tribe.tribelivesdk.game.GamePostIt;
import com.tribe.tribelivesdk.model.TribeGuest;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link GameRealm} (in the data layer)
 * to {@link Game} in the
 * domain layer.
 */
@Singleton public class GameRealmDataMapper {

  private Context context;

  @Inject public GameRealmDataMapper(Context context) {
    this.context = context;
  }

  /**
   * Transform a {@link GameRealm} into an {@link Game}.
   *
   * @param gameRealm Object to be transformed.
   * @return {@link Game} if valid {@link GameRealm} otherwise null.
   */
  public Game transform(GameRealm gameRealm) {
    Game game = null;

    if (gameRealm != null) {
      if (gameRealm.getId().equals(Game.GAME_DRAW)) {
        game = new GameDraw(context, gameRealm.getId());
      } else if (gameRealm.getId().equals(Game.GAME_CHALLENGE)) {
        game = new GameChallenge(context, gameRealm.getId());
      } else if (gameRealm.getId().equals(Game.GAME_POST_IT)) {
        game = new GamePostIt(context, gameRealm.getId());
      } else {
        game = new Game(context, gameRealm.getId());
      }

      game.set__typename(gameRealm.get__typename());
      game.setTitle(gameRealm.getTitle());
      game.setBaseline(gameRealm.getBaseline());
      game.setFeatured(gameRealm.isFeatured());
      game.setHasScores(gameRealm.hasScores());
      game.setIcon(gameRealm.getIcon());
      game.setPlayable(gameRealm.isPlayable());
      game.setNew(gameRealm.isNew());
      game.setOnline(gameRealm.isOnline());
      game.setPlays_count(gameRealm.getPlays_count());
      game.setPrimary_color(gameRealm.getPrimary_color());
      game.setSecondary_color(gameRealm.getSecondary_color());
      game.setIn_home(gameRealm.isIn_home());
      game.setPriority(gameRealm.getPriority());
      game.setUses_gamemaster(gameRealm.isUses_gamemaster());
      game.setMin_android_version(gameRealm.getMin_android_version());

      List<String> animation_icons = new ArrayList<>();
      for (AnimationIconRealm iconRealm : gameRealm.getAnimation_icons())
        animation_icons.add(iconRealm.getUrl());
      game.setAnimation_icons(animation_icons);
      game.setLogo(gameRealm.getLogo());
      game.setBackground(gameRealm.getBackground());

      if (gameRealm.getFriendLeaderScoreUser() != null) {
        TribeGuest guest = new TribeGuest(gameRealm.getFriendLeaderScoreUser().getId(),
            gameRealm.getFriendLeaderScoreUser().getDisplay_name(),
            gameRealm.getFriendLeaderScoreUser().getPicture(), false, false, null, null);
        guest.setScoreValue(gameRealm.getFriendLeaderScoreUser().getValue());
        guest.setRankingValue(gameRealm.getFriendLeaderScoreUser().getRanking());
        game.setFriendLeader(guest);
      }
      game.setEmoji(gameRealm.getEmoji());
      game.setUrl(gameRealm.getUrl());
    }

    return game;
  }

  /**
   * Transform a {@link Game} into an {@link GameRealm}.
   *
   * @param game Object to be transformed.
   * @return {@link GameRealm} if valid {@link Game} otherwise null.
   */
  public GameRealm transform(Game game) {
    GameRealm gameRealm = null;

    if (game != null) {
      gameRealm = new GameRealm();
      gameRealm.setId(game.getId());
      gameRealm.set__typename(game.get__typename());
      gameRealm.setTitle(game.getTitle());
      gameRealm.setBaseline(game.getBaseline());
      gameRealm.setFeatured(game.isFeatured());
      gameRealm.setHas_scores(game.hasScores());
      gameRealm.setIcon(game.getIcon());
      gameRealm.setPlayable(game.isPlayable());
      gameRealm.setNew(game.isNew());
      gameRealm.setOnline(game.isOnline());
      gameRealm.setPlays_count(game.getPlays_count());
      gameRealm.setPrimary_color(game.getPrimary_color());
      gameRealm.setSecondary_color(game.getSecondary_color());
      gameRealm.setUrl(game.getUrl());
      gameRealm.setLogo(game.getLogo());
      gameRealm.setBackground(game.getBackground());
      gameRealm.setPriority(game.getPriority());
      gameRealm.setIn_home(game.isIn_home());
      gameRealm.setMin_android_version(game.getMin_android_version());
      gameRealm.setUses_gamemaster(game.isUses_gamemaster());
    }

    return gameRealm;
  }

  public List<Game> transform(Collection<GameRealm> gameRealmCollection) {
    List<Game> GameList = new ArrayList<>();

    Game game;
    if (gameRealmCollection != null) {
      for (GameRealm gameRealm : gameRealmCollection) {
        game = transform(gameRealm);
        if (game != null) {
          GameList.add(game);
        }
      }
    }

    return GameList;
  }

  public RealmList<GameRealm> transformList(Collection<Game> gameCollection) {
    RealmList<GameRealm> gameRealmList = new RealmList<>();
    GameRealm gameRealm;
    if (gameCollection != null) {
      for (Game Game : gameCollection) {
        gameRealm = transform(Game);
        if (gameRealm != null) {
          gameRealmList.add(gameRealm);
        }
      }
    }

    return gameRealmList;
  }
}
