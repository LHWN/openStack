package com.spring.openstack.configure;

import com.spring.openstack.configure.filters.OpenStackFilter;
import com.spring.openstack.data.Constants;
import com.spring.openstack.data.OpenStackAuth;
import org.openstack4j.api.OSClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class BeanInitializer {
    // 사용자 요청이 있을 때마다 토큰으로 생성하는 영역
    @Bean
    @RequestScope
    public OSClient.OSClientV3 osClient() {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String scopedTokenId = (String)httpServletRequest.getSession().getAttribute("scopedTokenId");

        OpenStackAuth openStackAuth = OpenStackAuth.projectScopedAuth(Constants.ADMIN_NAME, Constants.ADMIN_PASSWORD, "Default", Constants.ADMIN_PROJECT);
        if(openStackAuth.validateToken(scopedTokenId)) {
            openStackAuth.setToken(openStackAuth.getTokenDetails(scopedTokenId));
        }

        return openStackAuth.getOsClient();
    }

    // 어드민을 미리 만들어놓고 활용하는 부분
    @Bean
    public OSClient.OSClientV3 adminOsClient() {
        return OpenStackAuth.projectScopedAuth(Constants.ADMIN_NAME, Constants.ADMIN_PASSWORD, "Default", Constants.ADMIN_PROJECT).getOsClient();
    }
}
