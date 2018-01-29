package com.tribe.app.presentation.view.component.live.game.birdrush;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.model.TribeGuest;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRush {

  private ScreenUtils screenUtils;

  // VARIABLES
  private String currentUserId;
  private int index;
  private boolean lost = false;
  private TribeGuest tribeGuest;
  private String id;
  private int x;
  private int y;

  private String[] birdsColors = new String[] {
      "FBCF26", "FA7FD9", "BE9EFF", "42F4B2", "F85C02", "3DE9DA", "8B572A", "FFFFFF"
  };

  private Integer[] birdsImage = new Integer[] {
      R.drawable.game_bird1, R.drawable.game_bird2, R.drawable.game_bird3, R.drawable.game_bird4,
      R.drawable.game_bird5, R.drawable.game_bird6, R.drawable.game_bird7, R.drawable.game_bird8
  };

  private Integer[] birdsBackImage = new Integer[] {
      R.drawable.game_bird_bck, R.drawable.game_bird_bck_2, R.drawable.game_bird_bck_3,
      R.drawable.game_bird_bck_4, R.drawable.game_bird_bck_5, R.drawable.game_bird_bck_6,
      R.drawable.game_bird7, R.drawable.game_bird8,
  };

  public BirdRush(int index, TribeGuest guest, ScreenUtils screenUtils, String currentUserId) {
    this.index = index;
    this.tribeGuest = guest;
    this.y = screenUtils.getHeightPx() / 2;
    this.currentUserId = currentUserId;
    //initView();
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public boolean isMine() {
    return tribeGuest.getId().equals(currentUserId);
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean isLost() {
    return lost;
  }

  /*
  private void initView() {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_bird_item, this, true);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    bird.setImageResource(birdsImage[index]);
    birdName.setBackground(ContextCompat.getDrawable(context, birdsBackImage[index]));

    String name = (tribeGuest != null) ? tribeGuest.getDisplayName() : currentUser.getDisplayName();
    Timber.e("NEW BIRD " + tribeGuest.toString());
    birdName.setText(name);
  }*/

  public String getGuestId() {
    return tribeGuest.getId();
  }

  public String getName() {
    return tribeGuest.getDisplayName();
  }
}
