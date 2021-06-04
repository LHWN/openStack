package com.spring.openstack.data;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.openstack.OSFactory;

public class OpenStackAuth {
    private OSClient.OSClientV3 osClient;
    private Token token;
    private String tokenId;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public static OpenStackAuth unscopedAuth(String username, String password, String domain) throws NullPointerException, ClientResponseException, AuthenticationException {
        return new OpenStackAuth(username, password, domain);
    }

    public static OpenStackAuth projectScopedAuth(String username, String password, String domain, String projectName) throws NullPointerException, ClientResponseException, AuthenticationException {

    }

    public OpenStackAuth(String username, String password, String domain) throws NullPointerException, ClientResponseException, AuthenticationException {
        // TODO: endPoint url 부분 추가 수정
        IOSClientBuilder.V3 v3 = OSFactory.builderV3()
                .endpoint(Constants.OPENSTACK_KEYSTONE_URL+Constants.KEYSTONE_API_VERSION);

        if (domain != null && !domain.equals("")) {
            v3.credentials(username, password, Identifier.byName(domain));
        } else {
           v3.credentials(username, password);
        }

        this.osClient = v3.authenticate();
        this.token = this.osClient.getToken();
        this.tokenId = this.token.getId();
    }

    // domain 없을 때
    public OpenStackAuth(String username, String password) {
        this(username, password, null);
    }

    public OpenStackAuth(String tokenId) {
        IOSClientBuilder.V3 v3 = OSFactory.builderV3()
                .endpoint(Constants.OPENSTACK_KEYSTONE_URL+Constants.KEYSTONE_API_VERSION)
                .token(tokenId);

        this.osClient = v3.authenticate();
        this.token = this.osClient.getToken();
        this.tokenId = this.token.getId();
    }

    public OpenStackAuth(OSClient.OSClientV3 osClient) {
        this.
    }
 }
