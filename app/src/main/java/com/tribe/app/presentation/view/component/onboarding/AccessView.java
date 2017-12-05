package com.tribe.app.presentation.view.component.onboarding;

/**
 * AccessView.java
 * Created by tiago on 12/14/16
 */
public class AccessView {
/*
  private final static int DURATION = 300;
  private final static int DURATION_SHORT = 100;
  private final static int DURATION_MEDIUM = 400;
  private final static int PULSATING_DURATION = 1200;

  @IntDef({ NONE, LOADING, DONE }) public @interface StatusType {
  }

  public static final int NONE = 0;
  public static final int LOADING = 1;
  public static final int DONE = 2;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutPulse) ViewGroup layoutPulse;

  @BindView(R.id.viewPulse) View viewPulse;

  @BindView(R.id.progressBar) CircularProgressBar progressBar;

  @BindView(R.id.imgCircle) ImageView imgCircle;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.txtNumFriends) TextSwitcher txtNumFriends;

  @BindView(R.id.layoutFriends) ViewGroup layoutFriends;

  @BindView(R.id.txtStatus) TextViewFont txtStatus;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  // VARIABLES
  private @StatusType int status;
  private boolean isEnd = true;
  private int nbFriends = 0;

  // RESOURCES
  private int totalTimeSynchro;

  public AccessView(Context context) {
    super(context);
  }

  public AccessView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_access_friends, this);
    unbinder = ButterKnife.bind(this);

    totalTimeSynchro = getContext().getAlts().getInteger(R.integer.time_synchro);

    initDependencyInjector();
    init();
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
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
        .build()
        .inject(this);
  }

  private void init() {
    status = NONE;

    hideView(layoutFriends, false);

    progressBar.setAnimationDuration(totalTimeSynchro);
    progressBar.setProgressColor(ContextCompat.getColor(getContext(), R.color.blue_new));
    progressBar.setProgressWidth(screenUtils.dpToPx(5.5f));

    int circleSize = (int) (screenUtils.getWidthPx() * 0.4f);
    int pulseSize = circleSize + screenUtils.dpToPx(40);

    setLayout(imgCircle, circleSize, circleSize);
    setLayout(progressBar, circleSize, circleSize);
    setLayout(layoutFriends, circleSize - screenUtils.dpToPx(20),
        circleSize - screenUtils.dpToPx(20));
    setLayout(viewPulse, pulseSize, pulseSize);

    int layoutPulseSize = pulseSize + screenUtils.dpToPx(60);
    setLayout(layoutPulse, layoutPulseSize, layoutPulseSize);

    expandAndContract();

    subscriptions.add(Observable.interval(PULSATING_DURATION, TimeUnit.MILLISECONDS,
        AndroidSchedulers.mainThread())
        .onBackpressureDrop()
        .subscribe(aVoid -> expandAndContract()));

    imgIcon.setScaleX(80f);
    imgIcon.setScaleY(80f);
    imgIcon.setAlpha(0f);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        int[] location = new int[2];
        imgCircle.getLocationOnScreen(location);

        MarginLayoutParams lp = (MarginLayoutParams) imgIcon.getLayoutParams();
        lp.leftMargin = location[0] + ((circleSize - imgIcon.getMeasuredWidth()) >> 1);
        int heightDiff = screenUtils.getHeightPx() - getMeasuredHeight();
        lp.topMargin =
            (location[1] - heightDiff) + ((circleSize - imgIcon.getMeasuredHeight()) >> 1);
        imgIcon.setLayoutParams(lp);

        imgIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        imgIcon.animate()
            .alpha(1)
            .scaleX(1)
            .scaleY(1)
            .setStartDelay(0)
            .setDuration(800)
            .setInterpolator(new OvershootInterpolator(0.45f))
            .start();
      }
    });
  }

  private void setLayout(View view, int width, int height) {
    ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
    viewLayoutParams.height = height;
    viewLayoutParams.width = width;
    view.setLayoutParams(viewLayoutParams);
  }

  private void removePulsingCircleAnimation() {
    Drawable backgrounds[] = new Drawable[2];
    backgrounds[0] =
        ResourcesCompat.getDrawable(getAlts(), R.drawable.shape_circle_black_3, null);
    backgrounds[1] =
        ResourcesCompat.getDrawable(getAlts(), R.drawable.shadow_circle_white, null);

    TransitionDrawable transitionDrawable = new TransitionDrawable(backgrounds);
    viewPulse.setBackground(transitionDrawable);
    viewPulse.animate().scaleX(0).scaleY(0).setDuration(600).start();
    transitionDrawable.startTransition(PULSATING_DURATION);
  }

  private void expandAndContract() {
    if (isEnd) {
      isEnd = false;
      viewPulse.animate()
          .scaleY(1.1f)
          .scaleX(1.1f)
          .setStartDelay(0)
          .setDuration(PULSATING_DURATION)
          .start();
    } else {
      isEnd = true;
      viewPulse.animate()
          .scaleY(1.3f)
          .scaleX(1.3f)
          .setStartDelay(0)
          .setDuration(PULSATING_DURATION)
          .start();
    }
  }

  public void animateProgress() {
    progressBar.setProgress(100, 0, null, null);
  }

  public @StatusType int getStatus() {
    return status;
  }

  public void showLoading(int nbFriends) {
    this.nbFriends = nbFriends;
    txtNumFriends.setText("" + nbFriends);

    if (status != LOADING) {
      status = LOADING;

      progressBar.clearAnimation();
      progressBar.setProgress(0, 0, null, null);

      txtStatus.setText(R.string.onboarding_queue_loading_description);

      hideView(imgIcon, true);
      layoutFriends.postDelayed(() -> showView(layoutFriends, true), DURATION >> 1);
    }
  }

  public void showCongrats() {
    if (status != DONE) {
      status = DONE;

      txtStatus.setText(R.string.onboarding_queue_valid_description);

      imgIcon.setImageResource(R.drawable.picto_tick_access);

      hideView(layoutFriends, true);
      imgIcon.postDelayed(() -> showView(imgIcon, true), DURATION >> 1);

      subscriptions.clear();
      removePulsingCircleAnimation();
    }
  }

  private void hideView(View view, boolean animate) {
    view.animate()
        .alpha(0)
        .translationY(screenUtils.dpToPx(20))
        .setDuration(animate ? DURATION : 0)
        .setStartDelay(0)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  private void showView(View view, boolean animate) {
    view.animate()
        .alpha(1)
        .translationY(0)
        .setDuration(animate ? DURATION : 0)
        .setStartDelay(0)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }*/
}
