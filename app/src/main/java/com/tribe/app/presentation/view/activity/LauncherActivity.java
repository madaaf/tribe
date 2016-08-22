package com.tribe.app.presentation.view.activity;

import android.os.Bundle;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Location;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.entity.Weather;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

    @Inject
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);

        this.getApplicationComponent().inject(this);

        if (true) {  // accessToken == null || accessToken.getAccessToken() == null
            //navigator.navigateToLogin(this);
            // TODO REMOVE BOGUS DATA
            Friendship friendship = new Friendship("id");
            friendship.setCategory("friend");
            friendship.setTag("friend");
            friendship.setBlocked(false);

            User friend = new User("Hey");
            friend.setUsername("@nicozer");
            friend.setProfilePicture("http://i.imgur.com/Nyi6mTT.png");
            friend.setDisplayName("Nico Buscemi");
            friendship.setFriend(friend);

            TribeMessage tribe = new TribeMessage();
            //tribe.setId("5ece491a9a1c4c44a17f1a0357f7a287");
            tribe.setId("5655016773779456");
            tribe.setMessageStatus(MessageStatus.STATUS_READY);
            tribe.setLocalId(tribe.getId());
            tribe.setTo(null);
            tribe.setFrom(friend);
            tribe.setRecordedAt(new Date());
            tribe.setCreatedAt(new Date());

            Location location = new Location(0.0D, 0.0D);
            location.setHasLocation(true);
            location.setCity("Kuala Lumpur");
            tribe.setLocation(location);

            Weather weather = new Weather();
            weather.setTempF(80);
            weather.setTempC(31);
            weather.setIcon("clear");
            tribe.setWeather(weather);

            List<TribeMessage> messages = new ArrayList<>();
            messages.add(tribe);
            friendship.setReceivedTribes(messages);
            navigator.navigateToTribe(this, 1, friendship);
            //navigator.navigateToLogin(this);
        } else {
            navigator.navigateToHome(this);
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}