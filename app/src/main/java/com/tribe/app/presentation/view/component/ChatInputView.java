package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.EditTextFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * View providing a text input field, a photo picker and a Send button.
 */
public class ChatInputView extends FrameLayout {

    @BindView(R.id.editTextMessage)
    EditTextFont editTextMessage;

    @BindView(R.id.btnPhotoPicker)
    ViewGroup btnPhotoPicker;

    @BindView(R.id.imgGallery)
    ImageView imgGallery;

    private long lastOnTypingNotification;

    // OBSERVABLES
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

    public void setImageGallery(Bitmap bitmap) {
        imgGallery.setImageBitmap(bitmap);
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
                .filter(s -> {
                    long elapsedTime = SystemClock.elapsedRealtime() - lastOnTypingNotification;
                    return (s.length() > 0 && elapsedTime > 2000);
                }).doOnNext(s ->
                    lastOnTypingNotification = SystemClock.elapsedRealtime())
                .subscribe(textChangeEventSubject);
    }

    public Observable<String> textChanges() {
        return textChangeEventSubject;
    }

    public Observable<String> sendClick() {
        return sendClickEventSubject;
    }
}
