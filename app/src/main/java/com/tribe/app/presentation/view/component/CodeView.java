package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

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

    @BindView(R.id.imgConnectedIcon)
    ImageView imgConnectedIcon;

    // OBSERVABLES
    private Unbinder unbinder;
    private PublishSubject<Boolean> codeValid = PublishSubject.create();

    public CodeView(Context context) {
        super(context);
    }

    public CodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_code, this);
        unbinder = ButterKnife.bind(this);

        editTextCode.setLetterSpacing((float) 1.5);
        imgConnectedIcon.setScaleX(0);
        imgConnectedIcon.setScaleY(0);

        RxTextView.textChanges(editTextCode).map(CharSequence::toString)
                .map(s -> {
                    switch (s.length()) {
                        case 0:
                            pinCircle1.setVisibility(VISIBLE);
                            pinCircle2.setVisibility(VISIBLE);
                            pinCircle3.setVisibility(VISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            break;
                        case 1:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(VISIBLE);
                            pinCircle3.setVisibility(VISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            break;
                        case 2:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(INVISIBLE);
                            pinCircle3.setVisibility(VISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            break;
                        case 3:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(INVISIBLE);
                            pinCircle3.setVisibility(INVISIBLE);
                            pinCircle4.setVisibility(VISIBLE);
                            break;
                        case 4:
                            pinCircle1.setVisibility(INVISIBLE);
                            pinCircle2.setVisibility(INVISIBLE);
                            pinCircle3.setVisibility(INVISIBLE);
                            pinCircle4.setVisibility(INVISIBLE);
                            break;
                    }
                    return s.length() == 4;
                })
                .subscribe(codeValid);
    }

    /**
     * Obeservable
     */

    public Observable<Boolean> codeValid() {
        return codeValid;
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

    public ImageView getBackIcon() {
        return imgBackIcon;
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

}
