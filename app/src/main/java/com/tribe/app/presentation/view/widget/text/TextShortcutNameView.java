package com.tribe.app.presentation.view.widget.text;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

/**
 * Created by tiago on 09/22/17.
 */
public class TextShortcutNameView extends LinearLayout {

  @IntDef({ NORMAL, CHAT, LIVE }) public @interface TextType {
  }

  public static final int NORMAL = 0;
  public static final int CHAT = 1;
  public static final int LIVE = 2;

  @BindView(R.id.txtNameShortcut) TextViewFont txtNameShortcut;

  private int textType = NORMAL;
  private Unbinder unbinder;

  public TextShortcutNameView(Context context) {
    super(context);
    init();
  }

  public TextShortcutNameView(Context context, AttributeSet attrs) {
    super(context, attrs);

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
    LayoutInflater.from(getContext()).inflate(R.layout.view_text_shortcut_name, this);
    unbinder = ButterKnife.bind(this);

    setGravity(Gravity.CENTER_VERTICAL);
  }

  public void setTextType(@TextType int textType) {
    if (this.textType == textType) return;

    this.textType = textType;

    if (textType == NORMAL) {
      setVisibility(View.GONE);
    } else {
      setVisibility(View.VISIBLE);

      if (textType == CHAT) {
        TextViewCompat.setTextAppearance(txtNameShortcut, R.style.Title_2_Blue);
      } else if (textType == LIVE) {
        TextViewCompat.setTextAppearance(txtNameShortcut, R.style.Title_2_Red);
      } else {
        TextViewCompat.setTextAppearance(txtNameShortcut, R.style.Title_2_Black);
      }

      txtNameShortcut.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
    }
  }

  public void setRecipient(Recipient recipient) {
    String shortcutName = "";

    if (recipient instanceof Shortcut) {
      Shortcut shortcut = (Shortcut) recipient;
      shortcutName = shortcut.getName();
    } else if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      shortcutName = invite.getShortcut().getName();
    }

    if (StringUtils.isEmpty(shortcutName)) {
      setVisibility(View.GONE);
    } else {
      setVisibility(View.VISIBLE);
      txtNameShortcut.setText(shortcutName);
    }
  }
}
