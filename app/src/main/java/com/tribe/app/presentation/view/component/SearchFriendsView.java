package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

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

    private final String TAG = "SearchFriendsDebug";

    public SearchFriendsView(Context context) {
        super(context);
    }

    public SearchFriendsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchFriendsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> editTextSearchClicked = PublishSubject.create();
    private PublishSubject<String> editTextSearchTextChanged = PublishSubject.create();

    public Observable<Void> editTextSearchClicked() {
        return editTextSearchClicked;
    }
    public Observable<String> editTextSearchTextChanged() {
        return editTextSearchTextChanged;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initDependencyInjector();
        LayoutInflater.from(getContext()).inflate(R.layout.view_search_friends, this);
        unbinder = ButterKnife.bind(this);

        initAnimationConstants();
        initViewManagementConstants();

        subscriptions.add(RxView.focusChanges(editTextSearch).subscribe(aBoolean -> {
            if (aBoolean) editTextSearchClicked.onNext(null);
        }));

        subscriptions.add(RxView.clicks(editTextSearch).subscribe(aVoid -> {
            editTextSearchClicked.onNext(null);
        }));
        subscriptions.add(RxTextView.textChanges(editTextSearch).subscribe(text -> {
            editTextSearchTextChanged.onNext(text.toString());
        }));

    }

    private void initAnimationConstants() {
        animationDuration = AnimationUtils.ANIMATION_DURATION_SHORT;
        startDelay = AnimationUtils.NO_START_DELAY;
        imageMargin = getResources().getDimensionPixelOffset(R.dimen.vertical_margin_smaller);
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

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
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
            if (!columnCountSet) {
                columnCount = size;
                columnCountSet = true;
            }
            // column count needs to be set before below is called
            forwardFullRow(currentRow);
            // TODO: change in future
//            searchMoved = false;
            // move text right
            currColumn = 1;
            moveSearch(moveRightPixels*(currColumn));
            searchMoved = false;
            currColumn++;
            currentRow++;
        }
        size++;
        addImageToFrontOfLayoutWithAnimation(pictureUrl);
        Log.d(TAG, "currColumn: " + currColumn);
        Log.d(TAG, "currRow: " + currentRow);
    }

    public void deleteFriend(String friendId) {
        int removeLoc = updateRemoveLocationMap(friendId);
        size--;
        currColumn--;
        Log.d(TAG, "removeLoc: " + removeLoc);
        Log.d(TAG, "currColumn: " + currColumn);
        fadeImageAndRemoveFromLayout(removeLoc);
        if (currentRow > 1) {
            if (searchMoved && roomToMoveRight(editTextSearch, layoutUserPicContainer.getChildAt(size - 1))) {
                searchMoved = false;
                collapseViewAndMoveSearch();
            } else if (!searchMoved) {
                moveSearch(moveRightPixels * (currColumn - 1));
            }
            backFullRow(currentRow, removeLoc);
            if (currColumn < 2) {
                currentRow--;
                currColumn = columnCount + 1;
                searchMoved = true;
            }
        } else if (searchMoved) {
            moveImagesLeft(removeLoc);
            if (roomToMoveRight(editTextSearch, layoutUserPicContainer.getChildAt(size - 1))) {
                searchMoved = false;
                collapseViewAndMoveSearch();
            }
        } else {
            if (currColumn > 1) {
                moveImagesAndSearchLeft(removeLoc);
            } else {
                moveSearch(moveRightPixels * (currColumn - 1));
            }
        }
        Log.d(TAG, "currColumn: " + currColumn);
        Log.d(TAG, "currRow: " + currentRow);

    }

    private int updateRemoveLocationMap(String friendId) {
        int removeLoc = userPicChildLocation.remove(friendId);
        for(Map.Entry<String, Integer> entry : userPicChildLocation.entrySet()) {
            if (entry.getValue() > removeLoc) {
                userPicChildLocation.put(entry.getKey(), entry.getValue() - 1);
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
        moveSearch(moveRightPixels * currColumn);
    }

    private void moveImagesAndSearchLeft(int removeLoc) {
        moveImagesLeft(removeLoc);
        moveSearch(moveRightPixels * (currColumn - 1));
    }


    private void moveImagesRight() {
        if (columnCountSet) {
            moveViewsRight(layoutUserPicContainer, (currentRow-1) * (columnCount), (currentRow-1) * (columnCount) + currColumn-1);
        } else {
            moveViewsRight(layoutUserPicContainer, 0, currColumn*currentRow-1);
        }
    }

    private void moveImagesLeft(int removeLoc) {
//        if (columnCountSet) {
//            moveViewsLeft(layoutUserPicContainer, removeLoc, (currentRow-1) * (columnCount +1) + currColumn);
//        } else {
            if (layoutUserPicContainer.getChildAt(removeLoc) != null) {
                    int moveLeftEnd = currColumn * currentRow - 1;
                    moveViewsLeft(layoutUserPicContainer, removeLoc, moveLeftEnd);
                    Log.d(TAG, "remove loc -> " + removeLoc + " moveLeftEnd ->" + moveLeftEnd);
            }
//        }
    }

    private void moveSearch(int pixels) {
        moveView(editTextSearch,(int) pixels, (int) editTextSearch.getY());
    }

    private void forwardFullRow(int rows) {
        for (int i = 1; i <= rows; i++) {
            int idxMoveRightStart  =  (i - 1) * (columnCount - 1) + i - 1;
            int idxMoveRightEnd = i * (columnCount - 1)  + i - 1;
            int idxMoveDown = i * (columnCount - 1)  + i - 1;
            moveViewsRight(layoutUserPicContainer, idxMoveRightStart, idxMoveRightEnd);
            moveView(layoutUserPicContainer.getChildAt(idxMoveDown), imageMargin, i * moveRightPixels);
        }
    }


    private void backFullRow(int rows, int removeLoc) {
        for (int i = rows; i >= 1; i--) {
            int idxMoveUp = (i - 1) * columnCount - 1;
            int idxStart = idxMoveUp + 1;
            if (i == 1) idxStart = idxMoveUp;
            int idxEnd = i * columnCount - 1;
            if (idxStart > size - 1) idxStart = size - 1;
            if (idxEnd > size - 1) idxEnd = size;
            if (idxStart < removeLoc) {
                idxStart = removeLoc;
                moveViewsLeft(layoutUserPicContainer, idxStart, idxEnd);
                break;
            }
            if (i == 1) {
                moveViewsLeft(layoutUserPicContainer, idxStart, idxEnd);
            } else {
                if (idxStart <= idxEnd) moveViewsLeft(layoutUserPicContainer, idxStart, idxEnd);
                if (idxMoveUp < removeLoc) {
                    idxMoveUp = removeLoc;
                    moveView(layoutUserPicContainer.getChildAt(idxMoveUp), imageMargin, (i - 2) * imageSize);
                    break;
                }
                int y = (i - 2) * moveRightPixels;
                if (y == 0) y = imageMargin;
                moveView(layoutUserPicContainer.getChildAt(idxMoveUp), screenUtils.getWidthPx()- moveRightPixels +imageMargin , y);
            }
        }
    }

    private void addImageToFrontOfLayoutWithAnimation(String imageUrl) {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageViewLp = new LinearLayout.LayoutParams(imageSize, imageSize);
        imageView.setLayoutParams(imageViewLp);
        layoutUserPicContainer.addView(imageView, 0);
        FrameLayout.LayoutParams imageViewFlp = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        imageViewFlp.leftMargin = imageMargin;
        imageViewFlp.topMargin = imageMargin;
        imageView.setLayoutParams(imageViewFlp);
        if (imageUrl.equals(getContext().getString(R.string.no_profile_picture_url))) {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_placeholder_avatar));
        } else {
            Glide.with(getContext()).load(imageUrl)
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(imageView);
        }
        imageView.setAlpha(AnimationUtils.ALPHA_NONE);
        imageView.animate()
                .setDuration(animationDuration)
                .setStartDelay(startDelay)
                .alpha(AnimationUtils.ALPHA_FULL)
                .start();
    }
    private void fadeImageAndRemoveFromLayout(int loc) {
        View view = layoutUserPicContainer.getChildAt(loc);
        layoutUserPicContainer.removeViewAt(loc);
        layoutParent.addView(view, 0);
        view.animate()
                .setDuration(animationDuration)
                .setStartDelay(startDelay)
                .alpha(AnimationUtils.ALPHA_NONE)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
//                        layoutParent.removeViewAt(0);
                    }
                })
                .start();

    }

    private void expandViewAndMoveSearch() {
        AnimationUtils.animateHeightFrameLayout(layoutParent, layoutParent.getHeight(), layoutParent.getHeight() + moveRightPixels, animationDuration);
        AnimationUtils.animateHeightFrameLayout(layoutUserPicContainer, layoutUserPicContainer.getHeight(), layoutUserPicContainer.getHeight() + moveRightPixels, animationDuration);
        moveView(editTextSearch, imageMargin, moveRightPixels *currentRow);
    }

    private void collapseViewAndMoveSearch() {
        AnimationUtils.animateHeightFrameLayout(layoutParent, layoutParent.getHeight(), layoutParent.getHeight() - moveRightPixels, animationDuration);
        AnimationUtils.animateHeightFrameLayout(layoutUserPicContainer, layoutUserPicContainer.getHeight(), layoutUserPicContainer.getHeight() - moveRightPixels, animationDuration);
        moveView(editTextSearch, (currColumn-1) * moveRightPixels + imageMargin, (currentRow - 1) * moveRightPixels);
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

    private boolean roomToMoveRight(View view, View lastView) {
        int endX = (int) lastView.getX() +lastView.getWidth();
        int remainingSpace = screenUtils.getWidthPx() - endX;
        if (remainingSpace > view.getWidth()) {
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
        if (xPosition == 0) xPosition = imageMargin;
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
