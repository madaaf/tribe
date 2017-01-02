package com.tribe.app.presentation.view.adapter.viewmodel;

import android.view.View;

import com.tribe.app.domain.entity.User;

/**
 * Created by tiago on 29/12/2016.
 */

public class UserLive {

    private User user;
    private View view;

    public UserLive(User user, View view) {
        this.user = user;
        this.view = view;
    }

    public User getUser() {
        return user;
    }

    public View getView() {
        return view;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setView(View view) {
        this.view = view;
    }

    @Override
    public boolean equals(Object obj) {
        return user.equals(obj);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
