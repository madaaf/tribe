package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by Mada on 30/05/2017.
 */
public class BookRoomLink extends UseCase {

  private LiveRepository liveRepository;

  private String linkId;

  @Inject public BookRoomLink(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setLinkId(String linkId) {
    this.linkId = linkId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.bookRoomLink(linkId);
  }
}
