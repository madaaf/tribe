package com.tribe.app.data.realm;

import android.support.annotation.StringDef;

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

    @StringDef({MESSAGE_SENDING_STATUS, MESSAGE_RECEIVING_STATUS, MESSAGE_DOWNLOADING_STATUS,
            PROGRESS, TOTAL_SIZE, FRIEND_ID_UPDATED_AT, GROUP_ID_UPDATED_AT})
    public @interface ChatRealmAttributes {}

    public static final String MESSAGE_SENDING_STATUS = "messageSendingStatus";
    public static final String MESSAGE_RECEIVING_STATUS = "messageReceivingStatus";
    public static final String MESSAGE_DOWNLOADING_STATUS = "messageDownloadingStatus";
    public static final String PROGRESS = "progress";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String FRIEND_ID_UPDATED_AT = "friendshipRealm.updated_at";
    public static final String GROUP_ID_UPDATED_AT = "membershipRealm.updated_at";

    @PrimaryKey
    private String localId;

    private String id;
    private String content;
    private UserRealm from;
    private @ChatMessage.ChatType String type;
    private FriendshipRealm friendshipRealm;
    private MembershipRealm membershipRealm;
    private Date recorded_at;
    private Date created_at;
    private boolean to_group = false;
    private @MessageSendingStatus.Status String messageSendingStatus;
    private @MessageReceivingStatus.Status String messageReceivingStatus;
    private @MessageDownloadingStatus.Status String messageDownloadingStatus;
    private Date updated_at;
    private RealmList<MessageRecipientRealm> recipientList;
    private long progress = 0L;
    private long totalSize = 0L;

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
    public RecipientRealmInterface getRecipient() {
        return to_group ? membershipRealm : friendshipRealm;
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

    @Override
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

    public MembershipRealm getMembershipRealm() {
        return membershipRealm;
    }

    public void setMembershipRealm(MembershipRealm membershipRealm) {
        this.membershipRealm = membershipRealm;
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

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
