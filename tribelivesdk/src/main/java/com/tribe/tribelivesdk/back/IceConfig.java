package com.tribe.tribelivesdk.back;

import java.util.List;

/**
 * Created by tiago on 13/01/2017.
 */

public class IceConfig {

  private List<String> urls;
  private String username;
  private String credential;

  public IceConfig() {
  }

  public IceConfig(List<String> urls, String username, String credential) {
    this.urls = urls;
    this.username = username;
    this.credential = credential;
  }

  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }

  public String getUsername() {
    return username == null ? "" : username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCredential() {
    return credential == null ? "" : credential;
  }

  public void setCredential(String credential) {
    this.credential = credential;
  }

  @Override public String toString() {
    return "url : " + urls + "\n username : " + username + "\n credential : " + credential + "\n";
  }
}
