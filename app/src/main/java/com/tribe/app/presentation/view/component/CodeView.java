package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * CodeView.java
 * Created by tiago on 10/06/2016.
 * Last Modified by Horatio.
 * Component used in View Pager in IntroViewFragment.java for a user to input their verification code.
 */
public class CodeView extends FrameLayout {

    @BindView(R.id.editTextCode)
    EditTextFont editTextCode;

    @BindView(R.id.circularProgressViewCode)
    CircularProgressView circularProgressViewCode;

    @BindView(R.id.imgBackIcon)
    ImageView imgBackIcon;

    @BindView(R.id.pinCircle1)
    ImageView pinCircle1;

    @BindView(R.id.pinCircle2)
    ImageView pinCircle2;

    @BindView(R.id.pinCircle3)
    ImageView pinCircle3;

    @BindView(R.id.pinCircle4)
    ImageView pinCircle4;

    @BindView(R.id.txtCode1)
    TextViewFont txtCode1;

    @BindView(R.id.txtCode2)
    TextViewFont txtCode2;

    @BindView(R.id.txtCode3)
    TextViewFont txtCode3;

    @BindView(R.id.txtCode4)
    TextViewFont txtCode4;

    @BindView(R.id.imgConnectedIcon)
    ImageView imgConnectedIcon;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Boolean> codeValid = PublishSubject.create();
    private PublishSubject<Void> backClicked = PublishSubject.create();

    public CodeView(Context context) {
        super(context);
    }

    public CodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Lifecycle methods
     */

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_code, this);
        unbinder = ButterKnife.bind(this);

        imgConnectedIcon.setScaleX(0);
        imgConnectedIcon.setScaleY(0);

        subscriptions.add(RxTextView.textChanges(editTextCode).map(CharSequence::toString)
                .map(s -> {
                    switch (s.length()) {
                        case 0:
                            pinCircle1.setVisibility(VISIBLE);
                            pinCircle2.setVisibility(VISIBLE);
                            pinCircle3.setVisibility(VISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            txtCode1.setText("");
                            break;
                        case 1:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(VISIBLE);
                            pinCircle3.setVisibility(VISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            txtCode1.setText(s);
                            txtCode2.setText("");
                            break;
                        case 2:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(INVISIBLE);
                            pinCircle3.setVisibility(VISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            txtCode2.setText(s.substring(1));
                            txtCode3.setText("");
                            break;
                        case 3:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(INVISIBLE);
                            pinCircle3.setVisibility(INVISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            txtCode3.setText(s.substring(2));
                            txtCode4.setText("");
                            break;
                        case 4:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(INVISIBLE);
                            pinCircle3.setVisibility(INVISIBLE);
                            pinCircle4.setVisibility(INVISIBLE);
                            txtCode4.setText(s.substring(3));
                            break;
                    }

                    return s.length() == 4;
                })
                .subscribe(codeValid));

        subscriptions.add(RxView.clicks(imgBackIcon).subscribe(aVoid -> {
            resetPinCodeView();
            backClicked.onNext(null);
        }));
    }

    @OnClick(R.id.layoutPin)
    void openKeyboard() {
        editTextCode.requestFocus();
        editTextCode.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editTextCode, 0);
        }, 200);
    }

    /**
     * Obeservable
     */

    public Observable<Boolean> codeValid() {
        return codeValid;
    }

    public Observable<Void> backClicked() {
        return backClicked;
    }

    /**
     * Public view methods
     */

    public String getCode() {
        return editTextCode.getText().toString();
    }

    public void progressViewVisible(boolean visible) {
        if (visible) {
            circularProgressViewCode.setVisibility(VISIBLE);
        } else {
            circularProgressViewCode.setVisibility(INVISIBLE);
        }
    }


    public void animateConnectedIcon() {
        AnimationUtils.scaleIn(imgConnectedIcon, 300);
    }

    public void fadeConnectedOut() {
        imgConnectedIcon.animate()
                .setDuration(50)
                .alpha(0)
                .setStartDelay(0)
                .start();
    }

    public void setImgBackIconVisible() {
        imgBackIcon.setAlpha(1f);
    }

    public void fadeBackOut() {
        imgBackIcon.animate()
                .setDuration(50)
                .alpha(0)
                .setStartDelay(0)
                .start();
    }

    private void resetPinCodeView() {
        pinCircle1.setVisibility(VISIBLE);
        pinCircle2.setVisibility(VISIBLE);
        pinCircle3.setVisibility(VISIBLE);
        pinCircle4.setVisibility(VISIBLE);
        editTextCode.setText("");
        txtCode1.setText("");
        txtCode2.setText("");
        txtCode3.setText("");
        txtCode4.setText("");
    }

}
