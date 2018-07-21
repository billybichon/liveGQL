package com.github.billybichon.livegql;


/**
 * Created by billy on 16/07/2017.
 */
class PayloadClient {
    public Object data;

    public ErrorClient[] errors;

    public String message;

    PayloadClient(Object data, ErrorClient[] errors, String message) {
        this.data = data;
        this.errors = errors;
        this.message = message;
    }
}
