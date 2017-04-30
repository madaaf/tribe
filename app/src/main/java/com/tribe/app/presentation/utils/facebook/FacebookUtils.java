package com.tribe.app.presentation.utils.facebook;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

/**
 * Created by horatiothomas on 8/25/16.
 */
public class FacebookUtils {

  public static boolean isLoggedIn() {
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    return accessToken != null;
  }

  public static AccessToken accessToken() {
    return AccessToken.getCurrentAccessToken();
  }

  public static void logout() {
    LoginManager.getInstance().logOut();
  }
}
