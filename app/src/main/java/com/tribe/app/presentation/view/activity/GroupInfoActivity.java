package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.fragment.SettingFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class GroupInfoActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        return intent;
    }

    Unbinder unbinder;
    FragmentManager fragmentManager;
    GroupsGridFragment groupsGridFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        initUi();

    }


    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_group_info);
        unbinder = ButterKnife.bind(this);

        Intent intent = getIntent();
        String groupId = intent.getStringExtra("groupId");

        Bundle bundle = new Bundle();
        bundle.putString("groupId", groupId);
        groupsGridFragment = GroupsGridFragment.newInstance(bundle);
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layoutFragmentContainer, groupsGridFragment);
        fragmentTransaction.commit();


    }



    /**
     * Dagger Setup
     */

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }
}
