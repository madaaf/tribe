package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.GameFile;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 01/18/2017.
 */
public interface GameMVPView extends MVPView {

  Context context();

  void onGameLeaderboard(List<Score> scoreList, boolean cloud);

  void onUserLeaderboard(List<Score> scoreList, boolean cloud);

  void onFriendsScore(List<Score> scoreList, boolean cloud);

  void onTriviaData(Map<String, List<TriviaQuestion>> map);

  void onBattleMusicData(Map<String, BattleMusicPlaylist> map);

  void onGameList(List<Game> gameList);

  void onGameFile(GameFile gameFile);
  
  void onGameData(List<String> data);

  void onUserBestScore(Score score);

  void onLookupContacts(List<Contact> contactList);

  void successFacebookLogin();

  void errorFacebookLogin();

  void onLookupContactsError(String message);
}
