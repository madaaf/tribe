package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.DiceView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by remy on 28/07/2017.
 */

public class TestActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, TestActivity.class);
    }

    @BindView(R.id.diceLayoutRoomView) DiceView diceLayoutRoomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        diceLayoutRoomView.setNextAnimation();
    }
}
