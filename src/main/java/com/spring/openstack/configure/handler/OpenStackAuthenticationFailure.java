package com.spring.openstack.configure.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class OpenStackAuthenticationFailure extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String messageUrl = "/login?error=invalie";
        HttpSession httpSession = request.getSession();
        request.getSession().removeAttribute("unscopedTokenId");
        request.getSession().removeAttribute("unscopedToken");
        request.getSession().removeAttribute("scopedTokenId");
        request.getSession().removeAttribute("scopedToken");
        request.getSession().removeAttribute("tokenExpire");
        request.getSession().removeAttribute("projectId");
        request.getSession().removeAttribute("projectName");
        request.getSession().removeAttribute("domain");

        this.setDefaultFailureUrl(messageUrl);

        super.onAuthenticationFailure(request, response, exception);
    }
}
