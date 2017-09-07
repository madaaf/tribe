package com.tribe.app.presentation.internal.di.modules;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.user.AddMembersToGroup;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.CreateFriendships;
import com.tribe.app.domain.interactor.user.CreateMembership;
import com.tribe.app.domain.interactor.user.DiskFindContactByValue;
import com.tribe.app.domain.interactor.user.DiskSearchResults;
import com.tribe.app.domain.interactor.user.DoLoginWithPhoneNumber;
import com.tribe.app.domain.interactor.user.DoRegister;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.domain.interactor.user.GetBlockedFriendshipList;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetDiskContactList;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.domain.interactor.user.GetDiskFBContactList;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.GetGroupMembers;
import com.tribe.app.domain.interactor.user.GetHeadDeepLink;
import com.tribe.app.domain.interactor.user.GetRequestCode;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.NotifyFBFriends;
import com.tribe.app.domain.interactor.user.RemoveFriendship;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.domain.interactor.user.UpdateGroup;
import com.tribe.app.domain.interactor.user.UpdateMembership;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UserMessageInfos;
import com.tribe.app.presentation.internal.di.scope.PerActivity;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

/**
 * Created by tiago on 19/05/2016.
 */
@Module public class UserModule {

  public UserModule() {
  }

  @Provides @PerActivity UseCase provideGetRequestCodeUseCase(GetRequestCode getRequestCode) {
    return getRequestCode;
  }

  @Provides @PerActivity UseCase provideDoLoginWithUsernameUseCase(
      DoLoginWithPhoneNumber doLoginWithPhoneNumber) {
    return doLoginWithPhoneNumber;
  }

  @Provides @PerActivity DoRegister provideDoRegister(CloudUserDataRepository userRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    return new DoRegister(userRepository, threadExecutor, postExecutionThread);
  }

  @Provides @PerActivity UseCase provideUpdateUser(UpdateUser updateUser) {
    return updateUser;
  }

  @Provides @PerActivity UseCase provideGetGroupMembers(GetGroupMembers getGroupMembers) {
    return getGroupMembers;
  }

  @Provides @PerActivity UseCase provideUpdateGroup(UpdateGroup updateGroup) {
    return updateGroup;
  }

  @Provides @PerActivity UseCase provideUpdateMembership(UpdateMembership updateMembership) {
    return updateMembership;
  }

  @Provides @PerActivity UseCase provideAddMembersToGroup(AddMembersToGroup addMembersToGroup) {
    return addMembersToGroup;
  }

  @Provides @PerActivity @Named("cloudUserInfos") UseCase provideCloudGetUserInfos(
      GetCloudUserInfos getCloudUserInfos) {
    return getCloudUserInfos;
  }

  @Provides @PerActivity @Named("cloudUserMessageInfos") UseCase provideUserMessageInfos(
      UserMessageInfos userMessageInfos) {
    return userMessageInfos;
  }

  @Provides @PerActivity @Named("diskUserInfos") GetDiskUserInfos provideDiskGetUserInfos(
      GetDiskUserInfos getDiskUserInfos) {
    return getDiskUserInfos;
  }

  @Provides @PerActivity @Named("sendToken") SendToken provideSendToken(SendToken sendToken) {
    return sendToken;
  }

  @Provides @PerActivity UseCase providesRemoveInstall(RemoveInstall removeInstall) {
    return removeInstall;
  }

  @Provides @PerActivity @Named("synchroContactList") UseCase provideSynchroContactList(
      SynchroContactList synchroContactList) {
    return synchroContactList;
  }

  @Provides @PerActivity @Named("diskContactList") UseCaseDisk provideGetContactList(
      GetDiskContactList getDiskContactList) {
    return getDiskContactList;
  }

  @Provides @PerActivity @Named("diskContactOnAppList") UseCaseDisk provideGetContactOnAppList(
      GetDiskContactOnAppList getDiskContactOnAppList) {
    return getDiskContactOnAppList;
  }

  @Provides @PerActivity @Named("diskFBContactList") UseCaseDisk provideGetFBContactList(
      GetDiskFBContactList getDiskFBContactList) {
    return getDiskFBContactList;
  }

  @Provides @PerActivity @Named("cloudFindByUsername") FindByUsername provideFindByUsername(
      FindByUsername findByUsername) {
    return findByUsername;
  }

  @Provides @PerActivity @Named("diskSearchResults") DiskSearchResults provideDiskSearchResults(
      DiskSearchResults diskSearchResults) {
    return diskSearchResults;
  }

  @Provides @PerActivity @Named("diskFindContactByValue")
  DiskFindContactByValue provideDiskFindContactByValue(
      DiskFindContactByValue diskFindContactByValue) {
    return diskFindContactByValue;
  }

  @Provides @PerActivity @Named("removeFriendship") RemoveFriendship provideRemoveFriendship(
      RemoveFriendship removeFriendship) {
    return removeFriendship;
  }

  @Provides @PerActivity CreateFriendship provideCreateFriendship(
      CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    return new CreateFriendship(userRepository, threadExecutor, postExecutionThread);
  }

  @Provides @PerActivity CreateFriendships provideCreateFriendships(
      CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    return new CreateFriendships(userRepository, threadExecutor, postExecutionThread);
  }

  @Provides @PerActivity @Named("notifyFBFriends") UseCase provideNotifyFBFriends(
      NotifyFBFriends notifyFBFriends) {
    return notifyFBFriends;
  }

  @Provides @PerActivity @Named("lookupByUsername") LookupUsername provideLookupUsername(
      LookupUsername lookupUsername) {
    return lookupUsername;
  }

  @Provides @PerActivity GetBlockedFriendshipList provideGetBlockedFriendshipList(
      DiskUserDataRepository diskUserDataRepository, PostExecutionThread postExecutionThread) {
    return new GetBlockedFriendshipList(diskUserDataRepository, postExecutionThread);
  }

  @Provides @PerActivity GetHeadDeepLink provideGetHeadDeepLink(
      CloudUserDataRepository cloudUserDataRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    return new GetHeadDeepLink(cloudUserDataRepository, threadExecutor, postExecutionThread);
  }

  @Provides @PerActivity CreateMembership provideCreateMembership(
      CloudUserDataRepository cloudUserDataRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    return new CreateMembership(cloudUserDataRepository, threadExecutor, postExecutionThread);
  }
}
