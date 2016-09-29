package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/26/16.
 */
public class GroupSuggestionsView extends FrameLayout {
    public GroupSuggestionsView(Context context) {
        super(context);
    }

    public GroupSuggestionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<String> groupSuggestionClicked = PublishSubject.create();

    @BindView(R.id.textFamily)
    TextViewFont textFamily;
    @BindView(R.id.textRoomies)
    TextViewFont textRoomies;
    @BindView(R.id.textBffs)
    TextViewFont textBffs;
    @BindView(R.id.textAbroad)
    TextViewFont textAbroad;
    @BindView(R.id.textWork)
    TextViewFont textWork;
    @BindView(R.id.textClassmates)
    TextViewFont textClassmates;
    @BindView(R.id.textTeammates)
    TextViewFont textTeammates;
    @BindView(R.id.textBaddest)
    TextViewFont textBaddest;
    @BindView(R.id.textNotAllowed)
    TextViewFont textNotAllowed;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_group_suggestions, this);
        unbinder = ButterKnife.bind(this);

        textFamily.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_family_title)));
        textRoomies.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_roomies_title)));
        textBffs.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_bffs_title)));
        textAbroad.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_abroad_title)));
        textWork.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_work_title)));
        textClassmates.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_classmates_title)));
        textTeammates.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_teammates_title)));
        textBaddest.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_baddest_title)));
        textNotAllowed.setText(EmojiParser.demojizedText(getContext().getString(R.string.group_public_not_allowed_title)));

        // Subscriptions
        subscriptions.add(RxView.clicks(textFamily).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textFamily.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textRoomies).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textRoomies.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textBffs).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textBffs.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textAbroad).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textAbroad.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textWork).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textWork.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textClassmates).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textClassmates.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textTeammates).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textTeammates.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textBaddest).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textBaddest.getText().toString());
        }));

        subscriptions.add(RxView.clicks(textNotAllowed).subscribe(aVoid -> {
            groupSuggestionClicked.onNext(textNotAllowed.getText().toString());
        }));
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
    }

    public Observable<String> groupSuggestionClicked() {
        return groupSuggestionClicked;
    }
}
