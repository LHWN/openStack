package com.spring.openstack.configure.filters;

import com.spring.openstack.data.OpenStackAuth;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenStackFilter extends UsernamePasswordAuthenticationFilter {
    private boolean postOnly = true;
    private SessionAuthenticationStrategy sessionAuthenticationStrategy = new NullAuthenticatedSessionStrategy();
    private boolean continueChainBeforeSuccessfulAuthentication = false;

    public String obtainDomain(HttpServletRequest httpServletRequest) { return (String)httpServletRequest.getParameter("domain"); }

    public OpenStackFilter() {
    }

    public OpenStackFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    @Override
    public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @Override
    public void setContinueChainBeforeSuccessfulAuthentication(boolean continueChainBeforeSuccessfulAuthentication) {
        this.continueChainBeforeSuccessfulAuthentication = continueChainBeforeSuccessfulAuthentication;
    }

    // POST 로 넘어온 username, password 를 추출하여 인증을 시도하는 부분이다.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request);
        username = (username != null) ? username : "";
        username = username.trim();

        String password = obtainPassword(request);
        password = (password != null) ? password : "";

        String domain = obtainDomain(request);
        domain = (domain != null) ? domain : "";
        domain = domain.trim();

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        usernamePasswordAuthenticationToken.setDetails(domain);

        return this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
    }

    private boolean checkExpire(String tokenExpire) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date tokenExpireDateTime = dateFormat.parse(tokenExpire);

        return tokenExpireDateTime.after(new Date()); // 현재 시간과 비교하여 tokenExpireDateTime 이 더 뒤에 있으면 true 이다. 즉, 아직 만료되지 않았을 경우 true 를 반환.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // 인증이 필요없으면 doFilter 를 통해 다음 Chain 이 실행되도록 한다.
        if(!requiresAuthentication(httpServletRequest, httpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // Session 을 가져와서 그 안에 있는 token 정보를 가져온다.
        HttpSession httpSession = httpServletRequest.getSession();
        String tokenId = (String)httpSession.getAttribute("unscopedTokenId");
        String tokenExpire = (String)httpSession.getAttribute("tokenExpire");

        // 이미 인증이 된 경우이다.
        if(tokenId != null && tokenExpire != null) {
            try {
                // 토큰이 만료되지 않았다면
                if(checkExpire(tokenExpire)) {
                    // 객체를 생성하는데, 객체가 생성되었다는 것은 인증이 되었다는 뜻
                    new OpenStackAuth(tokenId);
                    chain.doFilter(httpServletRequest, httpServletResponse);
                } else {
                    unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Token has been expired."));
                }
            } catch (ClientResponseException clientResponseException) {
                unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Token Id is invalidated."));
            } catch (ParseException parseException) {
                unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Token expire date time format invalidated."));
            }
        } else {
            // 아직 인증이 되지 않은 경우이다. (토큰이 없으면 POST 로 넘어온 username, password, domain 으로 인증을 진행한다.)
            if(this.postOnly && httpServletRequest.getMethod().equals("POST")) {
                String username = this.obtainUsername(httpServletRequest);
                String password = this.obtainPassword(httpServletRequest);
                String domain = this.obtainDomain(httpServletRequest);

                if(username != null && password != null && domain != null) {
                    try {
                        Authentication authenticationResult = attemptAuthentication(httpServletRequest, httpServletResponse);
                        if(authenticationResult == null) {
                            return;
                        }

                        this.sessionAuthenticationStrategy.onAuthentication(authenticationResult, httpServletRequest, httpServletResponse);

                        if(this.continueChainBeforeSuccessfulAuthentication) {
                            chain.doFilter(httpServletRequest, httpServletResponse);
                        }
                        successfulAuthentication(httpServletRequest, httpServletResponse, chain, authenticationResult);
                    } catch (InternalAuthenticationServiceException internalAuthenticationServiceException) {
                        unsuccessfulAuthentication(httpServletRequest, httpServletResponse, internalAuthenticationServiceException);
                    } catch (AuthenticationServiceException authenticationServiceException) {
                        unsuccessfulAuthentication(httpServletRequest, httpServletResponse, authenticationServiceException);
                    }
                } else {
                    unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Bye bye."));
                }
            } else {
                unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Bye bye."));
            }
        }

            // filter, manager, provider 는 인증에 대한 것만,
            // 인증이 성공/실패했을 경우는 handler 에 가서 처리하는 구조를 권장

    }
}
