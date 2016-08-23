package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.TribeMessage;

import java.util.List;

public interface TribeView extends SendTribeView {

    void updateNewTribes(List<TribeMessage> tribeList);
}
