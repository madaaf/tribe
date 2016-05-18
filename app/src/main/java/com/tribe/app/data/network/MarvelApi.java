/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.tribe.app.data.network;

import com.tribe.app.data.realm.MarvelCharacterRealm;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface MarvelApi {

    @GET("/v1/public/characters")
    Observable<List<MarvelCharacterRealm>> getCharacters(@Query("offset") int offset);
}
