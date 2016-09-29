package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface TribeApi {

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> getUserInfos(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<UserRealm>> getUserListInfos(@Field("query") String query);

    @Multipart
    @POST("/graphql")
    Observable<TribeRealm> uploadTribe(@Part("query") RequestBody query, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<MessageRealmInterface>> messages(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Installation> createOrUpdateInstall(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<Installation>> getInstallList(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Installation> removeInstall(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<TribeRealm>> markTribeListAsSeen(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<ChatRealm>> markMessageListAsSeen(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<ChatRealm> sendChat(@Field("query") String query);

    @Multipart
    @POST("/graphql")
    Observable<ChatRealm> uploadMessageMedia(@Part("query") RequestBody query, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<ChatRealm>> chatHistory(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<ChatRealm>> messagesById(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> updateUser(@Field("query") String query);

    @Multipart
    @POST("/graphql")
    Observable<UserRealm> updateUserMedia(@Part("query") RequestBody query, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<LookupEntity> lookup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<CreateFriendshipEntity> createFriendship(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<List<Integer>> howManyFriends(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<SearchResultRealm> findByUsername(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> lookupByUsername(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> removeFriendship(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> notifyFBFriends(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<GroupRealm> getGroupMembers(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<GroupRealm> createGroup(@Field("query") String query);

    @Multipart
    @POST("/graphql")
    Observable<GroupRealm> createGroupMedia(@Part("query") RequestBody query, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<GroupRealm> updateGroup(@Field("query") String query);

    @Multipart
    @POST("/graphql")
    Observable<GroupRealm> updateGroupMedia(@Part("query") RequestBody query, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> addMembersToGroup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> removeMembersFromGroup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> addAdminsToGroup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> removeAdminsFromGroup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> removeGroup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> leaveGroup(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<MembershipRealm> createMembership(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> updateScore(@Field("query") String query);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<Void> markTribeAsSave(@Field("query") String query);
}