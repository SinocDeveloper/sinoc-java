package org.sinoc.shell.jsonrpc;

import org.sinoc.shell.config.RpcEnabledCondition;
import org.sinoc.shell.util.AppConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@WebFilter(urlPatterns = AppConst.JSON_RPC_PATH)
@Conditional(RpcEnabledCondition.class)
public class AddContentTypeFilter implements Filter {
	Logger log = LoggerFactory.getLogger(AddContentTypeFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (AppConst.JSON_RPC_PATH.equals(httpRequest.getRequestURI())) {
                log.info("Found " + httpRequest.getRequestURI());
                AddParamsToHeader updatedRequest = new AddParamsToHeader((HttpServletRequest) request);
                httpResponse.addHeader("content-type", "application/json");
                httpResponse.addHeader("accept", "application/json");
                chain.doFilter(updatedRequest, response);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            throw new RuntimeException("AddContentTypeFilter supports only HTTP requests.");
        }
    }

    @Override
    public void destroy() {

    }
}

class AddParamsToHeader extends HttpServletRequestWrapper {
	Logger log = LoggerFactory.getLogger(AddParamsToHeader.class);
	
    public AddParamsToHeader(HttpServletRequest request) {
        super(request);
    }

    public String getHeader(String name) {
        log.info("getHeader " + name + ". Result:" + super.getHeader(name));
        if (name != null && "content-type".equals(name.toLowerCase())) {
            return "application/json";
        }

        return super.getHeader(name);
    }

    public Enumeration getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
//        names.add("content-type");
//        names.addAll(Collections.list(super.getParameterNames()));
        return Collections.enumeration(names);
    }
}
