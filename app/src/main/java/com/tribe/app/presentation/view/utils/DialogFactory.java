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
import com.tribe.app.domain.entity.GenericType;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.LabelType;
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

    /**
     * Bottom sheet set-up
     */
    public static Observable<GenericType> showBottomSheetForGroupMembers(Context context, GroupMember groupMember) {
        return Observable.create((Subscriber<? super GenericType> subscriber) -> {

            View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_base, null);
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewBottomSheet);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            LabelSheetAdapter labelSheetAdapter = new LabelSheetAdapter(context, generateLabelsForGroupMember(context, groupMember));
            labelSheetAdapter.setHasStableIds(true);
            recyclerView.setAdapter(labelSheetAdapter);
            Subscription clickSubscription = labelSheetAdapter.clickLabelItem()
                    .map((View labelView) -> labelSheetAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                    .subscribe(labelType -> {
                        GenericType genericType = (GenericType) labelType;
                        subscriber.onNext(genericType);
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

    private static List<LabelType> generateLabelsForGroupMember(Context context, GroupMember groupMember) {
        List<LabelType> genericTypes = new ArrayList<>();

        if (groupMember.isAdmin())
            genericTypes.add(new GenericType(context.getString(R.string.group_members_action_remove_admin), GenericType.REMOVE_FROM_ADMIN));
        else
            genericTypes.add(new GenericType(context.getString(R.string.group_members_action_add_admin), GenericType.SET_AS_ADMIN));

        genericTypes.add(new GenericType(context.getString(R.string.group_members_action_remove_member), GenericType.REMOVE_FROM_GROUP));

        return genericTypes;
    }
}