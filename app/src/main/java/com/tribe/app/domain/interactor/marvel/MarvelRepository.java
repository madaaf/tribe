package com.tribe.app.domain.interactor.marvel;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.MarvelCharacter;

import java.util.List;

import rx.Observable;

public interface MarvelRepository {
    Observable<List<MarvelCharacter>> characters();
}
