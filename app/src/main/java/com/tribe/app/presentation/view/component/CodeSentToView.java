package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by horatiothomas on 10/18/16.
 */
public class CodeSentToView extends FrameLayout {
  public CodeSentToView(Context context) {
    super(context);
  }

  public CodeSentToView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CodeSentToView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private Unbinder unbinder;

  @BindView(R.id.textPhoneNumber) TextViewFont textPhoneNumber;

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    initUi();
  }

  public void initUi() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_code_sent_to, this);
    unbinder = ButterKnife.bind(this);
  }

  public void setTextPhoneNumber(String phoneNumber) {
    textPhoneNumber.setText(phoneNumber);
  }
}
