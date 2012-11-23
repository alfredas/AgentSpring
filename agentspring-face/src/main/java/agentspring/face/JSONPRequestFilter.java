package agentspring.face;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class JSONPRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isJSONPRequest(request)) {
            ServletOutputStream out = response.getOutputStream();
            out.println(getCallbackMethod(request) + "(");
            filterChain.doFilter(request, response);
            out.println(");");
            response.setContentType("text/javascript");
        } else {
            filterChain.doFilter(request, response);
        }

    }

    private String getCallbackMethod(HttpServletRequest httpRequest) {
        return httpRequest.getParameter("callback");
    }
    
    private boolean isJSONPRequest(HttpServletRequest httpRequest) {
        String callbackMethod = getCallbackMethod(httpRequest);
        return (callbackMethod != null && callbackMethod.length() > 0);
    }

}

