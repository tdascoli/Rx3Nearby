package com.apollo29.rx3nearby;

import android.content.Context;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;

public final class RxNearby {
    public static Observable<Message> subscribe(Context context, final Function<Status, Observable<Void>> statusResolver) {
        return Observable
                .create(new MessageSubscribeOnSubscribe(context))
                .retryWhen(buildStatusResolutionHandler(statusResolver));
    }

    public static Observable<PublishResult> publish(Context context, final Observable<Message> messageObservable, Function<Status, Observable<Void>> statusResolver) {
        return messageObservable
                .lift(new MessagePublishOperator(context))
                .retryWhen(buildStatusResolutionHandler(statusResolver));
    }

    private static Function<Observable<? extends Throwable>, Observable<?>> buildStatusResolutionHandler(final Function<Status, Observable<Void>> statusResolver) {
        return new Function<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<Void> apply(Observable<? extends Throwable> observable) {
                return observable
                        .flatMap(new Function<Throwable, Observable<Void>>() {
                            @Override
                            public Observable<Void> apply(Throwable throwable) throws Throwable {
                                if (throwable instanceof ApiStatusException && ((ApiStatusException) throwable).getStatus().hasResolution()) {
                                    return statusResolver.apply(((ApiStatusException) throwable).getStatus());
                                } else {
                                    return Observable.error(throwable);
                                }
                            }
                        });
            }
        };
    }
}
