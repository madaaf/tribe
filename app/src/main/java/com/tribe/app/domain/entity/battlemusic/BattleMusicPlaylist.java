package com.tribe.app.domain.entity.battlemusic;

import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tiago on 18/01/2018.
 */

public class BattleMusicPlaylist {

  private String title;
  private List<BattleMusicTrack> tracks;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<BattleMusicTrack> getTracks() {
    return tracks;
  }

  public void setTracks(List<BattleMusicTrack> tracks) {
    this.tracks = tracks;
  }

  public List<BattleMusicTrack> getRandomTracks(int nb) {
    List<BattleMusicTrack> list = new ArrayList<>(tracks);
    Collections.shuffle(list);
    return list.subList(0, nb);
  }
}
