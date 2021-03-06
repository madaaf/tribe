package com.tribe.app.data.realm;

import com.tribe.app.presentation.utils.StringUtils;

import java.util.Date;

import io.realm.RealmObject;
import javax.inject.Singleton;

@Singleton public class AccessToken extends RealmObject {

  private String access_token;
  private String token_type;
  private String refresh_token;
  private String user_id;
  private Date access_expires_at;

  public String getAccessToken() {
    return access_token;
  }

  public void setAccessToken(String access_token) {
    this.access_token = access_token;
  }

  public String getTokenType() {
    if (!StringUtils.isEmpty(token_type) && !Character.isUpperCase(token_type.charAt(0))) {
      token_type = Character.toString(token_type.charAt(0)).toUpperCase() + token_type.substring(1);
    }

    return token_type;
  }

  public void setTokenType(String token_type) {
    this.token_type = token_type;
  }

  public String getRefreshToken() {
    return refresh_token;
  }

  public void setRefreshToken(String refresh_token) {
    this.refresh_token = refresh_token;
  }

  public String getUserId() {
    return user_id;
  }

  public void setUserId(String user_id) {
    this.user_id = user_id;
  }

  public Date getAccessExpiresAt() {
    return access_expires_at;
  }

  public void setAccessExpiresAt(Date access_expires_at) {
    this.access_expires_at = access_expires_at;
  }

  public boolean isAnonymous() {
    return user_id == null || user_id.startsWith("anon__");
  }

  public void copy(AccessToken accessToken) {
    this.access_token = accessToken.access_token;
    this.token_type = accessToken.token_type;
    this.refresh_token = accessToken.refresh_token;
    this.user_id = accessToken.user_id;
    this.access_expires_at = accessToken.access_expires_at;
  }

  public void clear() {
    access_token = null;
    token_type = null;
    refresh_token = null;
    user_id = null;
    access_expires_at = null;
  }
}
