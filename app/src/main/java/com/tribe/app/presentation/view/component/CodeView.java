package com.tribe.app.presentation.view.component;

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
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.IntroVideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/06/2016.
 */
public class CodeView extends FrameLayout {

    @BindView(R.id.editTextCode)
    EditTextFont editTextCode;

    @BindView(R.id.circularProgressViewCode)
    CircularProgressView circularProgressViewCode;

    @BindView(R.id.imgBackIcon)
    ImageView imgBackIcon;

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

        RxTextView.textChanges(editTextCode).map(CharSequence::toString)
                .map(s -> {
                    return s.length() == 4;
                })
                .subscribe(codeValid);
    }

    public String getCode() {
        return editTextCode.getText().toString();
    }

    public Observable<Boolean> codeValid() {
        return codeValid;
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
}
