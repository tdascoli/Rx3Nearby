package com.apollo29.rx3nearby;

import com.google.android.gms.common.api.Status;

public class ApiStatusException extends Exception {
    private Status status;

    public ApiStatusException(Status status) {
        super("Google API Error: " + status);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
