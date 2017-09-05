package com.tribe.app.presentation.view.widget.picto;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;

/**
 * Created by tiago on 09/05/17.
 */
public class PictoChatView extends FrameLayout {

  @IntDef({ ACTIVE, INACTIVE }) public @interface Status {
  }

  public static final int ACTIVE = 0;
  public static final int INACTIVE = 1;

  @BindView(R.id.imgChat) ImageView imgChat;

  private int status;
  private Unbinder unbinder;

  public PictoChatView(Context context) {
    super(context);
    init();
  }

  public PictoChatView(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PictoChatView);
    status = a.getInt(R.styleable.PictoChatView_status, INACTIVE);
    a.recycle();

    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_picto_chat, this);
    unbinder = ButterKnife.bind(this);
    setStatus(status);
  }

  public void setStatus(@Status int status) {
    this.status = status;
    if (status == INACTIVE) {
      imgChat.setImageResource(R.drawable.picto_chat_inactive);
    } else {
      imgChat.setImageResource(R.drawable.picto_chat_active);
    }
  }
}
