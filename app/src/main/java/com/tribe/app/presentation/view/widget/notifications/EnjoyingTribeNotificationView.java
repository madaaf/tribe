package com.tribe.app.presentation.view.widget.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.TextViewFont;

/**
 * Created by madaaflak on 04/04/2017.
 */

public class EnjoyingTribeNotificationView extends LifeNotification {

  public final static int MIN_USER_CALL_COUNT = 10;
  public final static int MIN_USER_CALL_MINUTES = 30;

  @BindView(R.id.enjoyingTribePopupView) LinearLayout enjoyingTribeNotificationView;
  @BindView(R.id.btnAction1) TextViewFont btnAction1;
  @BindView(R.id.btnAction2) TextViewFont btnAction2;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

  public EnjoyingTribeNotificationView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public EnjoyingTribeNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  ///////////////////
  //    ON CLICK   //
  ///////////////////

  @OnClick(R.id.btnAction1) void onClickAction1() {
    subscriptions.add(DialogFactory.dialog(getContext(),
        EmojiParser.demojizedText(getContext().getString(R.string.popup_feedback_title)),
        getContext().getString(R.string.popup_feedback_subtitle),
        getContext().getString(R.string.popup_feedback_action2),
        getContext().getString(R.string.popup_feedback_action1)).
        subscribe(positiveAction -> {
          if (positiveAction) {
            String[] emails = new String[] {
                getContext().getString(R.string.popup_feedback_tribe_mail)
            };
            navigator.composeEmail(getContext(), emails,
                getContext().getString(R.string.popup_feedback_tribe_mail_subject));
          }
          hideView();
        }));
  }

  @OnClick(R.id.btnAction2) void onClickAction2() {
    subscriptions.add(DialogFactory.dialog(getContext(),
        EmojiParser.demojizedText(getContext().getString(R.string.popup_review_playstore_title)),
        EmojiParser.demojizedText(getContext().getString(R.string.popup_review_playstore_subtitle)),
        getContext().getString(R.string.popup_review_playstore_action2),
        getContext().getString(R.string.popup_review_playstore_action1))
        .subscribe(positiveAction -> {
          if (positiveAction) {
            navigator.rateApp(getContext());
          }
          hideView();
        }));
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_enjoying_tribe_notification, this, true);

    unbinder = ButterKnife.bind(this);

    enjoyingTribeNotificationView.setVisibility(VISIBLE);
    btnAction1.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.enjoying_tribe_popup_action1)));
    btnAction2.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.enjoying_tribe_popup_action2)));
  }
}
