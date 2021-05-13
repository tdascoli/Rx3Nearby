package com.apollo29.rx3nearby.sample;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.apollo29.rx3nearby.PublishResult;
import com.apollo29.rx3nearby.RxNearby;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.reactivestreams.Subscription;

import java.io.UnsupportedEncodingException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int REQ_RESOLVE_MESSEAGE_API_ERROR = 1024;
    private BehaviorSubject<Message> sendMessageSubject;
    private PublishSubject<Void> retry = PublishSubject.create();
    private Subscription subSubscription;
    private Disposable pubSubscription;
    private ViewGroup contentView;
    private int messageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final String msg = "Hi! This is #" + (messageID++) + " message.";
                    sendMessageSubject.onNext(new Message(msg.getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Failed to serialize message.", e);
                }
            }
        });

        contentView = findViewById(R.id.content);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Function<Status, Observable<Void>> statusResolver = new Function<Status, Observable<Void>>() {
            @Override
            public Observable<Void> apply(Status status) {
                try {
                    Log.i(TAG, "Trying to resolve an error: " + status);
                    status.startResolutionForResult(MainActivity.this, REQ_RESOLVE_MESSEAGE_API_ERROR);
                    return retry;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Failed to resolve API status", e);
                    throw new RuntimeException(e);
                }
            }
        };

        sendMessageSubject = BehaviorSubject.create();
        pubSubscription = RxNearby
                .publish(this, sendMessageSubject, statusResolver)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PublishResult>() {
                    @Override
                    public void accept(PublishResult publishResult) {
                        try {
                            final TextView textView = new TextView(MainActivity.this);
                            textView.setText("Sent: [" + new String(publishResult.message.getContent(), "UTF-8") + "]");
                            contentView.addView(textView);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(MainActivity.class.getName(), "Failed to unserialize the sent message.", e);
                        }
                    }
                });

        RxNearby.subscribe(this, statusResolver)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Message>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.e(TAG, "Subscribe");
                    }

                    @Override
                    public void onNext(Message message) {
                        try {
                            TextView textView = new TextView(MainActivity.this);
                            textView.setText("Received: [" + new String(message.getContent(), "UTF-8") + "]");
                            contentView.addView(textView);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(MainActivity.class.getName(), "Failed to unserialize the received message.", e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Failed to subscribe: " + e.getMessage(), e);
                    }

                    @Override
                    public void onComplete() {
                        Log.i(MainActivity.class.getName(), "Subscribe completed.");
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        pubSubscription.dispose();
        if (subSubscription != null) {
            subSubscription.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_RESOLVE_MESSEAGE_API_ERROR:
                if (resultCode == RESULT_OK) {
                    // Permission granted or error resolved successfully then we proceed
                    // with publish and subscribe..
                    if (retry != null && !retry.hasComplete()) {
                        retry.onNext(null);
                        retry.onComplete();
                    }
                } else {
                    // This may mean that user had rejected to grant nearby permission.
                    Log.e(TAG, "Failed to resolve error with code: " + resultCode);
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
