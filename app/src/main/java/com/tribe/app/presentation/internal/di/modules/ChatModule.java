package com.tribe.app.presentation.internal.di.modules;

import dagger.Module;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Module public class ChatModule {

  public ChatModule() {
  }

/*  @Provides @PerActivity CreateMessage provideCreateMessage(CloudChatDataRepository cloudRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    return new CreateMessage(cloudRepository, threadExecutor, postExecutionThread);
  }

  @Provides @PerActivity UserMessageInfos provideUserMessageInfos(
      CloudChatDataRepository cloudRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    return new UserMessageInfos(cloudRepository, threadExecutor, postExecutionThread);
  }*/
}
