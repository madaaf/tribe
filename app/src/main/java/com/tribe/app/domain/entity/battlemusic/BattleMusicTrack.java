package com.tribe.app.domain.entity.battlemusic;

import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 18/01/2018.
 */

public class BattleMusicTrack {

  private static final String ID_KEY = "id";
  private static final String NAME_KEY = "name";
  private static final String ARTIST_KEY = "artist";
  private static final String URL_KEY = "url";
  private static final String IMAGE_KEY = "image";
  private static final String ALTERNATIVE_NAMES_KEY = "alternativeNames";

  private String id;
  private String name;
  private String artist;
  private String url;
  private String image;
  private List<String> alternativeNames;

  public BattleMusicTrack() {

  }

  public BattleMusicTrack(JSONObject json) {
    try {
      this.id = json.getString(ID_KEY);
      this.name = json.getString(NAME_KEY);
      this.artist = json.getString(ARTIST_KEY);
      this.url = json.getString(URL_KEY);
      this.alternativeNames = new ArrayList<>();
      this.image = json.has(IMAGE_KEY) ? json.getString(IMAGE_KEY) : null;

      JSONArray array = json.getJSONArray(ALTERNATIVE_NAMES_KEY);
      for (int i = 0; i < array.length(); i++) {
        this.alternativeNames.add(array.getString(i));
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public void setAlternativeNames(List<String> alternativeNames) {
    this.alternativeNames = alternativeNames;
  }

  public List<String> getAlternativeNames() {
    return alternativeNames;
  }

  public JSONObject asJSON() {
    JSONObject track = new JSONObject();
    JsonUtils.jsonPut(track, ID_KEY, id);
    JsonUtils.jsonPut(track, NAME_KEY, this.name);
    JsonUtils.jsonPut(track, ARTIST_KEY, this.artist);
    JsonUtils.jsonPut(track, URL_KEY, this.url);
    if (!StringUtils.isEmpty(image)) JsonUtils.jsonPut(track, IMAGE_KEY, this.image);
    JSONArray alternativeNamesArray = new JSONArray();
    for (String alternativeName : this.alternativeNames)
      alternativeNamesArray.put(alternativeName);
    JsonUtils.jsonPut(track, ALTERNATIVE_NAMES_KEY, alternativeNamesArray);
    return track;
  }
}
