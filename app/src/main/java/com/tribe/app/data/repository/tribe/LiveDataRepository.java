package com.tribe.app.data.repository.tribe;

import com.tribe.app.domain.interactor.tribe.LiveRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link LiveDataRepository} for handling live.
 */
@Singleton
public class LiveDataRepository implements LiveRepository {

    /**
     * Constructs a {@link LiveRepository}.
     */
    @Inject
    public LiveDataRepository() {

    }
}
