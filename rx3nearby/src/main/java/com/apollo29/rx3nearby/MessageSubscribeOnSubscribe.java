package com.apollo29.rx3nearby;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

class MessageSubscribeOnSubscribe implements ObservableOnSubscribe<Message> {
    private final Context context;

    public MessageSubscribeOnSubscribe(Context context) {
        this.context = context;
    }

    @Override
    public void subscribe(@NonNull final ObservableEmitter<Message> emitter) throws Throwable {
        final MessageListener listener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(message);
                }
            }
        };
        final ApiClientWrapper.ResultHandler handler = new ApiClientWrapper.ResultHandler() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Status status) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new ApiStatusException(status));
                }
            }
        };
        final ApiClientWrapper apiClient = new ApiClientWrapper(context) {
            @Override
            public void onConnected(Bundle bundle) {
                if (!emitter.isDisposed()) {
                    this.subscribe(listener, handler);
                }
            }
        };

        apiClient.connect();
    }
}
