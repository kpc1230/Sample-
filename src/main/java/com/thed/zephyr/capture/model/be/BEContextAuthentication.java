package com.thed.zephyr.capture.model.be;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.nimbusds.jwt.JWTClaimsSet;

public class BEContextAuthentication extends JwtAuthentication {

    private BEAuthToken beAuthToken;

    public BEContextAuthentication(AtlassianHostUser hostUser, JWTClaimsSet claims, BEAuthToken beAuthToken) {
        super(hostUser, claims);
        this.beAuthToken = beAuthToken;
    }

    public BEAuthToken getBeAuthToken() {
        return beAuthToken;
    }

    public void setBeAuthToken(BEAuthToken beAuthToken) {
        this.beAuthToken = beAuthToken;
    }
}
