package com.tribe.app.presentation.internal.di.modules;

import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.domain.interactor.user.DoLoginWithPhoneNumber;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.GetRequestCode;
import com.tribe.app.presentation.internal.di.PerActivity;

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
    UseCase provideDiskGetUserInfos(GetDiskUserInfos getDiskUserInfos) {
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
    UseCase provideCloudSendTribe(SendTribe sendTribeDisk) {
        return sendTribeDisk;
    }

}
