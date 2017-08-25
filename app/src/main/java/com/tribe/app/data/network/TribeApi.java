package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.BookRoomLinkEntity;
import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.network.entity.LookupFBResult;
import com.tribe.app.data.network.entity.RoomLinkEntity;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HEAD;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;
import rx.Observable;

public interface TribeApi {

  @FormUrlEncoded @POST("/graphql") Observable<UserRealm> getUserInfos(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<List<UserRealm>> getUserListInfos(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Installation> createOrUpdateInstall(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<List<Installation>> getInstallList(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Installation> removeInstall(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> incrUserTimeInCall(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<UserRealm> updateUser(@Field("query") String query);

  @Multipart @POST("/graphql") Observable<UserRealm> updateUserMedia(
      @Part("query") RequestBody query, @Part MultipartBody.Part file);

  @FormUrlEncoded @POST("/graphql") Observable<CreateFriendshipEntity> createFriendship(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<SearchResultRealm> findByUsername(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<UserRealm> lookupByUsername(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> removeFriendship(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> notifyFBFriends(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> removeMembersFromGroup(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> removeGroup(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> leaveGroup(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> bootstrapSupport(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<FriendshipRealm> updateFriendship(
      @Field("query") String query);

  @HEAD Observable<Response<Void>> getHeadDeepLink(@Url String url);

  @FormUrlEncoded @POST("/graphql") @Headers("@: CanBeAnonymous") Observable<Room> room(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") @Headers("@: CanBeAnonymous") Observable<Room> createRoom(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Room> updateRoom(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Boolean> inviteUserToRoom(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Boolean> buzzRoom(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<List<Invite>> invites(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> declineInvite(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<LookupFBResult> lookupFacebook(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<RoomLinkEntity> getRoomLink(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<BookRoomLinkEntity> bookRoomLink(
      @Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Void> roomAcceptRandom(@Field("query") String query);

  @FormUrlEncoded @POST("/graphql") Observable<Boolean> reportUser(@Field("query") String query);
}