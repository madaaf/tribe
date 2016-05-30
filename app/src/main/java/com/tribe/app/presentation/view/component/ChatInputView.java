package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.EditTextFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * View providing a text input field, a photo picker and a Send button.
 */
public class ChatInputView extends FrameLayout {

    @BindView(R.id.editTextMessage)
    EditTextFont editTextMessage;

    @BindView(R.id.btnSendEnabled)
    FloatingActionButton btnSendEnabled;

    @BindView(R.id.btnSendDisabled)
    FloatingActionButton btnSendDisabled;

    @BindView(R.id.btnPhotoPicker)
    ImageView btnPhotoPicker;

    private long lastOnTypingNotification;

    private Unbinder unbinder;
    private PublishSubject<String> textChangeEventSubject = PublishSubject.create();
    private PublishSubject<String> sendClickEventSubject = PublishSubject.create();

    public ChatInputView(Context context) {
        super(context);
    }

    public ChatInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChatInputView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Returns the text currently entered into the text field.
     *
     * @return the text in the text field
     */
    @NonNull
    public String getText() {
        return editTextMessage.getText().toString();
    }

    /**
     * Clears the text in the text field.
     */
    public void clearText() {
        editTextMessage.setText("", TextView.BufferType.EDITABLE);
    }

    public void onDestroy() {
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_chat_input, this);
        unbinder = ButterKnife.bind(this);

        RxTextView.textChanges(editTextMessage).map(CharSequence::toString)
                .doOnNext(s -> updateSendButtonState(s.length() > 0)).filter(s -> {
                    long elapsedTime = SystemClock.elapsedRealtime() - lastOnTypingNotification;
                    return (s.length() > 0 && elapsedTime > 2000);
                }).doOnNext(s ->
                    lastOnTypingNotification = SystemClock.elapsedRealtime())
                .subscribe(textChangeEventSubject);

        btnSendEnabled.setEnabled(true);
        btnSendEnabled.setVisibility(View.GONE);
        RxView.clicks(btnSendEnabled)
                .map(aVoid -> getText())
                .doOnNext(s -> clearText())
                .subscribe(sendClickEventSubject);

        btnSendDisabled.setEnabled(false);
        btnSendDisabled.setVisibility(View.VISIBLE);
    }

    private void updateSendButtonState(boolean enable) {
        if ((enable && btnSendEnabled.getVisibility() == VISIBLE) || (!enable && btnSendDisabled.getVisibility() == VISIBLE)) {
            return;
        }

        if (enable) {
            btnSendDisabled.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    btnSendEnabled.show();
                }
            });
        }
        else {
            btnSendEnabled.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    btnSendDisabled.show();
                }
            });
        }
    }

    public Observable<String> textChanges() {
        return textChangeEventSubject;
    }

    public Observable<String> sendClick() {
        return sendClickEventSubject;
    }
}
