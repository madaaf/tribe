package com.tribe.app.domain.interactor.live;

import android.util.Pair;
import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class UpdateRoom extends UseCase {

  private String roomId;
  private List<Pair<String, String>> values;
  private LiveRepository liveRepository;

  @Inject public UpdateRoom(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(String roomId, List<Pair<String, String>> values) {
    this.roomId = roomId;
    this.values = values;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.updateRoom(roomId, values);
  }
}
