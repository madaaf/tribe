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
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.TextViewUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 09/05/17.
 */
public class TextHomeNameActionView extends LinearLayout {

  @IntDef({ NORMAL, CHAT, LIVE }) public @interface TextType {
  }

  public static final int NORMAL = 0;
  public static final int CHAT = 1;
  public static final int LIVE = 2;

  @Inject User currentUser;

  @BindView(R.id.txtName) TextViewFont txtName;
  @BindView(R.id.txtAction) TextViewFont txtAction;

  private int textType = NORMAL;
  private Unbinder unbinder;
  private Recipient recipient;

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
    this.textType = textType;

    if (textType == NORMAL) {
      TextViewCompat.setTextAppearance(txtName, R.style.BiggerTitle_1_Black);
      txtName.setCustomFont(getContext(), FontUtils.PROXIMA_REGULAR);

      TextViewCompat.setTextAppearance(txtAction, R.style.BiggerBody_One_Grey);
    } else {
      TextViewCompat.setTextAppearance(txtName, R.style.BiggerTitle_2_Black);
      txtName.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

      if (textType == CHAT) {
        TextViewCompat.setTextAppearance(txtAction, R.style.BiggerBody_One_Blue);
        txtAction.setText(R.string.home_action_read_chat);
      } else {
        TextViewCompat.setTextAppearance(txtAction, R.style.BiggerBody_One_Red);
        txtAction.setText(R.string.home_action_join_live);
      }
    }

    txtAction.setCustomFont(getContext(), FontUtils.PROXIMA_REGULAR);
  }

  public void setRecipient(Recipient recipient) {
    this.recipient = recipient;

    setTextType(textType);

    if (recipient instanceof Shortcut) {
      Shortcut shortcut = (Shortcut) recipient;

      if (!StringUtils.isEmpty(shortcut.getName())) {
        txtName.setText(shortcut.getName());
      } else if (shortcut.isSingle()) {
        txtName.setText(shortcut.getUserDisplayNames());
      } else {
        List<User> userList = shortcut.getMembers();
        TextViewUtils.constraintTextInto(txtName, userList);
      }

      boolean override = false;
      if (shortcut.isSingle()) {
        User member = shortcut.getSingleFriend();
        if (member.isPlaying() != null && !StringUtils.isEmpty(member.isPlaying().getGame_id())) {
          txtAction.setText(
              getContext().getString(R.string.home_action_is_playing, member.isPlaying().getTitle(),
                  member.isPlaying().getEmoji()));
          override = true;
          TextViewCompat.setTextAppearance(txtAction, R.style.BiggerBody_One_BlueNew);
          txtAction.setCustomFont(getContext(), FontUtils.PROXIMA_REGULAR);
        }
      }

      if (!override) {
        if (!shortcut.isRead()) {
          txtAction.setText(R.string.home_action_read_chat);
        } else if (!StringUtils.isEmpty(shortcut.getLastMessage())) {
          txtAction.setText(shortcut.getLastMessage());
        } else {
          txtAction.setText(R.string.home_action_tap_to_chat);
        }
      }
    } else if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;

      if (invite.getShortcut() == null || StringUtils.isEmpty(invite.getShortcut().getName())) {
        List<User> userList = invite.getAllUsers();
        userList.remove(currentUser);
        if (userList.size() == 0) {
          txtName.setText(invite.getDisplayName());
        } else {
          TextViewUtils.constraintTextInto(txtName, userList);
        }
      } else {
        txtName.setText(recipient.getDisplayName());
      }
    } else {
      txtName.setText(recipient.getDisplayName());
    }
  }
}
