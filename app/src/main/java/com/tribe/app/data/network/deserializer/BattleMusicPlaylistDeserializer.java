package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.battlemusic.BattleMusicTrack;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BattleMusicPlaylistDeserializer
    implements JsonDeserializer<List<BattleMusicPlaylist>> {

  @Override public List<BattleMusicPlaylist> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    List<BattleMusicPlaylist> battleMusicPlaylists = new ArrayList<>();

    if (je.isJsonObject()) {
      JsonArray results = je.getAsJsonObject().getAsJsonArray("playlists");

      if (results != null) {
        for (final JsonElement jsonElement : results) {
          if (!jsonElement.isJsonNull()) {
            JsonObject jo = jsonElement.getAsJsonObject();
            BattleMusicPlaylist musicPlaylist = new BattleMusicPlaylist();
            musicPlaylist.setTitle(jo.get("title").getAsString());
            JsonArray tracksArray = jo.getAsJsonObject("tracks").getAsJsonArray("default");

            if (tracksArray != null) {
              List<BattleMusicTrack> tracks = new ArrayList<>();

              for (final JsonElement trackJson : tracksArray) {
                if (!trackJson.isJsonNull()) {
                  JsonObject trackJsonObject = trackJson.getAsJsonObject();
                  BattleMusicTrack track = new BattleMusicTrack();
                  track.setArtist(trackJsonObject.get("artist").getAsString());
                  track.setId(trackJsonObject.get("id").getAsString());
                  track.setImage(trackJsonObject.get("image").getAsString());
                  track.setName(trackJsonObject.get("name").getAsString());
                  track.setUrl(trackJsonObject.get("url").getAsString());
                  tracks.add(track);
                }
              }

              musicPlaylist.setTracks(tracks);
            }

            battleMusicPlaylists.add(musicPlaylist);
          }
        }
      }
    }

    return battleMusicPlaylists;
  }
}