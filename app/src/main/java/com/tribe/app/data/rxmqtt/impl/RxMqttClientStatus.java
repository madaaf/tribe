package com.tribe.app.data.rxmqtt.impl;

import com.tribe.app.data.rxmqtt.enums.RxMqttClientState;

import java.sql.Timestamp;

public class RxMqttClientStatus implements Cloneable {

    private Timestamp logTime;
    private int state;

    public RxMqttClientStatus() {
        this.state = RxMqttClientState.INIT;
    }

    public Timestamp getLogTime() {
        return logTime;
    }

    public void setLogTime(Timestamp logTime) {
        this.logTime = logTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("time:%sm state:%s", getLogTime(), getState());
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
