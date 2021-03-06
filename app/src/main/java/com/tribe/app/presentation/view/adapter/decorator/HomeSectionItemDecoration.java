package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import timber.log.Timber;

/**
 * https://github.com/paetztm/recycler_view_headers
 */

public class HomeSectionItemDecoration extends BaseSectionItemDecoration {

  private TextViewFont txtLabel;
  private ImageView imgPicto;

  public HomeSectionItemDecoration(int headerHeight, boolean sticky,
      @NonNull SectionCallback sectionCallback, ScreenUtils screenUtils) {
    super(headerHeight, sticky, sectionCallback, screenUtils);
  }

  @Override protected void populateHeader(Context context, int headerType,
      GradientDrawable gradientDrawable) {
    switch (headerType) {
      case HOME_ONGOING:
        txtLabel.setText(R.string.home_section_ongoing);
        imgPicto.setImageResource(R.drawable.picto_header_ongoing);
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.violet_home));
        break;

      case HOME_ONLINE:
        txtLabel.setText(R.string.home_section_online);
        imgPicto.setImageResource(R.drawable.picto_header_online);
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.blue_new));
        break;

      case HOME_RECENT:
        txtLabel.setText(R.string.home_section_recent);
        imgPicto.setImageResource(R.drawable.picto_recent);
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.black_dark_blue));
        break;

      case SEARCH_INVITES_TO_SEND:
        txtLabel.setText(R.string.home_section_invites_to_send);
        txtLabel.setVisibility(View.VISIBLE);
        imgPicto.setImageResource(R.drawable.picto_header_invites_to_send);
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.grey_offline));
        break;

      case SEARCH_SUGGESTED_CONTACTS:
        txtLabel.setText(R.string.home_section_suggested_friends);
        txtLabel.setVisibility(View.VISIBLE);
        imgPicto.setImageResource(R.drawable.picto_header_suggested);
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.grey_offline));
        break;

      case NONE:
        txtLabel.setText("");
        imgPicto.setImageDrawable(null);
        gradientDrawable.setColor(Color.TRANSPARENT);
    }
  }

  @Override protected View inflateHeaderView(RecyclerView parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_home_list_header, parent, false);
    txtLabel = ButterKnife.findById(view, R.id.txtLabel);
    imgPicto = ButterKnife.findById(view, R.id.imgPicto);
    return view;
  }
}