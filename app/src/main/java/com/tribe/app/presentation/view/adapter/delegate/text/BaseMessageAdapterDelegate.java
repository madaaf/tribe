package com.tribe.app.presentation.view.adapter.delegate.text;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public abstract class BaseMessageAdapterDelegate extends RxAdapterDelegate<List<ChatMessage>> {

    private final static float ALPHA_PENDING = 0.2f;
    private final static int DURATION = 200;

    // VARIABLES
    protected Context context;
    protected User currentUser;
    protected SimpleDateFormat simpleDateFormat;
    protected LayoutInflater layoutInflater;

    // RESOURCES
    protected int marginVerticalSmall;
    protected int marginVerticalXSmall;

    public BaseMessageAdapterDelegate(LayoutInflater inflater, Context context) {
        this.context = context;
        this.currentUser = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().currentUser();
        this.simpleDateFormat = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().simpleDateHoursMinutes();
        this.layoutInflater = inflater;
        this.marginVerticalSmall = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_small);
        this.marginVerticalXSmall = context.getResources().getDimensionPixelSize(R.dimen.vertical_margin_xsmall);
    }

    @Override
    public boolean isForViewType(@NonNull List<ChatMessage> items, int position) {
        ChatMessage chatMessage = items.get(position);
        return !chatMessage.isHeader();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return getViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull List<ChatMessage> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        BaseTextViewHolder vh = (BaseTextViewHolder) holder;
        ChatMessage chatMessage = items.get(position);

        holder.itemView.setTag(R.id.chat_id, chatMessage.getId());

        if (!chatMessage.isShouldDisplayTime() && !chatMessage.isOtherPerson()) {
            vh.layoutInfos.setVisibility(View.GONE);
        } else {
            vh.layoutInfos.setVisibility(View.VISIBLE);

            if (chatMessage.isOtherPerson()) {
                vh.txtName.setVisibility(View.VISIBLE);
                vh.txtName.setText(chatMessage.getFrom().getDisplayName());
                if (chatMessage.getFrom().equals(currentUser)) vh.txtName.setTextColor(context.getResources().getColor(R.color.blue_text));
                else vh.txtName.setTextColor(Color.BLACK);
            } else {
                vh.txtName.setVisibility(View.GONE);
            }

            vh.txtTime.setText(simpleDateFormat.format(chatMessage.getCreatedAt()));
        }

        if (chatMessage.getMessageSendingStatus() != null) {
            if (chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING)) {
                vh.getLayoutContent().setAlpha(ALPHA_PENDING);
                vh.viewStatus.setVisibility(View.GONE);
                vh.circularProgressView.setVisibility(View.VISIBLE);
            } else if (chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_SENT)) {
                vh.viewStatus.setBackgroundResource(R.drawable.shape_status_sent);
            } else if (chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_ERROR)) {
                vh.viewStatus.setBackgroundResource(R.drawable.shape_status_error);
            } else if (chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_OPENED_PARTLY)) {
                vh.viewStatus.setBackgroundResource(R.drawable.shape_status_read_partially);
            } else if (chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_OPENED)) {
                vh.viewStatus.setBackgroundResource(R.drawable.shape_status_read_everyone);
            }
        }

        if (vh.getLayoutContent().getAlpha() == ALPHA_PENDING
                && (chatMessage.getMessageSendingStatus() == null || !chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING))) {
            vh.getLayoutContent().animate().setDuration(DURATION).alpha(1f).start();
            vh.circularProgressView.setVisibility(View.GONE);
        }

        if (chatMessage.getMessageSendingStatus() == null) {
            vh.viewStatus.setVisibility(View.GONE);
        } else if (chatMessage.getMessageSendingStatus() != null && !chatMessage.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING)) {
            vh.viewStatus.setVisibility(View.VISIBLE);
        }

        if (chatMessage.isFirstOfSection() || chatMessage.isLastOfSection() || chatMessage.isOtherPerson()) {
            vh.itemView.setPadding(vh.itemView.getPaddingLeft(),
                    (chatMessage.isFirstOfSection() || chatMessage.isOtherPerson()) ? marginVerticalSmall : marginVerticalXSmall,
                    vh.itemView.getPaddingRight(),
                    chatMessage.isLastOfSection() ? marginVerticalSmall : 0);
        } else {
            vh.itemView.setPadding(vh.itemView.getPaddingLeft(), marginVerticalXSmall, vh.itemView.getPaddingRight(), 0);
        }
    }

    protected void setupClipboard(View view, String content) {
        view.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", content);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, R.string.chat_copied_to_clipboard, Toast.LENGTH_LONG).show();
            return false;
        });
    }

    protected abstract BaseTextViewHolder getViewHolder(ViewGroup parent);

    static abstract class BaseTextViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.txtTime) public TextViewFont txtTime;
        @BindView(R.id.layoutInfos) public ViewGroup layoutInfos;
        @BindView(R.id.circularProgressView) public CircularProgressView circularProgressView;
        @BindView(R.id.viewStatus) View viewStatus;

        public BaseTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected abstract ViewGroup getLayoutContent();
    }
}
