package com.tribe.app.presentation.view.adapter;

import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;

public interface SectionCallback {

  boolean isSection(int position);

  @BaseSectionItemDecoration.HeaderType int getSectionType(int position);
}