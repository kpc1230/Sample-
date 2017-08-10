package com.thed.zephyr.capture.web.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BonfireP3PFilter implements Filter {
    private static final String LOGIN_PAGE = "login.jsp";
    private static final String ADVANCE_CF_PAGE = "advancedcft.jspa";
    private final String filterName;

    protected BonfireP3PFilter() {
        filterName = this.getClass().getCanonicalName() + "_alreadyfiltered";
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // Only apply this filter once per request
        if (request.getAttribute(filterName) != null) {
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        } else {
            httpServletRequest.setAttribute(filterName, Boolean.TRUE);
        }
        // Do stuff
        String requestUrl = httpServletRequest.getRequestURL().toString().toLowerCase();
        if (requestUrl.contains(LOGIN_PAGE) || requestUrl.contains(ADVANCE_CF_PAGE)) {
            httpServletResponse.addHeader("p3p", "CP=\"IDC DSP COR ADM DEV TAI PSA PSD IVA IVD CON HIS OUR IND CNT\"");
        }
        // Call next filter chain
        chain.doFilter(httpServletRequest, httpServletResponse);
    }
}
