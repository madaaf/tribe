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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.widget.ActionEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * View providing a text input field, a photo picker and a Send button.
 */
public class ChatInputView extends FrameLayout {

    @BindView(R.id.editTextMessage)
    ActionEditText editTextMessage;

    @BindView(R.id.btnPhotoPicker)
    ViewGroup btnPhotoPicker;

    @BindView(R.id.imgGallery)
    ImageView imgGallery;

    private long lastOnTypingNotification;

    // OBSERVABLES
    private Unbinder unbinder;
    private PublishSubject<String> textChangeEventSubject = PublishSubject.create();
    private PublishSubject<String> sendClickEventSubject = PublishSubject.create();
    private PublishSubject<Void> chooseImageFromGallery = PublishSubject.create();

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

    /**
     * Requests the focus for the input text
     */
    public void showKeyboard() {
        editTextMessage.requestFocus();
        editTextMessage.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editTextMessage, 0);
        }, 1000);
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

        RxTextView.editorActions(editTextMessage).filter(action -> action.equals(EditorInfo.IME_ACTION_SEND))
                .map(action -> editTextMessage.getText().toString())
                .filter(s -> !StringUtils.isEmpty(s.trim()))
                .doOnNext(s -> clearText())
                .subscribe(sendClickEventSubject);
    }

    @OnClick(R.id.btnPhotoPicker)
    public void clickPhotoPicker() {
        chooseImageFromGallery.onNext(null);
    }

    public Observable<String> textChanges() {
        return textChangeEventSubject;
    }

    public Observable<String> sendClick() {
        return sendClickEventSubject;
    }

    public Observable<Void> chooseImageFromGallery() {
        return chooseImageFromGallery;
    }
}
