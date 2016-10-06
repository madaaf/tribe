package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class DeleteMessageJob extends BaseJob {

    @Inject
    ChatCache chatCache;

    @Inject
    ChatRealmDataMapper chatRealmDataMapper;

    private ChatMessage [] chatMessageArray;

    public DeleteMessageJob(ChatMessage [] chatMessageArray) {
        super(new Params(Priority.HIGH).groupBy("delete-message-status").setSingleId("delete-message-status"));
        this.chatMessageArray = chatMessageArray;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        for (ChatMessage message : chatMessageArray) {
            if (message.getType().equals(ChatMessage.PHOTO))
                FileUtils.delete(getApplicationContext(), message.getLocalId(), FileUtils.PHOTO);
            else
                FileUtils.delete(getApplicationContext(), message.getLocalId(), FileUtils.VIDEO);

            chatCache.delete(chatRealmDataMapper.transform(message));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }
}
