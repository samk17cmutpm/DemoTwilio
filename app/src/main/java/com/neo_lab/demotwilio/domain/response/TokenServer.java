package com.neo_lab.demotwilio.domain.response;

import com.google.gson.annotations.SerializedName;
import com.neo_lab.demotwilio.model.Token;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public class TokenServer implements Token {

    @SerializedName("identity")
    private String identity;

    @SerializedName("token")
    private String token;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return this.token;
    }
}
