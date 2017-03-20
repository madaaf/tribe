package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.FontCache;

/**
 * Created by tiago on 18/05/2016.
 */
public class TextViewFont extends android.support.v7.widget.AppCompatTextView {

  private String customFont;

  public TextViewFont(Context context) {
    super(context);
  }

  public TextViewFont(Context context, AttributeSet attrs) {
    super(context, attrs);
    setCustomFont(context, attrs);
  }

  public TextViewFont(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setCustomFont(context, attrs);
  }

  private void setCustomFont(Context ctx, AttributeSet attrs) {
    TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewFont);
    customFont = a.getString(R.styleable.TextViewFont_customFont);
    setCustomFont(ctx, customFont);
    a.recycle();
  }

  public boolean setCustomFont(Context ctx, String asset) {
    Typeface tf = FontCache.getTypeface(asset, ctx);
    setTypeface(tf);
    return true;
  }

  public String getCustomFont() {
    return customFont;
  }
}