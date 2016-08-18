package com.tribe.app.data.realm;

import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.view.utils.MessageStatus;

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
    private @MessageStatus.Status String messageStatus;
    private Date updated_at;
    private RealmList<TribeRecipientRealm> recipientList;

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

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
    }

    public void setRecipientList(RealmList<TribeRecipientRealm> recipientList) {
        this.recipientList = recipientList;
    }

    public RealmList<TribeRecipientRealm> getRecipientList() {
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

    public ChatRealm cloneMessageRealm(ChatRealm fromRealm) {
        ChatRealm tribeRealm = new ChatRealm();
        tribeRealm.setId(fromRealm.getId());
        tribeRealm.setLocalId(fromRealm.getLocalId());
        tribeRealm.setContent(fromRealm.getContent());
        tribeRealm.setGroup(fromRealm.getGroup());
        tribeRealm.setFriendshipRealm(fromRealm.getFriendshipRealm());
        tribeRealm.setType(fromRealm.getType());
        tribeRealm.setRecordedAt(fromRealm.getRecordedAt());
        tribeRealm.setUpdatedAt(fromRealm.getUpdatedAt());
        tribeRealm.setFrom(fromRealm.getFrom());
        tribeRealm.setMessageStatus(fromRealm.getMessageStatus());
        tribeRealm.setRecipientList(fromRealm.getRecipientList());

        return tribeRealm;
    }
}
