package com.tribe.tribelivesdk.model.error;

import android.support.annotation.IntDef;

/**
 * Created by tiago on 26/01/2017.
 */

public class WebSocketError {

  @IntDef({
      ERROR_CODE_ROOM_IS_FULL
  }) public @interface ErrorType {
  }

  public static final int ERROR_CODE_SYSTEM = 0x500;
  public static final int ERROR_CODE_DB = 0x501;
  public static final int ERROR_CODE_JSON = 0x502;
  public static final int ERROR_CODE_JSON_FIELD_MISSING = 0x503;
  public static final int ERROR_CODE_USER_ALREADY_EXIST = 0x504;
  public static final int ERROR_CODE_USER_UNKNOWN = 0x505;
  public static final int ERROR_CODE_AUTH_FAILED = 0x506;
  public static final int ERROR_CODE_FILE_NOT_EXIST = 0x507;
  public static final int ERROR_CODE_ENCRYPT = 0x508;
  public static final int ERROR_CODE_RMQ = 0x509;
  public static final int ERROR_CODE_UNKNOWN_ACTION = 0x510;
  public static final int ERROR_CODE_USER_NOT_AUTHENTICATED = 0x511;
  public static final int ERROR_CODE_SOCKET_ID_DOES_NOT_EXIST = 0x512;
  public static final int ERROR_CODE_ROOM_EMPTY = 0x513;
  public static final int ERROR_CODE_REGEXP = 0x514;
  public static final int ERROR_CODE_JANUS_CREATE_ROOM = 0x515;
  public static final int ERROR_CODE_JANUS_JOIN = 0x516;
  public static final int ERROR_CODE_JANUS_CONFIGURE = 0x517;
  public static final int ERROR_CODE_JANUS_TRICKLE_ICE = 0x518;
  public static final int ERROR_CODE_JANUS_ATTACH = 0x519;
  public static final int ERROR_CODE_JANUS_START_ROOM = 0x520;
  public static final int ERROR_CODE_UNITGROUP_NOT_FOUND = 0x521;
  public static final int ERROR_CODE_ROOM_IS_NOT_JOINABLE = 0x522;
  public static final int ERROR_CODE_ROOM_ALREADY_JOINED = 0x523;
  public static final int ERROR_CODE_ROOM_IS_FULL = 0x524;
  public static final int ERROR_CODE_SOCKETID_DOES_NOT_EXIST = 0x525;
  public static final int ERROR_CODE_JANUS_SESSION = 0x526;
  public static final int ERROR_CODE_SESSION = 0x527;

  private String message;
  private int id;

  public WebSocketError(int id, String message) {
    this.id = id;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public String getStringId() {
    switch (id) {
      case ERROR_CODE_SYSTEM:
        return "ERROR_CODE_SYSTEM";
      case ERROR_CODE_DB:
        return "ERROR_CODE_DB";
      case ERROR_CODE_JSON:
        return "ERROR_CODE_JSON";
      case ERROR_CODE_JSON_FIELD_MISSING:
        return "ERROR_CODE_JSON_FIELD_MISSING";
      case ERROR_CODE_USER_ALREADY_EXIST:
        return "ERROR_CODE_USER_ALREADY_EXIST";
      case ERROR_CODE_USER_UNKNOWN:
        return "ERROR_CODE_USER_UNKNOWN";
      case ERROR_CODE_AUTH_FAILED:
        return "ERROR_CODE_AUTH_FAILED";
      case ERROR_CODE_FILE_NOT_EXIST:
        return "ERROR_CODE_FILE_NOT_EXIST";
      case ERROR_CODE_ENCRYPT:
        return "ERROR_CODE_ENCRYPT";
      case ERROR_CODE_RMQ:
        return "ERROR_CODE_RMQ";
      case ERROR_CODE_UNKNOWN_ACTION:
        return "ERROR_CODE_UNKNOWN_ACTION";
      case ERROR_CODE_USER_NOT_AUTHENTICATED:
        return "ERROR_CODE_USER_NOT_AUTHENTICATED";
      case ERROR_CODE_SOCKET_ID_DOES_NOT_EXIST:
        return "ERROR_CODE_SOCKET_ID_DOES_NOT_EXIST";
      case ERROR_CODE_ROOM_EMPTY:
        return "ERROR_CODE_ROOM_EMPTY";
      case ERROR_CODE_REGEXP:
        return "ERROR_CODE_REGEXP";
      case ERROR_CODE_JANUS_CREATE_ROOM:
        return "ERROR_CODE_JANUS_CREATE_ROOM";
      case ERROR_CODE_JANUS_JOIN:
        return "ERROR_CODE_JANUS_JOIN";
      case ERROR_CODE_JANUS_CONFIGURE:
        return "ERROR_CODE_JANUS_CONFIGURE";
      case ERROR_CODE_JANUS_TRICKLE_ICE:
        return "ERROR_CODE_JANUS_TRICKLE_ICE";
      case ERROR_CODE_JANUS_ATTACH:
        return "ERROR_CODE_JANUS_ATTACH";
      case ERROR_CODE_JANUS_START_ROOM:
        return "ERROR_CODE_JANUS_START_ROOM";
      case ERROR_CODE_UNITGROUP_NOT_FOUND:
        return "ERROR_CODE_UNITGROUP_NOT_FOUND";
      case ERROR_CODE_ROOM_IS_NOT_JOINABLE:
        return "ERROR_CODE_ROOM_IS_NOT_JOINABLE";
      case ERROR_CODE_ROOM_ALREADY_JOINED:
        return "ERROR_CODE_ROOM_ALREADY_JOINED";
      case ERROR_CODE_ROOM_IS_FULL:
        return "ERROR_CODE_ROOM_IS_FULL";
      case ERROR_CODE_SOCKETID_DOES_NOT_EXIST:
        return "ERROR_CODE_SOCKETID_DOES_NOT_EXIST";
      case ERROR_CODE_JANUS_SESSION:
        return "ERROR_CODE_JANUS_SESSION";
      case ERROR_CODE_SESSION:
        return "ERROR_CODE_SESSION";

      default:
        return "";
    }
  }

  public int getId() {
    return id;
  }
}
