package com.tribe.app.presentation.view.tutorial;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.StringDef;
import android.view.Gravity;
import android.view.View;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.scope.TutorialState;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TutorialManager {

    private ScreenUtils screenUtils;
    private Preference<Set<String>> tutorialState;

    @StringDef({REFRESH, MESSAGES_SUPPORT, NEXT, CLOSE, REPLY_MODE, REPLY, RELEASE, CANCEL})
    public @interface TutorialKey {
    }

    public static final String REFRESH = "REFRESH";
    public static final String MESSAGES_SUPPORT = "MESSAGES_SUPPORT";
    public static final String NEXT = "NEXT";
    public static final String CLOSE = "CLOSE";
    public static final String REPLY_MODE = "REPLY_MODE";
    public static final String REPLY = "REPLY";
    public static final String RELEASE = "RELEASE";
    public static final String CANCEL = "CANCEL";

    @Inject
    public TutorialManager(ScreenUtils screenUtils, @TutorialState Preference<Set<String>> tutorialState) {
        this.screenUtils = screenUtils;
        this.tutorialState = tutorialState;
    }

    public void addTutorialKey(@TutorialKey String key) {
        if (!StringUtils.isEmpty(key)) {
            Set<String> tut = tutorialState.get();
            tut.add(key);
            tutorialState.set(tut);
        }
    }

    public boolean shouldDisplay(@TutorialKey String key) {
        boolean result = false;

        if (key.equals(REPLY)) return true;

        if (tutorialState.get().contains(key)) return result;

        if (key.equals(REFRESH) && tutorialState.get().contains(MESSAGES_SUPPORT)
                || key.equals(CLOSE) && tutorialState.get().contains(NEXT)
                || key.equals(REPLY_MODE) && tutorialState.get().contains(CLOSE)
                || key.equals(REPLY) && tutorialState.get().contains(REPLY_MODE)
                || key.equals(RELEASE) && tutorialState.get().contains(REPLY)
                || key.equals(CANCEL) && tutorialState.get().contains(RELEASE)
                || (key.equals(NEXT) || key.equals(MESSAGES_SUPPORT))) {
            result = true;
        }

        return result;
    }

    public Tutorial showRefresh(Activity activity, View target, int holeRadius, Bitmap overlayImage, int overlayImageSize,
                                View.OnClickListener onClickListener) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, REFRESH).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_refresh))
                            .setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                            .setBackgroundRes(R.drawable.bg_tuto_center_downward)
                    )
                    .setOverlay(new Overlay(activity)
                            .setHoleRadius(holeRadius)
                            .setStyle(Overlay.RECTANGLE)
                            .setHoleCornerRadius(screenUtils.dpToPx(5))
                            .setImageOverlay(overlayImage)
                            .setImageOverlaySize(overlayImageSize)
                            .setImageOverlayOffsetX((target.getHeight() >> 1) - (overlayImageSize >> 1))
                            .setImageOverlayOffsetY((target.getHeight() >> 1) - (overlayImageSize >> 1))
                            .withDefaultAnimation(true)
                            .setOnClickListener(onClickListener)
                    ).playOn(target);
        }

        return null;
    }

    public Tutorial showMessagesSupport(Activity activity, View target, int toolTipOffsetX, int toolTipOffsetY,
                                        int holeRadius, int holeRadiusPulsePadding, Bitmap overlayImage, int overlayImageSize) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, REFRESH).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_support, 2))
                            .setGravity(Gravity.BOTTOM | Gravity.LEFT)
                            .setOffsetX(toolTipOffsetX)
                            .setOffsetY(toolTipOffsetY)
                            .setBackgroundRes(R.drawable.bg_tuto_right_upward)
                    )
                    .setOverlay(new Overlay(activity)
                            .setStyle(Overlay.CIRCLE)
                            .setHoleRadius(holeRadius)
                            .setHoleRadiusPulsePadding(holeRadiusPulsePadding)
                            .setImageOverlay(overlayImage)
                            .setImageOverlaySize(overlayImageSize)
                            .setImageOverlayOffsetX(target.getWidth() - (overlayImageSize >> 1))
                            .setImageOverlayOffsetY((target.getHeight() >> 1) - (overlayImageSize >> 1))
                            .withDefaultAnimation(true)
                    ).playOn(target);
        }

        return null;
    }

    public Tutorial showNext(Activity activity, View target, int toolTipOffsetX, int toolTipOffsetY,
                                        int holeRadius, int holeRadiusPulsePadding, Bitmap overlayImage, int overlayImageSize) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, NEXT).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_next_message))
                            .setGravity(Gravity.TOP | Gravity.LEFT)
                            .setOffsetX(toolTipOffsetX)
                            .setOffsetY(toolTipOffsetY)
                            .setBackgroundRes(R.drawable.bg_tuto_right_downward)
                    )
                    .setOverlay(new Overlay(activity)
                            .setStyle(Overlay.CIRCLE)
                            .setHoleRadius(holeRadius)
                            .setHoleRadiusPulsePadding(holeRadiusPulsePadding)
                            .setImageOverlay(overlayImage)
                            .setImageOverlaySize(overlayImageSize)
                            .setImageOverlayOffsetX((target.getWidth() >> 1) - (overlayImageSize >> 1))
                            .setImageOverlayOffsetY((target.getHeight() >> 1) - (overlayImageSize >> 1))
                            .withDefaultAnimation(true)
                    ).playOn(target);
        }

        return null;
    }

    public Tutorial showClose(Activity activity, View target, int toolTipOffsetX, int toolTipOffsetY,
                             int holeRadius, int holeRadiusPulsePadding, Bitmap overlayImage, int overlayImageSize) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, CLOSE).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_close_message))
                            .setGravity(Gravity.TOP | Gravity.LEFT)
                            .setOffsetX(toolTipOffsetX)
                            .setOffsetY(toolTipOffsetY)
                            .setBackgroundRes(R.drawable.bg_tuto_right_downward)
                    )
                    .setOverlay(new Overlay(activity)
                            .setStyle(Overlay.CIRCLE)
                            .setHoleRadius(holeRadius)
                            .setHoleRadiusPulsePadding(holeRadiusPulsePadding)
                            .setImageOverlay(overlayImage)
                            .setImageOverlaySize(overlayImageSize)
                            .setImageOverlayOffsetX((target.getWidth() >> 1) - (overlayImageSize >> 1))
                            .setImageOverlayOffsetY((target.getHeight() >> 1) - (overlayImageSize >> 1))
                            .withDefaultAnimation(true)
                    ).playOn(target);
        }

        return null;
    }

    public Tutorial showReplyMode(Activity activity, View target, int holeRadius, int holeRadiusPulsePadding, View.OnClickListener onClickListener) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, REPLY_MODE).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_reply))
                            .setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                            .setBackgroundRes(R.drawable.bg_tuto_center_downward)
                    )
                    .setOverlay(new Overlay(activity)
                            .setStyle(Overlay.CIRCLE)
                            .setHoleRadius(holeRadius)
                            .setHoleRadiusPulsePadding(holeRadiusPulsePadding)
                            .withDefaultAnimation(true)
                            .setOnClickListener(onClickListener)
                    ).playOn(target);
        }

        return null;
    }

    public Tutorial showReply(Activity activity, View target, int holeRadius, int holeRadiusPulsePadding, View.OnClickListener onClickListener) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, REPLY).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_record_reply))
                            .setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                            .setBackgroundRes(R.drawable.bg_tuto_center_downward)
                    )
                    .setOverlay(new Overlay(activity)
                            .setStyle(Overlay.CIRCLE)
                            .setBackgroundColor(Color.TRANSPARENT)
                            .setHoleRadius(holeRadius)
                            .setHoleRadiusPulsePadding(holeRadiusPulsePadding)
                            .withDefaultAnimation(true)
                            .setOnClickListener(onClickListener)
                    ).playOn(target);
        }

        return null;
    }

    public Tutorial showRelease(Activity activity, View target) {
        if (activity != null) {
            return Tutorial.init(activity, screenUtils, REPLY).with(Tutorial.CLICK)
                    .setToolTip(new ToolTip(activity, screenUtils)
                            .setTitle(activity.getString(R.string.tutorial_message_release))
                            .setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                            .setBackgroundRes(R.drawable.bg_tuto_no_ind)
                    ).playOn(target);
        }

        return null;
    }
}
