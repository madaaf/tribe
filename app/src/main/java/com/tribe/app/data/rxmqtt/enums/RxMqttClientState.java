package com.tribe.app.data.rxmqtt.enums;

public class RxMqttClientState {

    public static int INIT = 0;
    public static int CONNECTING = 1;
    public static int CONNECTED = 2;
    public static int CONNECTING_FAILED = 3;
    public static int CONNECTION_LOST = 4;
    public static int TRY_DISCONNECT = 5;
    public static int DISCONNECTED = 6;

    private int code;

    RxMqttClientState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
