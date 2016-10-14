package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.fragment.GroupMemberFragment;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class GroupInfoActivity extends BaseActivity {

    public static final int OPEN_CAMERA_RESULT = 102, OPEN_GALLERY_RESULT = 103;
    private static final String GROUP_AVATAR = "GROUP_AVATAR";

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        return intent;
    }

    private Unbinder unbinder;
    private FragmentManager fragmentManager;
    private GroupsGridFragment groupsGridFragment;
    private GroupMemberFragment groupMemberFragment;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    ScreenUtils screenUtils;

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.layoutFragmentContainer);
        if (currentFragment instanceof GroupMemberFragment) {
            groupsGridFragment.setGroupMemberList(groupMemberFragment.getGroupMemberList());
        }

        super.onBackPressed();
    }

    private void initUi() {
        setContentView(R.layout.activity_group_info);
        unbinder = ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        Bundle bundle = new Bundle();
        bundle.putString("membershipId", extras.getString("membershipId"));
        bundle.putBoolean("isCurrentUserAdmin", extras.getBoolean("isCurrentUserAdmin"));
        bundle.putString("groupId", extras.getString("groupId"));
        bundle.putString("groupName", extras.getString("groupName"));
        bundle.putString("groupPicture", extras.getString("groupPicture"));
        bundle.putString("privateGroupLink", extras.getString("privateGroupLink"));
        bundle.putLong("privateGroupLinkExpiresAt", extras.getLong("privateGroupLinkExpiresAt"));
        groupsGridFragment = GroupsGridFragment.newInstance(bundle);
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layoutFragmentContainer, groupsGridFragment);
        fragmentTransaction.commit();

        subscriptions.add(groupsGridFragment.imageGoToMembersClicked().subscribe(aVoid -> {
            goToGroupMembers();
        }));

    }

    private void goToMain() {
        screenUtils.hideKeyboard(this);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_left, R.anim.fragment_out_from_right);
        fragmentTransaction.replace(R.id.layoutFragmentContainer, groupsGridFragment);
        fragmentTransaction.commit();
    }

    private void goToGroupMembers() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("groupMemberList", groupsGridFragment.getGroupMemberList());
        bundle.putString("groupId", groupsGridFragment.getGroupId());
        groupMemberFragment = GroupMemberFragment.newInstance(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_right, R.anim.fragment_out_from_left);
        fragmentTransaction.add(R.id.layoutFragmentContainer, groupMemberFragment);
        fragmentTransaction.addToBackStack("GroupMember");
        fragmentTransaction.commit();

        subscriptions.add(groupMemberFragment.imageBackClicked().subscribe(aVoid -> {
            groupsGridFragment.setGroupMemberList(groupMemberFragment.getGroupMemberList());
            goToMain();
        }));
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
