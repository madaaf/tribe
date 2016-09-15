package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 9/14/16.
 */
public class GroupPresenter implements Presenter {

    private final GetGroupMembers getGroupMembers;

    private GroupView groupView;

    @Inject
    GroupPresenter(GetGroupMembers getGroupMembers) {
        this.getGroupMembers = getGroupMembers;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void attachView(View v) {
        groupView = (GroupView) v;
    }

    @Override
    public void onCreate() {

    }

    public void getGroupMembers(String groupId) {
        getGroupMembers.prepare(groupId);
        getGroupMembers.execute(new GetGroupMemberSubscriber());
    }

    private final class GetGroupMemberSubscriber extends DefaultSubscriber<Group> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Group group) {
        }
    }

}
