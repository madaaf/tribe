package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
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

/**
 * https://github.com/paetztm/recycler_view_headers
 */

public class SearchSectionItemDecoration extends BaseSectionItemDecoration {

  private TextViewFont txtLabel;
  private ImageView imgPicto;
  private ImageView imgEmpty;

  public SearchSectionItemDecoration(int headerHeight, boolean sticky,
      @NonNull SectionCallback sectionCallback, ScreenUtils screenUtils) {
    super(headerHeight, sticky, sectionCallback, screenUtils);
  }

  @Override protected void populateHeader(Context context, int headerType,
      GradientDrawable gradientDrawable) {

    gradientDrawable.setColor(ContextCompat.getColor(context, R.color.grey_offline));

    switch (headerType) {
      case SEARCH_RESULTS:
        txtLabel.setText(R.string.home_section_results);
        txtLabel.setVisibility(View.VISIBLE);
        imgPicto.setImageResource(R.drawable.picto_header_results);
        break;

      case SEARCH_SUGGESTED_CONTACTS:
        txtLabel.setText(R.string.home_section_suggested_friends);
        txtLabel.setVisibility(View.VISIBLE);
        imgPicto.setImageResource(R.drawable.picto_header_online);
        break;

      case SEARCH_INVITES_TO_SEND:
        txtLabel.setText(R.string.home_section_invites_to_send);
        txtLabel.setVisibility(View.VISIBLE);
        imgPicto.setImageResource(R.drawable.picto_recent);
        break;

      case SEARCH_EMPTY:
        imgEmpty.setVisibility(View.VISIBLE);
        txtLabel.setVisibility(View.GONE);
        imgPicto.setImageResource(R.drawable.picto_empty_header);
        break;
    }
  }

  @Override protected View inflateHeaderView(RecyclerView parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_search_list_header, parent, false);
    txtLabel = ButterKnife.findById(view, R.id.txtLabel);
    imgPicto = ButterKnife.findById(view, R.id.imgPicto);
    imgEmpty = ButterKnife.findById(view, R.id.imgEmpty);
    return view;
  }
}