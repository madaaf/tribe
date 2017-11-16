package com.tribe.app.presentation.view.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.tribelivesdk.game.Game;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public final class DialogFactory {

  public static Dialog createConfirmationDialog(Context context, String title, String message,
      String positiveMessage, String negativeMessage,
      DialogInterface.OnClickListener positiveListener,
      DialogInterface.OnClickListener negativeListener) {
    ContextThemeWrapper themedContext;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      themedContext =
          new ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
    } else {
      themedContext =
          new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
    }

    AlertDialog.Builder alertDialog = new AlertDialog.Builder(themedContext).setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveMessage, positiveListener)
        .setNegativeButton(negativeMessage, negativeListener);
    return alertDialog.create();
  }

  public static ProgressDialog createProgressDialog(Context context, int title) {
    int themedContext;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      themedContext = android.R.style.Theme_Material_Light_Dialog_NoActionBar;
    } else {
      themedContext = android.R.style.Theme_Holo_Light_Dialog_NoActionBar;
    }

    ProgressDialog pd = new ProgressDialog(context, themedContext);
    pd.setTitle(title);
    return pd;
  }

  public static Observable<Boolean> dialog(Context context, String title, String message,
      String positiveMessage, String negativeMessage) {
    return Observable.create((Subscriber<? super Boolean> subscriber) -> {

      ContextThemeWrapper themedContext;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        themedContext = new ContextThemeWrapper(context,
            android.R.style.Theme_Material_Light_Dialog_NoActionBar);
      } else {
        themedContext =
            new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
      }

      final AlertDialog ad = new AlertDialog.Builder(themedContext).setTitle(title)
          .setMessage(message)
          .setPositiveButton(positiveMessage, (dialog, which) -> {
            subscriber.onNext(true);
            subscriber.onCompleted();
          })
          .setNegativeButton(negativeMessage, (dialog, which) -> {
            subscriber.onNext(false);
            subscriber.onCompleted();
          })
          .create();

      subscriber.add(Subscriptions.create(ad::dismiss));
      ad.show();
    });
  }

  public static Observable<String> inputDialog(Context context, String title, String body,
      String positiveMessage, String negativeMessage, int inputType) {
    return Observable.create((Subscriber<? super String> subscriber) -> {

      ContextThemeWrapper themedContext;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        themedContext = new ContextThemeWrapper(context,
            android.R.style.Theme_Material_Light_Dialog_NoActionBar);
      } else {
        themedContext =
            new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
      }

      View parent = LayoutInflater.from(context).inflate(R.layout.view_edit_shortcut_name, null);
      EditTextFont et = parent.findViewById(R.id.editTxtName);
      et.setInputType(inputType);

      final AlertDialog ad = new AlertDialog.Builder(themedContext).setTitle(title)
          .setMessage(body)
          .setPositiveButton(positiveMessage, (dialog, which) -> {

            if (et.getText() != null) {
              subscriber.onNext(et.getText().toString());
            }

            subscriber.onCompleted();
          })
          .setNegativeButton(negativeMessage, (dialog, which) -> subscriber.onCompleted())
          .setView(parent)
          .create();

      subscriber.add(Subscriptions.create(ad::dismiss));
      ad.show();
    });
  }

  private static Observable<LabelType> createBottomSheet(Context context,
      List<LabelType> genericTypeList) {
    return Observable.create((Subscriber<? super LabelType> subscriber) -> {

      View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_base, null);
      RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBottomSheet);
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      LabelSheetAdapter labelSheetAdapter = new LabelSheetAdapter(context, genericTypeList);
      labelSheetAdapter.setHasStableIds(true);
      recyclerView.setAdapter(labelSheetAdapter);
      Subscription clickSubscription = labelSheetAdapter.clickLabelItem()
          .map((View labelView) -> labelSheetAdapter.getItemAtPosition(
              (Integer) labelView.getTag(R.id.tag_position)))
          .subscribe(labelType -> {
            subscriber.onNext(labelType);
            subscriber.onCompleted();
          });

      BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
      bottomSheetDialog.setContentView(view);

      subscriber.add(Subscriptions.create(() -> {
        clickSubscription.unsubscribe();
        labelSheetAdapter.releaseSubscriptions();
        bottomSheetDialog.dismiss();
      }));

      bottomSheetDialog.show();
    });
  }

  public static Observable<LabelType> showBottomSheetForCamera(Context context) {
    return createBottomSheet(context, generateLabelsForCamera(context));
  }

  public static Observable<LabelType> showBottomSheetForMoreBtn(Context context,
      String displayName) {
    return createBottomSheet(context, generateLabelsForMoreBtn(context, displayName));
  }

  private static List<LabelType> generateLabelsForCamera(Context context) {
    List<LabelType> cameraTypeList = new ArrayList<>();
    cameraTypeList.add(
        new LabelType(EmojiParser.demojizedText(context.getString(R.string.image_picker_camera)),
            LabelType.OPEN_CAMERA));
    cameraTypeList.add(
        new LabelType(EmojiParser.demojizedText(context.getString(R.string.image_picker_library)),
            LabelType.OPEN_PHOTOS));
    return cameraTypeList;
  }

  public static Observable<LabelType> showBottomSheetForRecipient(Context context,
      Recipient recipient) {
    return createBottomSheet(context, generateLabelsForRecipient(context, recipient));
  }

  private static List<LabelType> generateLabelsForMoreBtn(Context context, String displayName) {
    List<LabelType> cameraTypeList = new ArrayList<>();
    cameraTypeList.add(new LabelType(context.getString(R.string.action_report) + " " + displayName,
        LabelType.REPORT));
    cameraTypeList.add(new LabelType(context.getString(R.string.action_cancel), LabelType.CANCEL));
    return cameraTypeList;
  }

  private static List<LabelType> generateLabelsForRecipient(Context context, Recipient recipient) {
    List<LabelType> moreTypeList = new ArrayList<>();

    if (recipient instanceof Shortcut) {
      Shortcut shortcut = (Shortcut) recipient;

      if (!shortcut.isSingle()) {
        moreTypeList.add(new LabelType(
            EmojiParser.demojizedText(context.getString(R.string.home_menu_shortcut_customize)),
            LabelType.CUSTOMIZE));
      }

/*      moreTypeList.add(new LabelType(
          EmojiParser.demojizedText(context.getString(R.string.home_block_group_shortcut_title)),
          LabelType.BLOCK_GROUP));*/

      if (!shortcut.isMute()) {
        moreTypeList.add(new LabelType(
            EmojiParser.demojizedText(context.getString(R.string.grid_menu_friendship_mute)),
            LabelType.MUTE));
      } else {
        moreTypeList.add(new LabelType(
            EmojiParser.demojizedText(context.getString(R.string.grid_menu_friendship_unmute)),
            LabelType.UNMUTE));
      }

      moreTypeList.add(new LabelType(
          context.getString(R.string.grid_menu_friendship_hide, recipient.getDisplayName()),
          LabelType.HIDE));

      // if (shortcut.isSingle()) {
      if (true) {
        moreTypeList.add(new LabelType(context.getString(R.string.home_block_shortcut_validate),
            LabelType.BLOCK_HIDE));
      }
    } else if (recipient instanceof Invite) {
      moreTypeList.add(
          new LabelType(context.getString(R.string.grid_menu_invite_decline), LabelType.DECLINE));
    }

    return moreTypeList;
  }

  public static Observable<LabelType> showBottomSheetForFollow(Context context) {
    return createBottomSheet(context, generateLabelsForFollow(context));
  }

  private static List<LabelType> generateLabelsForFollow(Context context) {
    List<LabelType> followTypes = new ArrayList<>();

    followTypes.add(
        new LabelType(context.getString(R.string.settings_follow_twitter), LabelType.TWITTER));
    followTypes.add(
        new LabelType(context.getString(R.string.settings_follow_instagram), LabelType.INSTAGRAM));
    followTypes.add(
        new LabelType(context.getString(R.string.settings_follow_snapchat), LabelType.SNAPCHAT));
    return followTypes;
  }

  public static Observable<LabelType> showBottomSheetForFacebookAuth(Context context) {
    return createBottomSheet(context, generateLabelsForFacebookAuth(context));
  }

  private static List<LabelType> generateLabelsForFacebookAuth(Context context) {
    List<LabelType> followTypes = new ArrayList<>();

    followTypes.add(new LabelType("Login", LabelType.LOGIN));
    followTypes.add(new LabelType("Force logout", LabelType.FORCE_LOGOUT));
    return followTypes;
  }

  public static Observable<LabelType> showBottomSheetForPhoneNumberAuth(Context context) {
    return createBottomSheet(context, generateLabelsForPhoneNumberAuth(context));
  }

  private static List<LabelType> generateLabelsForPhoneNumberAuth(Context context) {
    List<LabelType> followTypes = new ArrayList<>();

    followTypes.add(new LabelType("Login", LabelType.LOGIN));
    followTypes.add(new LabelType("Login (alternative)", LabelType.LOGIN_ALTERNATIVE));
    followTypes.add(new LabelType("Login (call)", LabelType.LOGIN_CALL));
    return followTypes;
  }

  public static Observable<LabelType> showBottomSheetForInvites(Context context) {
    return createBottomSheet(context, generateLabelsForInvites(context));
  }

  private static List<LabelType> generateLabelsForInvites(Context context) {
    List<LabelType> moreTypeList = new ArrayList<>();

    moreTypeList.add(new LabelType(
        EmojiParser.demojizedText(context.getString(R.string.topbar_invite_search_action)),
        LabelType.SEARCH));
    moreTypeList.add(new LabelType(
        EmojiParser.demojizedText(context.getString(R.string.topbar_invite_sms_action_android)),
        LabelType.INVITE_SMS));

    if (DeviceUtils.appInstalled(context, "com.whatsapp")) {
      moreTypeList.add(new LabelType(
          EmojiParser.demojizedText(context.getString(R.string.topbar_invite_whatsapp_action)),
          LabelType.INVITE_WHATSAPP));
    }

    if (DeviceUtils.appInstalled(context, "com.facebook.orca")) {
      moreTypeList.add(new LabelType(
          EmojiParser.demojizedText(context.getString(R.string.topbar_invite_messenger_action)),
          LabelType.INVITE_MESSENGER));
    }

    return moreTypeList;
  }

  public static Observable<LabelType> showBottomSheetForGame(Context context, Game game) {
    return createBottomSheet(context, generateLabelsForGame(context, game));
  }

  private static List<LabelType> generateLabelsForGame(Context context, Game game) {
    List<LabelType> gameLabels = new ArrayList<>();

    gameLabels.add(new LabelType(context.getString(R.string.game_menu_play_another_game),
        LabelType.GAME_PLAY_ANOTHER));
    gameLabels.add(new LabelType(context.getString(R.string.game_menu_reset_scores),
        LabelType.GAME_RESET_SCORES));
    gameLabels.add(
        new LabelType(context.getString(R.string.game_post_it_menu_stop), LabelType.GAME_STOP));

    return gameLabels;
  }

  public static Observable<LabelType> showBottomSheetForCustomizeShortcut(Context context,
      Shortcut shortcut) {
    return createBottomSheet(context, generateLabelsForCustomizeShortcut(context, shortcut));
  }

  private static List<LabelType> generateLabelsForCustomizeShortcut(Context context,
      Shortcut shortcut) {
    List<LabelType> moreTypeList = new ArrayList<>();

    moreTypeList.add(new LabelType(
        EmojiParser.demojizedText(context.getString(R.string.home_menu_shortcut_update_name)),
        LabelType.CHANGE_NAME));

    moreTypeList.add(new LabelType(
        EmojiParser.demojizedText(context.getString(R.string.home_menu_shortcut_update_avatar)),
        LabelType.CHANGE_PICTURE));

    return moreTypeList;
  }
}