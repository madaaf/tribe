package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.ActionEditText;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * View providing a text input field, a photo picker and a Send button.
 */
public class ChatInputView extends FrameLayout {

    private static final int DURATION = 300;
    private static final float OVERSHOOT = 0.75f;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.txtPendingMessages)
    TextViewFont txtPendingMessages;

    @BindView(R.id.editTextMessage)
    ActionEditText editTextMessage;

    @BindView(R.id.btnPhotoPicker)
    ViewGroup btnPhotoPicker;

    @BindView(R.id.imgGallery)
    ImageView imgGallery;

    @BindView(R.id.viewDivider)
    View viewDivider;

    private long lastOnTypingNotification;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private PublishSubject<String> textChangeEventSubject = PublishSubject.create();
    private PublishSubject<String> sendClickEventSubject = PublishSubject.create();
    private PublishSubject<Void> chooseImageFromGallery = PublishSubject.create();
    private PublishSubject<Void> onPendingClick = PublishSubject.create();

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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (compositeSubscription != null && compositeSubscription.hasSubscriptions()) compositeSubscription.unsubscribe();
        super.onDetachedFromWindow();
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

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), 0);
    }

    public void setImageGallery(Bitmap bitmap) {
        imgGallery.setImageBitmap(bitmap);
    }

    public void showPendingMessages(int nb) {
        txtPendingMessages.setText("" + nb);

        if (txtPendingMessages.getTranslationX() > 0) {
            txtPendingMessages.clearAnimation();
            txtPendingMessages.animate().setDuration(DURATION)
                    .translationX(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            txtPendingMessages.setVisibility(View.VISIBLE);
                            txtPendingMessages.animate().setListener(null).start();
                        }
                    })
                    .setInterpolator(new OvershootInterpolator(OVERSHOOT))
                    .start();
        }
    }

    public void hidePendingMessages() {
        if (txtPendingMessages.getTranslationX() == 0) {
            txtPendingMessages.clearAnimation();
            txtPendingMessages.animate().setDuration(DURATION).translationX(screenUtils.getWidthPx() >> 1)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            txtPendingMessages.setVisibility(View.GONE);
                            txtPendingMessages.animate().setListener(null).start();
                        }
                    })
                    .start();
        }
    }

    public void hidePhotoPicker() {
        btnPhotoPicker.setVisibility(View.GONE);
        viewDivider.setVisibility(View.GONE);
    }

    public void onDestroy() {
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_chat_input, this);
        unbinder = ButterKnife.bind(this);

        screenUtils = ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().screenUtils();

        hidePendingMessages();

        compositeSubscription.add(RxTextView.textChanges(editTextMessage).map(CharSequence::toString)
                .filter(s -> {
                    long elapsedTime = SystemClock.elapsedRealtime() - lastOnTypingNotification;
                    return (s.length() > 0 && elapsedTime > 2000);
                }).doOnNext(s ->
                    lastOnTypingNotification = SystemClock.elapsedRealtime())
                .subscribe(textChangeEventSubject));

        compositeSubscription.add(RxTextView.editorActions(editTextMessage).filter(action -> action.equals(EditorInfo.IME_ACTION_SEND))
                .map(action -> editTextMessage.getText().toString())
                .filter(s -> !StringUtils.isEmpty(s.trim()))
                .doOnNext(s -> clearText())
                .subscribe(sendClickEventSubject));

        compositeSubscription.add(RxView.clicks(txtPendingMessages).subscribe(onPendingClick));
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

    public Observable<Void> onPendingClick() {
        return onPendingClick;
    }
}
