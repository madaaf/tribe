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
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public final class DialogFactory {

    public static Dialog createConfirmationDialog(Context context, String title,
                                                  String message, String positiveMessage, String negativeMessage,
                                                  DialogInterface.OnClickListener positiveListener,
                                                  DialogInterface.OnClickListener negativeListener) {
        ContextThemeWrapper themedContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        } else {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(themedContext)
                .setTitle(title)
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

    public static Observable<Boolean> dialog(Context context, String title,
                                             String message, String positiveMessage,
                                             String negativeMessage) {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {

            ContextThemeWrapper themedContext;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
            } else {
                themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
            }

            final AlertDialog ad = new AlertDialog.Builder(themedContext)
                    .setTitle(title)
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

    private static Observable<LabelType> createBottomSheet(Context context, List<LabelType> genericTypeList) {
        return Observable.create((Subscriber<? super LabelType> subscriber) -> {

            View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_base, null);
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewBottomSheet);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            LabelSheetAdapter labelSheetAdapter = new LabelSheetAdapter(context, genericTypeList);
            labelSheetAdapter.setHasStableIds(true);
            recyclerView.setAdapter(labelSheetAdapter);
            Subscription clickSubscription = labelSheetAdapter.clickLabelItem()
                    .map((View labelView) -> labelSheetAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
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

    public static Observable<LabelType> showBottomSheetForGroupMembers(Context context, GroupMember groupMember) {
        return createBottomSheet(context, generateLabelsForGroupMember(context, groupMember));
    }

    private static List<LabelType> generateLabelsForGroupMember(Context context, GroupMember groupMember) {
        List<LabelType> labelTypeList = new ArrayList<>();

        if (groupMember.isAdmin())
            labelTypeList.add(new LabelType(context.getString(R.string.group_members_action_remove_admin), LabelType.REMOVE_FROM_ADMIN));
        else
            labelTypeList.add(new LabelType(context.getString(R.string.group_members_action_add_admin), LabelType.SET_AS_ADMIN));

        labelTypeList.add(new LabelType(context.getString(R.string.group_members_action_remove_member), LabelType.REMOVE_FROM_GROUP));

        return labelTypeList;
    }

    public static Observable<LabelType> showBottomSheetForCamera(Context context) {
        return createBottomSheet(context, generateLabelsForCamera(context));
    }

    private static List<LabelType> generateLabelsForCamera(Context context) {
        List<LabelType> cameraTypeList = new ArrayList<>();
        cameraTypeList.add(new LabelType(context.getString(R.string.image_picker_camera), LabelType.OPEN_CAMERA));
        cameraTypeList.add(new LabelType(context.getString(R.string.image_picker_library), LabelType.OPEN_PHOTOS));
        return cameraTypeList;
    }

    public static Observable<LabelType> showBottomSheetForRecipient(Context context, Recipient recipient) {
        return createBottomSheet(context, generateLabelsForRecipient(context, recipient));
    }

    private static List<LabelType> generateLabelsForRecipient(Context context, Recipient recipient) {
        List<LabelType> moreTypeList = new ArrayList<>();

        if (recipient instanceof Friendship) {
            moreTypeList.add(new LabelType(context.getString(R.string.grid_menu_friendship_hide, recipient.getDisplayName()), LabelType.HIDE));
            moreTypeList.add(new LabelType(context.getString(R.string.grid_menu_friendship_block, recipient.getDisplayName()), LabelType.BLOCK_HIDE));
        } else if (recipient instanceof Membership) {
            moreTypeList.add(new LabelType(context.getString(R.string.grid_menu_group_infos), LabelType.GROUP_INFO));
        }

        return moreTypeList;
    }

    public static Observable<LabelType> showBottomSheetForFollow(Context context) {
        return createBottomSheet(context, generateLabelsForFollow(context));
    }

    private static List<LabelType> generateLabelsForFollow(Context context) {
        List<LabelType> followTypes = new ArrayList<>();

        followTypes.add(new LabelType(context.getString(R.string.settings_follow_twitter), LabelType.TWITTER));
        followTypes.add(new LabelType(context.getString(R.string.settings_follow_instagram), LabelType.INSTAGRAM));
        followTypes.add(new LabelType(context.getString(R.string.settings_follow_snapchat), LabelType.SNAPCHAT));
        return followTypes;
    }

    public static Observable<LabelType> showBottomSheetForContact(Context context) {
        return createBottomSheet(context, generateLabelsForContact(context));
    }

    private static List<LabelType> generateLabelsForContact(Context context) {
        List<LabelType> messageTypes = new ArrayList<>();

        messageTypes.add(new LabelType(context.getString(R.string.settings_message_tribe), LabelType.TRIBE));
        messageTypes.add(new LabelType(context.getString(R.string.settings_message_email), LabelType.EMAIL));
        messageTypes.add(new LabelType(context.getString(R.string.settings_message_twitter), LabelType.TWITTER));
        return messageTypes;
    }

    public static Observable<LabelType> showBottomSheetForInvites(Context context) {
        return createBottomSheet(context, generateLabelsForInvites(context));
    }

    private static List<LabelType> generateLabelsForInvites(Context context) {
        List<LabelType> moreTypeList = new ArrayList<>();

        moreTypeList.add(new LabelType(EmojiParser.demojizedText(context.getString(R.string.topbar_invite_search_action)), LabelType.SEARCH));
        moreTypeList.add(new LabelType(EmojiParser.demojizedText(context.getString(R.string.topbar_invite_sms_action_android)), LabelType.INVITE_SMS));

        if (DeviceUtils.appInstalled(context, "com.whatsapp"))
            moreTypeList.add(new LabelType(EmojiParser.demojizedText(context.getString(R.string.topbar_invite_whatsapp_action)), LabelType.INVITE_WHATSAPP));

        if (DeviceUtils.appInstalled(context, "com.facebook.orca"))
            moreTypeList.add(new LabelType(EmojiParser.demojizedText(context.getString(R.string.topbar_invite_messenger_action)), LabelType.INVITE_MESSENGER));

        return moreTypeList;
    }
}