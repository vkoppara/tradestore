package com.venkata.tradestore.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author vkopp
 *
 */
@Configuration
@EnableAspectJAutoProxy
@Aspect
@Component
public class TradeStoreConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(TradeStoreConfiguration.class);

	/**
	 * This an AOP interceptor method called for the methods annotated with Intercepted
	 * This method logs the timetaken by the methods in milliseconds 
	 * @param proccedingJointPoint
	 * @param intercepted
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(intercepted)")
	public Object profileAllMethods(ProceedingJoinPoint proccedingJointPoint, Intercepted intercepted) throws Throwable{
		try {
			MethodSignature methodSignature = (MethodSignature) proccedingJointPoint.getSignature();
			String className = methodSignature.getDeclaringType().getSimpleName();
			String methodName = methodSignature.getName();
			
			final StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			Object result = proccedingJointPoint.proceed();
			stopWatch.stop();
			long timeTaken = stopWatch.getTotalTimeMillis();
			logger.info("Execution time of {}.{}::{}ms",className,methodName,timeTaken);
			return result;
		}catch(Throwable e) {
			throw e;
		}
	}
	
	
}
