package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 08/10/2016.
 *
 * Class that represents a Message in the domain layer.
 */
public abstract class Message implements Serializable {

    public Message() {

    }

    protected String id;
    protected String localId;
    protected String content;
    protected User from;
    protected Recipient to;
    protected boolean toGroup;
    protected Date recordedAt;
    protected Date createdAt;
    protected Date updatedAt;
    protected @MessageSendingStatus.Status String messageSendingStatus;
    protected @MessageReceivingStatus.Status String messageReceivingStatus;
    protected @MessageDownloadingStatus.Status String messageDownloadingStatus;
    protected List<MessageRecipient> recipientList;

    public String getId() {
        return id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public Recipient getTo() {
        return to;
    }

    public void setTo(Recipient to) {
        this.to = to;
    }

    public boolean isToGroup() {
        return toGroup;
    }

    public void setToGroup(boolean toGroup) {
        this.toGroup = toGroup;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @MessageSendingStatus.Status String getMessageSendingStatus() {
        return messageSendingStatus;
    }

    public void setMessageSendingStatus(String messageSendingStatus) {
        this.messageSendingStatus = messageSendingStatus;
    }

    public @MessageDownloadingStatus.Status String getMessageDownloadingStatus() {
        return messageDownloadingStatus;
    }

    public void setMessageDownloadingStatus(String messageDownloadingStatus) {
        this.messageDownloadingStatus = messageDownloadingStatus;
    }

    public @MessageReceivingStatus.Status String getMessageReceivingStatus() {
        return messageReceivingStatus;
    }

    public void setMessageReceivingStatus(String messageReceivingStatus) {
        this.messageReceivingStatus = messageReceivingStatus;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRecipientList(List<MessageRecipient> recipientList) {
        this.recipientList = recipientList;
    }

    public List<MessageRecipient> getRecipientList() {
        return recipientList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Message that = (Message) o;

        return localId != null ? localId.equals(that.localId) : that.localId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (localId != null ? localId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** TribeMessage Details *****\n");
        stringBuilder.append("\nid = " + id);
        stringBuilder.append("\ncontent = " + content);
        stringBuilder.append("\nfrom.id = " + from.getId());
        stringBuilder.append("\nupdatedAt = " + updatedAt);
        stringBuilder.append("\n*******************************");

        return stringBuilder.toString();
    }

    public static int nullSafeComparator(final Message one, final Message two) {
        if (one == null ^ two == null) {
            return (one == null) ? 1 : -1;
        }

        if (one == null && two == null) {
            return 0;
        }

        return one.getRecordedAt().compareTo(two.getRecordedAt());
    }

    public static Message getMostRecentMessage(final Message... messages) {
        List<Message> messageList = Arrays.asList(messages);

        Collections.sort(messageList, (one, two) -> {
            if (one == null ^ two == null) {
                return (one == null) ? -1 : 1;
            }

            if (one == null && two == null) return 0;

            if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
                return (one.getUpdatedAt() == null) ? -1 : 1;
            }

            if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
                return one.getRecordedAt().before(two.getRecordedAt()) ? -1 : 1;
            }

            return one.getUpdatedAt().before(two.getUpdatedAt()) ? -1 : 1;
        });

        return messageList.get(messageList.size() - 1);
    }
}
