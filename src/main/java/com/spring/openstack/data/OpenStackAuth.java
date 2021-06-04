package com.spring.openstack.data;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.openstack.OSFactory;

@Slf4j
@NoArgsConstructor
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

    public OSClient.OSClientV3 getOsClient() {
        return osClient;
    }

    public void setOsClient(OSClient.OSClientV3 osClient) {
        this.osClient = osClient;
    }

    public static OpenStackAuth unscopedAuth(String username, String password, String domain) throws NullPointerException, ClientResponseException, AuthenticationException {
        return new OpenStackAuth(username, password, domain);
    }

    // 프로젝트 스콥이어야 프로젝트 내에서 구현 정보 뿐만 아니라  생성과 같은 액션을 할 수이;ㅆ음
    public static OpenStackAuth projectScopedAuth(String username, String password, String domain, String projectName) throws NullPointerException, ClientResponseException, AuthenticationException {
        return new OpenStackAuth(OSFactory.builderV3().endpoint(Constants.OPENSTACK_KEYSTONE_URL+Constants.KEYSTONE_API_VERSION).credentials(username, password, Identifier.byName(domain)).scopeToProject(Identifier.byName(projectName), Identifier.byName(domain)).authenticate());
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
        this.setOsClient(osClient);
    }

    public boolean validateToken(String tokenId) {
        ActionResponse validateToken = null;
        boolean returnBoolean = false;

        try {
            validateToken = osClient.identity().tokens().check(tokenId);
            returnBoolean = validateToken.isSuccess();
        } catch (NullPointerException nullPointerException) {

        }

        return returnBoolean;
    }

    public Token getTokenDetails(String tokenId) {
        Token tokenDetail = null;
        try {
            tokenDetail = osClient.identity().tokens().get(tokenId);
        } catch (NullPointerException nullPointerException) {
            log.error("OpenStack instance is null. Please check out authentication.");
        }

        return tokenDetail;
    }
 }
