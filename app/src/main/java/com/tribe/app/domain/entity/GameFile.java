package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 01/03/2018.
 */

public class GameFile implements Serializable {

  private String url;
  private String gameId;
  private String path;
  private String downloadStatus;
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

  @Override public String toString() {
    return "Url : " +
        url +
        "\n Path : " +
        path +
        "\n DownloadStatus : " +
        downloadStatus +
        "\n Progress : " +
        progress +
        "\n Total Size : " +
        totalSize +
        "\n Game Id : " +
        gameId;
  }
}
