package com.tribe.app.domain.entity;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by tiago on 02/09/2016.
 */
public class ContactAB extends Contact {

    private List<String> phones;
    private long lastTimeContacted;
    private int version;

    public ContactAB(String id) {
        super(id);
    }

    /**
     * Get first phone if available
     * @return
     */
    @Nullable
    public String getPhone() {
        return !phones.isEmpty() ? phones.get(0) : null;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s\tphones=%s",
                id, String.valueOf(name), String.valueOf(phones));
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public long getLastTimeContacted() {
        return lastTimeContacted;
    }

    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
