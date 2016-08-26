package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import com.tribe.app.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * SettingActivity.java
 * Created by horatiothomas on 8/26/16.
 */
public class SettingActivity extends AppCompatActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);
    }



}
