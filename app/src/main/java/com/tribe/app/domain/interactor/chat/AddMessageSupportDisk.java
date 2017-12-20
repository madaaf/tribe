package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class AddMessageSupportDisk extends UseCaseDisk {

  private Message message;
  private ChatRepository chatRepository;

  @Inject public AddMessageSupportDisk(DiskChatDataRepository chatRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setup(Message message) {
    this.message = message;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.addMessageSupportDisk(message);
  }
}
