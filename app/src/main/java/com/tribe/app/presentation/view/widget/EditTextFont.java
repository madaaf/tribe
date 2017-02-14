package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.FontCache;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class EditTextFont extends EditText {

  PublishSubject<Void> keyBackPressed = PublishSubject.create();

  public EditTextFont(Context context) {
    super(context);
  }

  public EditTextFont(Context context, AttributeSet attrs) {
    super(context, attrs);
    setCustomFont(context, attrs);
  }

  public EditTextFont(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setCustomFont(context, attrs);
  }

  private void setCustomFont(Context ctx, AttributeSet attrs) {
    TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewFont);
    String customFont = a.getString(R.styleable.TextViewFont_customFont);
    setCustomFont(ctx, customFont);
    a.recycle();
  }

  public boolean setCustomFont(Context ctx, String asset) {
    Typeface tf = FontCache.getTypeface(asset, ctx);
    setTypeface(tf);
    return true;
  }

  @Override public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      keyBackPressed.onNext(null);
    }
    return super.dispatchKeyEvent(event);
  }

  public Observable<Void> keyBackPressed() {
    return keyBackPressed;
  }
}