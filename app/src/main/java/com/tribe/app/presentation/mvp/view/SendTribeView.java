package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Tribe;

public interface SendTribeView extends LoadDataView {

    void setCurrentTribe(Tribe tribe);
}
