package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a ChatMessage in the domain layer.
 */
public class ChatMessage extends Message {

    public static int MINUTE = 65 * 1000; // IN MS (WE GIVE A MARGIN OF 500 MS)

    @StringDef({VIDEO, TEXT, PHOTO})
    public @interface ChatType {}

    public static final String VIDEO = "video";
    public static final String TEXT = "text";
    public static final String PHOTO = "photo";

    public ChatMessage() {

    }

    private @ChatType String type;
    private int widthImage;
    private int heightImage;

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

    public static ChatMessage createMessage(User user, Recipient recipient, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setLocalId(FileUtils.generateIdForMessage());
        chatMessage.setRecordedAt(new Date(System.currentTimeMillis()));
        chatMessage.setFrom(user);
        chatMessage.setTo(recipient);
        chatMessage.setToGroup(recipient instanceof Group);
        chatMessage.setMessageStatus(MessageStatus.STATUS_PENDING);
        chatMessage.setType(ChatMessage.TEXT);
        return chatMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        ChatMessage that = (ChatMessage) o;

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
        stringBuilder.append("\nfrom.id = " + from.getId());
        stringBuilder.append("\nupdatedAt = " + updatedAt);
        stringBuilder.append("\n*******************************");

        return stringBuilder.toString();
    }

    public static List<ChatMessage> computeMessageList(List<ChatMessage> chatMessageList) {
        List<ChatMessage> result = new ArrayList<>();
        ChatMessage previousChatMessage = null;
        int count = 0;

        for (ChatMessage chatMessage : chatMessageList) {
            computeMessage(result, previousChatMessage, chatMessage);
            count++;

            previousChatMessage = result.get(result.size() - 1);

            if (count == chatMessageList.size()) previousChatMessage.isLastOfSection = true;
        }

        return result;
    }

    public static void computeMessage(List<ChatMessage> result, ChatMessage previousChatMessage, ChatMessage chatMessage) {
        boolean shouldAddMessage = true;

        if (previousChatMessage != null) {
            chatMessage.isOtherPerson = !previousChatMessage.getFrom().equals(chatMessage.getFrom());

            if (chatMessage.getRecordedAt().getTime() - previousChatMessage.getRecordedAt().getTime() > MINUTE) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(previousChatMessage.getRecordedAt());
                cal2.setTime(chatMessage.getRecordedAt());
                chatMessage.isFirstOfSection = !(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
                previousChatMessage.isLastOfSection = chatMessage.isFirstOfSection;

                if (chatMessage.isFirstOfSection)
                    chatMessage.isOtherPerson = true; // WE FORCE SHOWING THE USER IF'S THE FIRST MESSAGE OF THE SECTION
            } else {
                chatMessage.shouldDisplayTime = false;
                chatMessage.isFirstOfSection = false;
            }
        } else {
            chatMessage.isFirstOfSection = true;
            chatMessage.isOtherPerson = true;
            chatMessage.shouldDisplayTime = true;
        }

        if (chatMessage.isFirstOfSection) {
            ChatMessage header = new ChatMessage();
            header.setRecordedAt(chatMessage.recordedAt);
            header.setLocalId("header_" + chatMessage.recordedAt.getTime());
            header.isHeader = true;

            // COMPUTING IF THE DATE IS TODAY'S DATE
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(header.getRecordedAt());
            header.isToday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            result.add(header);
        }

        if (chatMessage.getType().equals(TEXT)) {
            chatMessage.isOnlyEmoji = StringUtils.isOnlyEmoji(chatMessage.content);

            if (!chatMessage.isOnlyEmoji) {
                String[] urls = StringUtils.extractLinks(chatMessage.content);

                if (urls.length > 0) {
                    shouldAddMessage = false;

                    String text = chatMessage.content;

                    for (String url : urls) {
                        String str = text.substring(0, text.indexOf(url));
                        text = text.substring(text.indexOf(url) + url.length());

                        if (!StringUtils.isEmpty(str)) {
                            ChatMessage chatMessageCopy = cloneMessage(chatMessage, true);
                            chatMessageCopy.content = str;
                            result.add(chatMessageCopy);
                        }

                        ChatMessage chatMessageCopy = cloneMessage(chatMessage, StringUtils.isEmpty(str));
                        chatMessageCopy.content = url;
                        chatMessageCopy.isLink = true;
                        result.add(chatMessageCopy);
                    }

                    if (!StringUtils.isEmpty(text)) {
                        ChatMessage chatMessageCopy = cloneMessage(chatMessage, false);
                        chatMessageCopy.content = text;
                        result.add(chatMessageCopy);
                    }
                }
            }
        }

        if (shouldAddMessage) {
            result.add(chatMessage);
        }
    }

    public static ChatMessage cloneMessage(ChatMessage chatMessageFrom, boolean shouldKeepState) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatMessageFrom.content);
        chatMessage.setType(chatMessageFrom.type);
        chatMessage.setLocalId(chatMessageFrom.localId);
        chatMessage.setId(chatMessageFrom.id + chatMessageFrom.content);
        chatMessage.setRecordedAt(chatMessageFrom.recordedAt);
        chatMessage.setUpdatedAt(chatMessageFrom.updatedAt);
        chatMessage.setFrom(chatMessageFrom.from);
        chatMessage.setTo(chatMessageFrom.to);
        chatMessage.setToGroup(chatMessageFrom.toGroup);
        chatMessage.setMessageStatus(chatMessageFrom.messageStatus);

        if (shouldKeepState) {
            chatMessage.isOtherPerson = chatMessageFrom.isOtherPerson;
            chatMessage.shouldDisplayTime = chatMessageFrom.shouldDisplayTime;
            chatMessage.isFirstOfSection = chatMessageFrom.isFirstOfSection;
            chatMessage.isLastOfSection = chatMessageFrom.isLastOfSection;
            chatMessage.isOnlyEmoji = chatMessageFrom.isOnlyEmoji;
            chatMessage.isLink = chatMessageFrom.isLink;
        } else {
            chatMessage.isOtherPerson = false;
            chatMessage.shouldDisplayTime = false;
            chatMessage.isFirstOfSection = false;
            chatMessage.isLastOfSection = false;
            chatMessage.isOnlyEmoji = false;
            chatMessage.isLink = false;
        }

        return chatMessage;
    }

    public static int nullSafeComparator(final ChatMessage one, final ChatMessage two) {
        if (one == null ^ two == null) {
            return (one == null) ? 1 : -1;
        }

        if (one == null && two == null) {
            return 0;
        }

        return one.getRecordedAt().compareTo(two.getRecordedAt());
    }

    public static ChatMessage getMostRecentMessage(final ChatMessage... chatMessages) {
        List<ChatMessage> chatMessageList = Arrays.asList(chatMessages);

        Collections.sort(chatMessageList, (one, two) -> {
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

        return chatMessageList.get(chatMessageList.size() - 1);
    }
}
