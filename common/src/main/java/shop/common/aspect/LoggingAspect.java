package shop.common.aspect;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collection;

//todo need refactoring. Mb need create separate aspects for controller and service (need divide some methods)

@Component
@Aspect
@Slf4j
public class  LoggingAspect {
    @Pointcut("execution (public * shop.auth.controller.*.*(..))")
    public void controllerLogging() {}

    @Pointcut("execution (public * shop.auth.service.*.*(..))")
    public void serviceLogging() {}

    @Before("controllerLogging()")
    public void doBeforeLogController(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        if (request != null) {
            log.info("NEW REQUEST : IP: {}, URL: {}, HTTP_METHOD : {}, CONTROLLER_METHOD : {}. {}",
                    request.getRemoteAddr(),
                    request.getRequestURL().toString(),
                    request.getMethod(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }else{
            log.info("️NEW REQUEST (no HttpServletRequest): {}.{}()",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }
    }

    @Before("serviceLogging()")
    public void doBeforeLogService(JoinPoint joinPoint) {

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        //array of arguments passed to the method
        Object[] args = joinPoint.getArgs();

        String argsString = args.length > 0 ? Arrays.toString(args) : "METHOD WITHOUT ARGS";

        log.info("RUN SERVICE: SERVICE_METHOD: {}.{}\n METHOD ARGUMENTS [{}]",
                className, methodName, argsString);
    }

    @AfterReturning(returning = "returnObject", pointcut = "controllerLogging()")
    public void doAfterReturningLogController(Object returnObject) {
        if (returnObject instanceof Collection<?> collection) {
            log.info("RETURNED COLLECTION: ( {} ITEMS: )", collection.size());
            for (Object item : collection) {
                log.info("RETURNED ITEM: {}", item);
            }
        }else{
            log.info("RETURNED OBJECT:\n{}", returnObject);
        }
    }
}
