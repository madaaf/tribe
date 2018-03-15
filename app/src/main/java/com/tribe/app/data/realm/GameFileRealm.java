package com.tribe.app.data.realm;

import android.support.annotation.StringDef;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 01/03/2018.
 */

public class GameFileRealm extends RealmObject {

  @StringDef({ STATUS_TO_DOWNLOAD, STATUS_DOWNLOADED, STATUS_PENDING })
  public @interface DownloadStatus {
  }

  public static final String STATUS_TO_DOWNLOAD = "STATUS_TO_DOWNLOAD";
  public static final String STATUS_DOWNLOADED = "STATUS_DOWNLOADED";
  public static final String STATUS_DOWNLOADING = "STATUS_DOWNLOADING";
  public static final String STATUS_PENDING = "STATUS_PENDING";

  public static final String PATH = "path";
  public static final String URL = "url";
  public static final String DOWNLOAD_STATUS = "downloadStatus";
  public static final String PROGRESS = "progress";
  public static final String TOTAL_SIZE = "totalSize";

  @PrimaryKey private String url;
  @Index private String gameId;
  private String path;
  private @DownloadStatus String downloadStatus;
  private int progress = 0;
  private int totalSize = 0;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setDownloadStatus(String downloadStatus) {
    this.downloadStatus = downloadStatus;
  }

  public String getDownloadStatus() {
    return downloadStatus;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public int getProgress() {
    return progress;
  }

  public void setTotalSize(int totalSize) {
    this.totalSize = totalSize;
  }

  public int getTotalSize() {
    return totalSize;
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }
}
