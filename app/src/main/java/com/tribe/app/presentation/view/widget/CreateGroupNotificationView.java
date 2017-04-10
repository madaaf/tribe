package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 05/04/2017.
 */

public class CreateGroupNotificationView extends FrameLayout {
  public static final String PREFILLED_GRP_MEMBERS = "PREFILLED_GRP_MEMBERS";
  public static final String CREATE_GRP_DIRECTLY = "CREATE_GRP_DIRECTLY";

  private final static int AVATARS_SIZE = 55;
  private final static int DURATION_ANIMATION = 800;
  private final static int DURATION_OFFSET = 500;

  @Inject Navigator navigator;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.createGrpNotificationViewContainer) FrameLayout container;

  @BindView(R.id.createGrpNotificationView) LinearLayout notificationView;

  @BindView(R.id.txtGrpMembersNames) TextViewFont txtGrpMembersNames;

  @BindView(R.id.imgGrpMembersAvatar) LinearLayout avatarsContainer;

  @BindView(R.id.bgCreateGrpNotificationView) View bgView;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  GestureDetectorCompat gestureScanner;
  List<GroupMember> prefilledGrpMembers = new ArrayList<>();

  public CreateGroupNotificationView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public CreateGroupNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;

    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_create_grp_notification, this, true);

    unbinder = ButterKnife.bind(this);

    container.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return gestureScanner.onTouchEvent(event);
      }
    });
    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());
  }

  @OnClick(R.id.btnAction1) void onClickAction1() {
    navigator.navigateToPrefilledCreationGroup((Activity) getContext(), prefilledGrpMembers, false);
    hideView();
  }

  @OnClick(R.id.btnAction2) void onClickAction2() {
    navigator.navigateToPrefilledCreationGroup((Activity) getContext(), prefilledGrpMembers, true);
    hideView();
  }

  public void displayView(ArrayList<TribeGuest> members) {
    prefilledGrpMembers.clear();
    drawAvatarsAndNamesMembers(members);
    prefilledGrpMembers = getUserList(members);
    bgView.animate().setDuration(DURATION_ANIMATION).alpha(1f).start();
    setVisibility(VISIBLE);
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.notif_slide_in_from_top);
    slideInAnimation.setStartOffset(DURATION_OFFSET);
    slideInAnimation.setDuration(DURATION_ANIMATION);
    notificationView.startAnimation(slideInAnimation);
  }

  private void hideView() {
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_to_down);
    setAnimation(slideInAnimation);
    slideInAnimation.setFillAfter(false);
    slideInAnimation.setDuration(DURATION_ANIMATION);
    slideInAnimation.setAnimationListener(new AnimationListenerAdapter() {

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        clearAnimation();
        bgView.animate().setDuration(DURATION_ANIMATION).alpha(0f).start();
        setVisibility(GONE);
      }
    });
    notificationView.startAnimation(slideInAnimation);
  }

  private void drawAvatarsAndNamesMembers(List<TribeGuest> members) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    LinearLayout layout = new LinearLayout(getContext());
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setGravity(Gravity.CENTER);
    layout.setLayoutParams(params);

    String txtNames = "";
    for (int i = 0; i < members.size(); i++) {

      TribeGuest groupMember = members.get(i);
      txtNames += groupMember.getDisplayName();
      if (!((members.size() - 1) == i)) {
        txtNames += ", ";
      }
      AvatarView avatarView = new AvatarView(getContext());
      avatarView.setBackgroundResource(R.drawable.shape_circle_white);
      int padding = getResources().getDimensionPixelSize(R.dimen.margin_horizonta_avatrs);
      avatarView.setPadding(padding, padding, padding, padding);
      avatarView.load(groupMember.getPicture());
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

      layoutParams.width = screenUtils.dpToPx(AVATARS_SIZE);
      layoutParams.height = screenUtils.dpToPx(AVATARS_SIZE);
      if (i != 0) {
        layoutParams.setMargins(screenUtils.dpToPx(-AVATARS_SIZE / 3), 0, 0, 0);
      }
      avatarView.setLayoutParams(layoutParams);
      layout.addView(avatarView);
    }
    avatarsContainer.addView(layout);
    txtGrpMembersNames.setText(txtNames);
  }

  private List<GroupMember> getUserList(ArrayList<TribeGuest> tribeGuests) {
    List<GroupMember> userList = new ArrayList<>();

    if (tribeGuests == null) return userList;

    for (TribeGuest guest : tribeGuests) {
      User u = new User(guest.getId());
      u.setProfilePicture(guest.getPicture());
      GroupMember groupMember = new GroupMember(u);
      groupMember.setMember(true);
      groupMember.setFriend(true);
      userList.add(groupMember);
    }

    return userList;
  }

  ///////////////////
  //  LIFE CYCLE   //
  ///////////////////
  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    clearAnimation();
    super.onDetachedFromWindow();
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  ///////////////////
  //  GESTURE  IMP //
  ///////////////////

  private class TapGestureListener implements GestureDetector.OnGestureListener {

    @Override public boolean onDown(MotionEvent e) {
      hideView();
      return true;
    }

    @Override public void onShowPress(MotionEvent e) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }
}
