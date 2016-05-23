package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.EditText;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Text;
import com.tribe.app.presentation.internal.di.HasComponent;
import com.tribe.app.presentation.internal.di.components.DaggerFriendshipComponent;
import com.tribe.app.presentation.internal.di.components.DaggerTextComponent;
import com.tribe.app.presentation.internal.di.components.FriendshipComponent;
import com.tribe.app.presentation.internal.di.components.TextComponent;
import com.tribe.app.presentation.internal.di.modules.TextModule;
import com.tribe.app.presentation.mvp.presenter.TextPresenter;
import com.tribe.app.presentation.view.fragment.DiscoverGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.fragment.MediaGridFragment;
import com.tribe.app.presentation.view.widget.EditTextFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextActivity extends BaseActivity {

    public static final String CONVERSATION_ID = "CONVERSATION_ID";

    public static Intent getCallingIntent(Context context, int id) {
        Intent intent = new Intent(context, TextActivity.class);
        intent.putExtra(CONVERSATION_ID, id);
        return intent;
    }

    //@BindView(R.id.editTextMessage)
    //EditTextFont editTextFont;

    @Inject
    TextPresenter textPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initializeDependencyInjector();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePresenter();
    }

    private void initUi() {
        setContentView(R.layout.activity_text);
        ButterKnife.bind(this);
    }

    private void initializeDependencyInjector() {
        DaggerTextComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initializePresenter() {
        textPresenter.onStart();
    }
}