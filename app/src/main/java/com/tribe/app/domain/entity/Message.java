package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a Tribe in the domain layer.
 */
public class Message implements Serializable {

    public static int MINUTE = 60 * 1000; // IN MS

    public Message() {

    }

    private String id;
    private String localId;
    private String text;
    private User from;
    private String type;
    private Recipient to;
    private boolean toGroup;
    private Date recordedAt;
    private Date updatedAt;
    private String url;
    private @MessageStatus.Status String messageStatus;

    private boolean isHeader;
    private boolean isToday;
    private boolean shouldDisplayTime = true;
    private boolean isLastOfSection = false;
    private boolean isFirstOfSection = true;
    private boolean isOtherPerson = false;
    private boolean isOnlyEmoji = false;
    private boolean isLink = false;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public @MessageStatus.Status String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isShouldDisplayTime() {
        return shouldDisplayTime;
    }

    public void setShouldDisplayTime(boolean shouldDisplayTime) {
        this.shouldDisplayTime = shouldDisplayTime;
    }

    public boolean isLastOfSection() {
        return isLastOfSection;
    }

    public void setLastOfSection(boolean lastOfSection) {
        isLastOfSection = lastOfSection;
    }

    public boolean isFirstOfSection() {
        return isFirstOfSection;
    }

    public void setFirstOfSection(boolean firstOfSection) {
        isFirstOfSection = firstOfSection;
    }

    public boolean isOtherPerson() {
        return isOtherPerson;
    }

    public void setOtherPerson(boolean otherPerson) {
        isOtherPerson = otherPerson;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isOnlyEmoji() {
        return isOnlyEmoji;
    }

    public void setOnlyEmoji(boolean onlyEmoji) {
        isOnlyEmoji = onlyEmoji;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    public static Message createMessage(User user, Recipient recipient, String text) {
        Message message = new Message();
        message.setText(text);
        message.setLocalId(FileUtils.generateIdForTribe());
        message.setRecordedAt(new Date(System.currentTimeMillis()));
        message.setFrom(user);
        message.setTo(recipient);
        message.setToGroup(recipient instanceof Group);
        message.setMessageStatus(MessageStatus.STATUS_PENDING);
        return message;
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

        stringBuilder.append("***** Tribe Details *****\n");
        stringBuilder.append("\nid = " + id);
        stringBuilder.append("\nfrom.id = " + from.getId());
        stringBuilder.append("\nupdatedAt = " + updatedAt);
        stringBuilder.append("\n*******************************");

        return stringBuilder.toString();
    }

    public static List<Message> computeMessageList(List<Message> messageList) {
        List<Message> result = new ArrayList<>();
        Message previousMessage = null;
        int count = 0;

        for (Message message : messageList) {
            if (previousMessage != null) {
                message.isOtherPerson = !previousMessage.getFrom().equals(message.getFrom());

                if (message.getRecordedAt().getTime() - previousMessage.getRecordedAt().getTime() > MINUTE) {
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(previousMessage.getRecordedAt());
                    cal2.setTime(message.getRecordedAt());
                    message.isFirstOfSection = !(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
                    previousMessage.isLastOfSection = message.isFirstOfSection;

                    if (message.isFirstOfSection) message.isOtherPerson = true; // WE FORCE SHOWING THE USER IF'S THE FIRST MESSAGE OF THE SECTION
                } else {
                    message.shouldDisplayTime = false;
                    message.isFirstOfSection = false;
                }
            } else {
                message.isFirstOfSection = true;
                message.isOtherPerson = true;
                message.shouldDisplayTime = true; //message.isLastOfSection = true;
            }

            if (message.isFirstOfSection) {
                Message header = new Message();
                header.setRecordedAt(message.getRecordedAt());
                header.isHeader = true;

                // COMPUTING IF THE DATE IS TODAY'S DATE
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(header.getRecordedAt());
                header.isToday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
                result.add(header);
            }

            count++;

            if (count == messageList.size()) message.isLastOfSection = true;
            message.isOnlyEmoji = StringUtils.isOnlyEmoji(message.getText());
            if (!message.isOnlyEmoji) message.isLink = StringUtils.isUrl(message.getText());

            previousMessage = message;
            result.add(message);
        }

        return result;
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
