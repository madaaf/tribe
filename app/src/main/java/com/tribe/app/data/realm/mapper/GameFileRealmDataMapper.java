package com.tribe.app.data.realm.mapper;

import android.content.Context;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.domain.entity.GameFile;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link GameFileRealm} (in the data layer)
 * to {@link GameFile} in the
 * domain layer.
 */
@Singleton public class GameFileRealmDataMapper {

  private Context context;

  @Inject public GameFileRealmDataMapper(Context context) {
    this.context = context;
  }

  /**
   * Transform a {@link GameFileRealm} into an {@link GameFile}.
   *
   * @param gameFileRealm Object to be transformed.
   * @return {@link GameFile} if valid {@link GameFileRealm} otherwise null.
   */
  public GameFile transform(GameFileRealm gameFileRealm) {
    GameFile gameFile = null;

    if (gameFileRealm != null) {
      gameFile = new GameFile();
      gameFile.setUrl(gameFileRealm.getUrl());
      gameFile.setDownloadStatus(gameFileRealm.getDownloadStatus());
      gameFile.setGameId(gameFileRealm.getGameId());
      gameFile.setPath(gameFileRealm.getPath());
      gameFile.setProgress(gameFileRealm.getProgress());
      gameFile.setTotalSize(gameFileRealm.getTotalSize());
    }

    return gameFile;
  }

  /**
   * Transform a {@link GameFile} into an {@link GameFileRealm}.
   *
   * @param gameFile Object to be transformed.
   * @return {@link GameFileRealm} if valid {@link GameFile} otherwise null.
   */
  public GameFileRealm transform(GameFile gameFile) {
    GameFileRealm gameFileRealm = null;

    if (gameFile != null) {
      gameFileRealm = new GameFileRealm();
      gameFileRealm.setUrl(gameFile.getUrl());
      gameFileRealm.setGameId(gameFile.getGameId());
      gameFileRealm.setDownloadStatus(gameFile.getDownloadStatus());
      gameFileRealm.setTotalSize(gameFile.getTotalSize());
      gameFileRealm.setProgress(gameFile.getProgress());
      gameFileRealm.setPath(gameFile.getPath());
    }

    return gameFileRealm;
  }

  public List<GameFile> transform(Collection<GameFileRealm> gameFileRealmCollection) {
    List<GameFile> gameFileList = new ArrayList<>();

    GameFile gameFile;
    if (gameFileRealmCollection != null) {
      for (GameFileRealm gameFileRealm : gameFileRealmCollection) {
        gameFile = transform(gameFileRealm);
        if (gameFile != null) {
          gameFileList.add(gameFile);
        }
      }
    }

    return gameFileList;
  }

  public RealmList<GameFileRealm> transformList(Collection<GameFile> gameFileCollection) {
    RealmList<GameFileRealm> gameFileRealmList = new RealmList<>();
    GameFileRealm gameFileRealm;
    if (gameFileCollection != null) {
      for (GameFile gameFile : gameFileCollection) {
        gameFileRealm = transform(gameFile);
        if (gameFileRealm != null) {
          gameFileRealmList.add(gameFileRealm);
        }
      }
    }

    return gameFileRealmList;
  }
}
