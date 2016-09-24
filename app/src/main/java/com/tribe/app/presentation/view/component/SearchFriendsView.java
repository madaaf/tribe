package com.tribe.app.presentation.view.component;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import android.widget.LinearLayout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by horatiothomas on 9/22/16.
 */
public class SearchFriendsView extends FrameLayout {

    /**
     * Injected Dependencies
     */
    @Inject
    ScreenUtils screenUtils;

    // Butterknife views
    @BindView(R.id.layoutUserPicContainer)
    FrameLayout layoutUserPicContainer;
    @BindView(R.id.layoutParent)
    FrameLayout layoutParent;

    @BindView(R.id.editTextSearch)
    EditTextFont editTextSearch;

    /**
     * Animation Constants
     */
    // Durations
    private int animationDuration;
    private int startDelay;
    // Pixels
    private int moveRightPixels;
    private int moveLeftPixels;
    private int imageSize;
    private int imageMargin;

    /**
     * Other Globals
     */
    // Subscriptions
    private Unbinder unbinder;
    // View tracking
    private Map<String, Integer> userPicChildLocation;
    private int currentRow;
    private int columnCount;
    private int currColumn;
    private int size;
    private boolean columnCountSet;
    private boolean searchMoved;

    public SearchFriendsView(Context context) {
        super(context);
    }

    public SearchFriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchFriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initDependencyInjector();
        LayoutInflater.from(getContext()).inflate(R.layout.view_search_friends, this);
        unbinder = ButterKnife.bind(this);

