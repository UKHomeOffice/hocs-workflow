package uk.gov.digital.ho.hocs.workflow;

//import org.apache.camel.Processor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class RequestData implements HandlerInterceptor {


    private static final String CORRELATION_ID_HEADER = "X-Correlationid";
    private static final String USER_ID_HEADER = "X-Auth-Userid";
    private static final String USERNAME_HEADER = "X-Auth-Username";

    private static final String ANONYMOUS = "anonymous";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserId(request));
        MDC.put(USERNAME_HEADER, initialiseUserName(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(USERNAME_HEADER, userId());
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        MDC.clear();
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return !isNullOrEmpty(correlationId) ? correlationId : UUID.randomUUID().toString();
    }

    private String initialiseUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return !isNullOrEmpty(userId) ? userId : ANONYMOUS;
    }

    private String initialiseUserName(HttpServletRequest request) {
        String username = request.getHeader(USERNAME_HEADER);
        return !isNullOrEmpty(username) ? username : ANONYMOUS;
    }


    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() { return MDC.get(USER_ID_HEADER); }

    public String username() { return MDC.get(USERNAME_HEADER); }


    //public static Processor transferHeadersToMDC() {
    //    return ex -> {
    //        MDC.put(CORRELATION_ID_HEADER, ex.getIn().getHeader(CORRELATION_ID_HEADER, String.class));
    //        MDC.put(USER_ID_HEADER, ex.getIn().getHeader(USER_ID_HEADER, String.class));
    //        MDC.put(USERNAME_HEADER, ex.getIn().getHeader(USERNAME_HEADER, String.class));
    //    };
    //}

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.equals("");
    }

}
