package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class DownloadChatVideoJob extends DownloadVideoJob {

    private static final String TAG = "DownloadChatVideoJob";

    @Inject
    FileApi fileApi;

    @Inject
    ChatCache chatCache;

    @Inject
    ChatRealmDataMapper chatRealmDataMapper;

    // VARIABLES
    private ChatMessage chatMessage;

    public DownloadChatVideoJob(ChatMessage chatMessage) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy(
                (chatMessage.isToGroup() ? chatMessage.getTo().getId() : chatMessage.getFrom().getId())
        ).setSingleId(chatMessage.getId()).addTags(chatMessage.getId()));

        this.chatMessage = chatMessage;
    }

    @Override
    public void onAdded() {
        setStatus(MessageDownloadingStatus.STATUS_DOWNLOADING);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        File file = FileUtils.getFileEnd(chatMessage.getId());

        setStatus(file.exists() && file.length() > 0 ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    @Override
    protected String getFileId() {
        return chatMessage.getId();
    }

    @Override
    protected String getUrl() {
        return chatMessage.getContent();
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void saveResult(boolean writtenToDisk) {
        setStatus(writtenToDisk ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
    }

    @Override
    protected void setStatus(@MessageDownloadingStatus.Status String status) {
        ChatRealm chatRealm = chatRealmDataMapper.transform(chatMessage);
        chatRealm.setMessageDownloadingStatus(status);
        chatCache.update(chatRealm);
    }
}
