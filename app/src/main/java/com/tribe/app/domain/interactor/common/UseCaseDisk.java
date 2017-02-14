/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tribe.app.domain.interactor.common;

import com.tribe.app.domain.executor.PostExecutionThread;
import java.io.Serializable;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This interfaces represents a execution unit for different use cases (this means any use case
 * in the application should implement this contract).
 * <p>
 * By convention each UseCase implementation will return the result using a {@link Subscriber}
 * that will execute its job in a background thread and will post the result in the UI thread.
 */
public abstract class UseCaseDisk implements Serializable {

  private final PostExecutionThread postExecutionThread;

  private Subscription subscription = Subscriptions.empty();

  protected UseCaseDisk(PostExecutionThread postExecutionThread) {
    this.postExecutionThread = postExecutionThread;
  }

  /**
   * Builds an {@link Observable} which will be used when executing the current {@link
   * UseCaseDisk}.
   */
  protected abstract Observable buildUseCaseObservable();

  /**
   * Executes the current use case.
   *
   * @param UseCaseSubscriber The guy who will be listen to the observable build
   * with {@link #buildUseCaseObservable()}.
   */
  @SuppressWarnings("unchecked") public void execute(Subscriber UseCaseSubscriber) {
    this.subscription = this.buildUseCaseObservable()
        .subscribeOn(postExecutionThread.getScheduler())
        .observeOn(postExecutionThread.getScheduler())
        .subscribe(UseCaseSubscriber);
  }

  /**
   * Unsubscribes from current {@link Subscription}.
   */
  public void unsubscribe() {
    if (!subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }
}
