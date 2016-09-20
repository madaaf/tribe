package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateTribeListNotSeenStatusJob extends BaseJob {

    @Inject
    TribeCache tribeCache;

    @Inject
    ChatCache chatCache;

    @Inject
    TribeRealmDataMapper tribeRealmDataMapper;

    @Inject
    ChatRealmDataMapper chatRealmDataMapper;

    private List<Message> messageList;

    private List<TribeRealm> tribeRealmList;
    private List<ChatRealm> chatRealmList;

    public UpdateTribeListNotSeenStatusJob(List<Message> messageList) {
        super(new Params(Priority.HIGH).groupBy("update-tribe-list-not-seen").setSingleId("update-tribe-list-not-seen"));
        this.messageList = messageList;
    }

    @Override
    public void onAdded() {
        List<TribeMessage> tribeMessageList = new ArrayList<>();
        List<ChatMessage> chatMessageList = new ArrayList<>();

        for (Message message : messageList) {
            if (message instanceof TribeMessage) tribeMessageList.add((TribeMessage) message);
            if (message instanceof ChatMessage) chatMessageList.add((ChatMessage) message);
        }

        tribeRealmList = tribeRealmDataMapper.transform(tribeMessageList);
        chatRealmList = chatRealmDataMapper.transform(chatMessageList);
    }

    @Override
    public void onRun() throws Throwable {
        Map<String, List<Pair<String, Object>>> chatUpdates = new HashMap<>();
        Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

        for (TribeRealm tribeRealm : tribeRealmList) {
            List<Pair<String, Object>> values = new ArrayList<>();
            values.add(Pair.create(TribeRealm.MESSAGE_RECEIVING_STATUS, MessageReceivingStatus.STATUS_NOT_SEEN));

            if (tribeRealm.isToGroup() && tribeRealm.getGroup() != null
                    && (tribeRealm.getGroup().getUpdatedAt() == null || tribeRealm.getGroup().getUpdatedAt().before(tribeRealm.getRecordedAt()))) {
                values.add(Pair.create(TribeRealm.GROUP_ID_UPDATED_AT, tribeRealm.getRecordedAt()));
            } else if (!tribeRealm.isToGroup() && tribeRealm.getFrom() != null
                    && (tribeRealm.getFrom().getUpdatedAt() == null || tribeRealm.getFrom().getUpdatedAt().before(tribeRealm.getRecordedAt()))) {
                values.add(Pair.create(TribeRealm.FRIEND_ID_UPDATED_AT, tribeRealm.getRecordedAt()));
            }

            tribeUpdates.put(tribeRealm.getLocalId(), values);
        }

        tribeCache.update(tribeUpdates);

        for (ChatRealm chatRealm : chatRealmList) {
            List<Pair<String, Object>> values = new ArrayList<>();
            values.add(Pair.create(ChatRealm.MESSAGE_RECEIVING_STATUS, MessageReceivingStatus.STATUS_NOT_SEEN));

            if (chatRealm.isToGroup() && chatRealm.getGroup() != null
                    && (chatRealm.getGroup().getUpdatedAt() == null || chatRealm.getGroup().getUpdatedAt().before(chatRealm.getRecordedAt()))) {
                values.add(Pair.create(ChatRealm.GROUP_ID_UPDATED_AT, chatRealm.getRecordedAt()));
            } else if (!chatRealm.isToGroup() && chatRealm.getFrom() != null
                    && (chatRealm.getFrom().getUpdatedAt() == null || chatRealm.getFrom().getUpdatedAt().before(chatRealm.getRecordedAt()))) {
                values.add(Pair.create(ChatRealm.FRIEND_ID_UPDATED_AT, chatRealm.getRecordedAt()));
            }

            chatUpdates.put(chatRealm.getLocalId(), values);
        }

        chatCache.update(chatUpdates);
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
