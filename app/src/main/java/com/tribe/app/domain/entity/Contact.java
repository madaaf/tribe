package com.tribe.app.domain.entity;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by tiago on 02/09/2016.
 */
public class Contact implements Comparable<Contact> {

    public String id;
    public String name;
    public List<String> phones;
    public long lastTimeContacted;

    public Contact(String id) {
        this.id = id;
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

    @Override
    public int compareTo(Contact another) {
        return name != null && another.name != null ? name.compareToIgnoreCase(another.name) : -1;
    }
}
