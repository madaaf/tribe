package com.tribe.app.data.repository.user.contact;

import android.provider.ContactsContract.Contacts;

public enum Filter {

    HAS_PHONE(Contacts.HAS_PHONE_NUMBER, ">0");

    private String field;
    private String condition;

    Filter(String field, String condition) {
        this.field = field;
        this.condition = condition;
    }

    @Override
    public String toString() {
        return field + " " + condition;
    }
}