        initAnimationConstants();
        initViewManagementConstants();
    }

    private void initAnimationConstants() {
        animationDuration = AnimationUtils.ANIMATION_DURATION_SHORT;
        startDelay = AnimationUtils.NO_START_DELAY;
        imageMargin = getResources().getDimensionPixelOffset(R.dimen.vertical_margin_xsmall);
        imageSize = getResources().getDimensionPixelOffset(R.dimen.setting_pic_size);
        moveRightPixels = imageSize + imageMargin * 2;
        moveLeftPixels = -moveRightPixels;
    }

    private void initViewManagementConstants() {
        userPicChildLocation = new HashMap<>();
        currentRow = 1;
        currColumn = 1;
        size = 0;
        columnCountSet = false;
        searchMoved = false;
    }

    // INSERTION
    // use a hashmap to track pic location in view <String : userId, int : location>
    // Check if there is space in first row
    //  if there is space to fit both image and search text
    //    1. add image view to layout in front
    //    2. animate image view appearing and move search text over as well as rest of images
    //  if there is not
    //    check if there is space just to fit one
    //      check if search bar has been moved down
    //        2. if not animate move search bar down
    //     1. add image to layout
    //     2. animate image appearing
    //     if there isnt move image view down
    // DELETION
    // find view by location gotten from hashmap
    // 1. animate view fade out
    //   check if there is enough room to move view in next row up
    //    if there isnt just animate everything right
    //    if there is animate view up as well
    // 1. if not

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    public void insertFriend(String friendId, String pictureUrl) {
        updateAddLocationMap(friendId);
        // room to move text right
        if (roomToMoveRight(editTextSearch) && !searchMoved) {
            if (currentRow > 1) forwardFullRow(currentRow-1);
            moveImagesAndSearchRight();
            currColumn++;
        }
        // Room to move images right
        else if(roomToMoveRight(layoutUserPicContainer.getChildAt(size-1))) {
            if (currentRow > 1) forwardFullRow(currentRow-1);
            if (searchMoved) {
                moveImagesRight();
            } else {
                expandViewAndMoveSearch();
                moveImagesRight();
                searchMoved = true;

            }
            currColumn++;
        } else {
            forwardFullRow(currentRow);
            if (!columnCountSet) {
                columnCount = size - 1;
                columnCountSet = true;
            }
            searchMoved = false;
            // move text right
            currColumn = 1;
            moveSearch(moveRightPixels);
            currentRow++;
        }
        size++;
        addImageToFrontOfLayoutWithAnimation(pictureUrl);
    }

    public void deleteFriend(String friendId) {
//        int removeLoc  = updateRemoveLocationMap(friendId);
//        fadeImageAndRemoveFromLayout(removeLoc);
//        moveImagesAndSearchLeft(removeLoc);

    }

    private int updateRemoveLocationMap(String friendId) {
        int removeLoc = userPicChildLocation.remove(friendId);
        for(Map.Entry<String, Integer> entry : userPicChildLocation.entrySet()) {
            if (entry.getValue() > removeLoc) {
                userPicChildLocation.put(entry.getKey(), entry.getValue() + 1);
            }
        }
        return removeLoc;
    }


    private void updateAddLocationMap(String friendId) {
        for(Map.Entry<String, Integer> entry : userPicChildLocation.entrySet()) {
            userPicChildLocation.put(entry.getKey(), entry.getValue() + 1);
        }
        userPicChildLocation.put(friendId, 0);
    }

    private void moveImagesAndSearchRight() {
        moveImagesRight();
        moveSearch(moveRightPixels);
    }

    private void moveImagesAndSearchLeft(int removeLoc) {
        moveImagesLeft(removeLoc);
        moveSearch(moveLeftPixels);
    }


    private void moveImagesRight() {
        if (columnCountSet) {
            moveViewsRight(layoutUserPicContainer, (currentRow-1) * (columnCount + 1), (currentRow-1) * (columnCount +1) + currColumn);
        } else {
            moveViewsRight(layoutUserPicContainer, 0, currColumn*currentRow - 1);
        }
    }

    private void moveImagesLeft(int removeLoc) {
        if (columnCountSet) {

        } else {
            if (layoutUserPicContainer.getChildAt(removeLoc + 1) != null)
            moveViewsLeft(layoutUserPicContainer, removeLoc+1, currColumn*currentRow -1);
        }
    }

    private void moveSearch(int pixels) {
        moveView(editTextSearch,(int) editTextSearch.getX() + pixels, (int) editTextSearch.getY());
    }

    private void forwardFullRow(int rows) {
        for (int i = 1; i <= rows; i++) {
            int idxMoveRightStart  =  (i-1) * columnCount + i -1;
            int idxMoveRightEnd = i * columnCount + i -1;
            int idxMoveDown = i * columnCount + i -1;
            moveViewsRight(layoutUserPicContainer, idxMoveRightStart, idxMoveRightEnd);
            moveView(layoutUserPicContainer.getChildAt(idxMoveDown), 0, i * imageSize);
        }
    }


    private void backFullRow() {
        currColumn--;
    }

    private void addImageToFrontOfLayoutWithAnimation(String imageUrl) {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageViewLp = new LinearLayout.LayoutParams(imageSize, imageSize);
        imageView.setLayoutParams(imageViewLp);
        imageView.setAlpha(AnimationUtils.ALPHA_NONE);
        layoutUserPicContainer.addView(imageView, 0);
        if (imageUrl == null || imageUrl.isEmpty()) imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_avatar_placeholder));
        else Glide.with(getContext()).load(imageUrl)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(imageView);
        imageView.animate()
                .setDuration(animationDuration)
                .setStartDelay(startDelay)
                .alpha(AnimationUtils.ALPHA_FULL)
                .start();
    }
    private void fadeImageAndRemoveFromLayout(int loc) {
        View view = layoutUserPicContainer.getChildAt(loc);
        view.animate()
                .setDuration(animationDuration)
                .setStartDelay(startDelay)
                .alpha(AnimationUtils.ALPHA_NONE)
                .start();
        layoutUserPicContainer.removeViewAt(loc);
    }

    private void expandViewAndMoveSearch() {
        AnimationUtils.animateHeightFrameLayout(layoutParent, layoutParent.getHeight(), layoutParent.getHeight() + imageSize, animationDuration);
        AnimationUtils.animateHeightFrameLayout(layoutUserPicContainer, layoutUserPicContainer.getHeight(), layoutUserPicContainer.getHeight() + imageSize, animationDuration);
        moveView(editTextSearch, 0, imageSize *currentRow);
    }

    private boolean roomToMoveRight(View lastView) {
        int endX = (int) lastView.getX() +lastView.getWidth();
        int remainingSpace = screenUtils.getWidthPx() - endX;
        if (remainingSpace > moveRightPixels) {
            return true;
        } else {
            return false;
        }
    }



    private void moveViewsRight(ViewGroup parent, int idxStart, int idxEnd) {
        moveViewsHorizontally(parent, idxStart, idxEnd, moveRightPixels);
    }

    private void moveViewsLeft(ViewGroup parent, int idxStart, int idxEnd) {
        moveViewsHorizontally(parent, idxStart, idxEnd, moveLeftPixels);
    }

    private void moveViewsHorizontally(ViewGroup parent, int idxStart, int idxEnd, int horizontalOffset) {
        for(int i = idxStart; i < idxEnd; i++) {
            View currView = parent.getChildAt(i);
            currView.animate()
                    .setDuration(animationDuration)
                    .setStartDelay(startDelay)
                    .x(currView.getX() + horizontalOffset)
                    .start();
        }
    }

    private void moveView(View view, int xPosition, int yPosition) {
        view.animate()
                .setDuration(animationDuration)
                .setStartDelay(startDelay)
                .x(xPosition)
                .y(yPosition)
                .start();
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(((Activity) getContext()));
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }



}
