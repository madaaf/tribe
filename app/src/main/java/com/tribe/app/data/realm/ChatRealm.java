package com.tribe.app.data.realm;

import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 29/06/2016.
 */
public class ChatRealm extends RealmObject implements MessageRealmInterface {

    @PrimaryKey
    private String localId;

    private String id;
    private String content;
    private UserRealm from;
    private @ChatMessage.ChatType String type;
    private FriendshipRealm friendshipRealm;
    private GroupRealm group;
    private Date recorded_at;
    private Date created_at;
    private boolean to_group = false;
    private @MessageSendingStatus.Status String messageSendingStatus;
    private @MessageReceivingStatus.Status String messageReceivingStatus;
    private @MessageDownloadingStatus.Status String messageDownloadingStatus;
    private Date updated_at;
    private RealmList<MessageRecipientRealm> recipientList;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    @Override
    public UserRealm getFrom() {
        return from;
    }

    @Override
    public void setFrom(UserRealm from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isToGroup() {
        return to_group;
    }

    public void setToGroup(boolean to_group) {
        this.to_group = to_group;
    }

    public Date getRecordedAt() {
        return recorded_at;
    }

    public void setRecordedAt(Date recorded_at) {
        this.recorded_at = recorded_at;
    }

    public GroupRealm getGroup() {
        return group;
    }

    public void setGroup(GroupRealm group) {
        this.group = group;
    }

    public FriendshipRealm getFriendshipRealm() {
        return friendshipRealm;
    }

    public void setFriendshipRealm(FriendshipRealm friendshipRealm) {
        this.friendshipRealm = friendshipRealm;
    }

    public void setMessageSendingStatus(String messageSendingStatus) {
        this.messageSendingStatus = messageSendingStatus;
    }

    public String getMessageSendingStatus() {
        return messageSendingStatus;
    }

    public String getMessageDownloadingStatus() {
        return messageDownloadingStatus;
    }

    public void setMessageDownloadingStatus(String messageDownloadingStatus) {
        this.messageDownloadingStatus = messageDownloadingStatus;
    }

    public String getMessageReceivingStatus() {
        return messageReceivingStatus;
    }

    public void setMessageReceivingStatus(String messageReceivingStatus) {
        this.messageReceivingStatus = messageReceivingStatus;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
    }

    public void setRecipientList(RealmList<MessageRecipientRealm> recipientList) {
        this.recipientList = recipientList;
    }

    public RealmList<MessageRecipientRealm> getRecipientList() {
        return recipientList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }
}
