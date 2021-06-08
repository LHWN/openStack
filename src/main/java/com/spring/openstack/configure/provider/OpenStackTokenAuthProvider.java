package com.spring.openstack.configure.provider;

import com.spring.openstack.data.OpenStackAuth;
import com.spring.openstack.data.UserInformation;
import lombok.extern.slf4j.Slf4j;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.openstack.identity.v3.domain.KeystoneProject;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OpenStackTokenAuthProvider implements AuthenticationProvider {

    private final OSClient.OSClientV3 adminOSClient;

    public OpenStackTokenAuthProvider(OSClient.OSClientV3 adminOSClient) {
        this.adminOSClient = adminOSClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String domain = (String)authentication.getDetails();

        try {
            OpenStackAuth openStackAuth = new OpenStackAuth(username, password, domain);
            log.info("Login succeed: [{}]", username);
            String userId = openStackAuth.getOsClient().getToken().getUser().getId();
            String defaultProjectId = adminOSClient.identity().users().get(userId).getDefaultProjectId();
            String projectId = null;
            String projectName = null;
            String domainId = null;
            String domainName = null;

            List<KeystoneProject> list = (List<KeystoneProject>)adminOSClient.identity().users().listUserProjects(userId);

            if(defaultProjectId == null) {
                projectId = list.get(0).getId();
                projectName = list.get(0).getName();
                domainId = list.get(0).getDomainId();
                domainName = list.get(0).getDomain().getName();
            } else {
                KeystoneProject keystoneProject = list.stream().filter(x -> x.getId().equals(defaultProjectId)).collect(Collectors.toList()).get(0);
                projectId = defaultProjectId;
                projectName = keystoneProject.getName();
                domainId = keystoneProject.getDomainId();
                domainName = keystoneProject.getDomain().getName();
            }
            UserInformation.UserInformationBuilder userInformationBuilder = UserInformation.builder().unscopedToken(openStackAuth.getOsClient().getToken());
            openStackAuth = OpenStackAuth.projectScopedAuth(username, password, domainName, projectName);
            UserInformation userInformation = userInformationBuilder.scopedToken(openStackAuth.getOsClient().getToken())
                    .projectId(projectId)
                    .projectName(projectName)
                    .domain(domain).build();

            Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("member"));
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password, authorities);
            usernamePasswordAuthenticationToken.setDetails(userInformation);

            return usernamePasswordAuthenticationToken;

        } catch (AuthenticationException authenticationException) {

        } catch (ClientResponseException clientResponseException) {

        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
