package com.tribe.app.presentation.internal.di.modules;

import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.DiskMarkTribeListAsRead;
import com.tribe.app.domain.interactor.tribe.GetNotSeenDiskTribeList;
import com.tribe.app.domain.interactor.tribe.GetPendingTribeList;
import com.tribe.app.domain.interactor.tribe.GetReceivedDiskTribeList;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.domain.interactor.user.DoLoginWithPhoneNumber;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.GetRequestCode;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.presentation.internal.di.scope.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tiago on 19/05/2016.
 */
@Module
public class UserModule {

    public UserModule() {
    }

    @Provides
    @PerActivity
    UseCase provideGetRequestCodeUseCase(GetRequestCode getRequestCode) {
        return getRequestCode;
    }

    @Provides
    @PerActivity
    UseCase provideDoLoginWithUsernameUseCase(DoLoginWithPhoneNumber doLoginWithPhoneNumber) {
        return doLoginWithPhoneNumber;
    }

    @Provides
    @PerActivity
    @Named("cloudUserInfos")
    UseCase provideCloudGetUserInfos(GetCloudUserInfos getCloudUserInfos) {
        return getCloudUserInfos;
    }

    @Provides
    @PerActivity
    @Named("diskUserInfos")
    UseCaseDisk provideDiskGetUserInfos(GetDiskUserInfos getDiskUserInfos) {
        return getDiskUserInfos;
    }

    @Provides
    @PerActivity
    @Named("diskSaveTribe")
    SaveTribe provideDiskSendTribe(SaveTribe saveTribeDisk) {
        return saveTribeDisk;
    }

    @Provides
    @PerActivity
    @Named("cloudSendTribe")
    SendTribe provideCloudSendTribe(SendTribe sendTribeDisk) {
        return sendTribeDisk;
    }

    @Provides
    @PerActivity
    @Named("diskDeleteTribe")
    DeleteTribe provideDiskDeleteTribe(DeleteTribe deleteTribeDisk) {
        return deleteTribeDisk;
    }

    @Provides
    @PerActivity
    @Named("diskGetNotSeenTribes")
    UseCaseDisk provideDiskGetNotSeenTribes(GetNotSeenDiskTribeList getNotSeenDiskTribeList) {
        return getNotSeenDiskTribeList;
    }

    @Provides
    @PerActivity
    @Named("diskGetReceivedTribes")
    UseCaseDisk provideDiskGetReceivedTribes(GetReceivedDiskTribeList getReceivedDiskTribeList) {
        return getReceivedDiskTribeList;
    }

    @Provides
    @PerActivity
    @Named("sendToken")
    SendToken provideSendToken(SendToken sendToken) {
        return sendToken;
    }

    @Provides
    @PerActivity
    @Named("diskGetPendingTribes")
    UseCaseDisk provideGetPendingTribeList(GetPendingTribeList pendingTribeList) {
        return pendingTribeList;
    }

    @Provides
    @Named("diskMarkTribeListAsRead")
    DiskMarkTribeListAsRead diskMarkTribeListAsRead(DiskMarkTribeListAsRead diskMarkTribeListAsRead) {
        return diskMarkTribeListAsRead;
    }
}
