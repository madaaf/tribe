package com.tribe.app.presentation.view.widget.text;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

/**
 * Created by tiago on 09/05/17.
 */
public class TextHomeNameActionView extends LinearLayout {

  @IntDef({ NORMAL, CHAT, LIVE }) public @interface TextType {
  }

  public static final int NORMAL = 0;
  public static final int CHAT = 1;
  public static final int LIVE = 2;

  @BindView(R.id.txtName) TextViewFont txtName;
  @BindView(R.id.txtAction) TextViewFont txtAction;

  private int textType = NORMAL;
  private Unbinder unbinder;

  public TextHomeNameActionView(Context context) {
    super(context);
    init();
  }

  public TextHomeNameActionView(Context context, AttributeSet attrs) {
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
    LayoutInflater.from(getContext()).inflate(R.layout.view_text_home_name_action, this);
    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER_VERTICAL);
  }

  public void setTextType(@TextType int textType) {
    if (this.textType == textType) return;

    this.textType = textType;

    if (textType == NORMAL) {
      TextViewCompat.setTextAppearance(txtName, R.style.Title_1_Black);
      txtName.setCustomFont(getContext(), FontUtils.PROXIMA_REGULAR);

      TextViewCompat.setTextAppearance(txtAction, R.style.Body_One_Grey);
      txtAction.setCustomFont(getContext(), FontUtils.PROXIMA_REGULAR);
    } else {
      TextViewCompat.setTextAppearance(txtName, R.style.Title_2_Black);
      txtName.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

      if (textType == CHAT) {
        TextViewCompat.setTextAppearance(txtAction, R.style.Body_One_Blue);
        txtAction.setText(R.string.home_action_read_chat);
      } else {
        TextViewCompat.setTextAppearance(txtAction, R.style.Body_One_Red);
        txtAction.setText(R.string.home_action_join_live);
      }
    }
  }

  public void setRecipient(Recipient recipient) {
    if (recipient instanceof Shortcut) {
      Shortcut shortcut = (Shortcut) recipient;
      if (!StringUtils.isEmpty(shortcut.getName())) {
        txtName.setText(shortcut.getName());
      } else {
        txtName.setText(shortcut.getUserDisplayNames());
      }

      if (!StringUtils.isEmpty(shortcut.getLastMessage())) {
        txtAction.setText(shortcut.getLastMessage());
      } else {
        txtAction.setText(R.string.home_action_tap_to_chat);
      }
    } else {
      txtName.setText(recipient.getDisplayName());
    }
  }
}
