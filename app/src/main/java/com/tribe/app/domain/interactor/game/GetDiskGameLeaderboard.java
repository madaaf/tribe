package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.data.repository.game.DiskGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/20/2017
 */
public class GetDiskGameLeaderboard extends UseCaseDisk {

  private GameRepository gameRepository;
  private String gameId;
  private boolean friendsOnly = true;
  private int offset = 0;
  private int limit = 0;

  @Inject public GetDiskGameLeaderboard(DiskGameDataRepository gameRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.gameRepository = gameRepository;
  }

  public void setup(String gameId, boolean friendsOnly, int limit, int offset) {
    this.gameId = gameId;
    this.friendsOnly = friendsOnly;
    this.limit = limit;
    this.offset = offset;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getGameLeaderBoard(gameId, friendsOnly, limit, offset);
  }
}
