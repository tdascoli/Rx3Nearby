package com.apollo29.rx3nearby;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;

class MessagePublishOperator implements ObservableOperator<PublishResult, Message> {
    private final Context context;

    public MessagePublishOperator(Context context) {
        this.context = context;
    }

    @Override
    public @NonNull Observer<? super Message> apply(@NonNull Observer<? super PublishResult> observer) throws Throwable {
        final ApiClientWrapper apiClient = new ApiClientWrapper(this.context);
        MessagePublishSubscriber parent = new MessagePublishSubscriber(observer, apiClient);
        return parent;
    }

    private class MessagePublishSubscriber implements Observer<Message> {
        private final Observer<? super PublishResult> observer;
        private final ApiClientWrapper apiClient;

        public MessagePublishSubscriber(Observer<? super PublishResult> observer, final ApiClientWrapper apiClient) {
            this.apiClient = apiClient;
            this.observer = observer;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(final Message message) {
            if (apiClient.isConnected()) {
                publish(message);
            } else {
                apiClient.setOnConnectedListener(new ApiClientWrapper.ConnectionListener() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        publish(message);
                    }

                    @Override
                    public void onConnectionSuspended(int causeCode) {
                        Exceptions.throwIfFatal(new ApiConnectionSuspendedException(causeCode));
                    }

                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Exceptions.throwIfFatal(new ApiConnectionFailedException(connectionResult));
                    }
                });
                apiClient.connect();
            }
        }

        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public void onComplete() {
            observer.onComplete();
        }

        private void publish(final Message message) {
            apiClient.publish(message, new ApiClientWrapper.ResultHandler() {
                @Override
                public void onSuccess() {
                    observer.onNext(new PublishResult(message));
                }

                @Override
                public void onError(Status status) {
                    Exceptions.throwIfFatal(new ApiStatusException(status));
                }
            });
        }
    }
}
