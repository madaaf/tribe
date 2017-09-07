package com.tribe.app.presentation.view.adapter;

import com.tribe.app.presentation.view.widget.header.HomeListViewHeader;

public interface SectionCallback {

    boolean isSection(int position);

    @HomeListViewHeader.HeaderType int getSectionType(int position);
  }