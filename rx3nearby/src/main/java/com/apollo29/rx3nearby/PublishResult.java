package com.apollo29.rx3nearby;

import com.google.android.gms.nearby.messages.Message;

public class PublishResult {
    public final Message message;

    public PublishResult(Message message) {
        this.message = message;
    }
}
