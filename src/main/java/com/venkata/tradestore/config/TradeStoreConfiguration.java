package com.venkata.tradestore.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * @author vkopp
 *
 */
@Configuration
@EnableAspectJAutoProxy
@Aspect
@Component
@EnableRedisHttpSession
public class TradeStoreConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(TradeStoreConfiguration.class);
	
	@Autowired
        private Environment env;
	
	@Bean
	public LettuceConnectionFactory connectionFactory() {
		RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration();
                redisConf.setHostName(env.getProperty("spring.redis.host"));
                redisConf.setPort(Integer.parseInt(env.getProperty("spring.redis.port")));
                redisConf.setPassword(RedisPassword.of(env.getProperty("spring.redis.password")));

                return new LettuceConnectionFactory(redisConf);
		
		//return new LettuceConnectionFactory(); 
	}

	@Bean
	public HttpSessionIdResolver httpSessionIdResolver() {
		return HeaderHttpSessionIdResolver.xAuthToken(); 
	}
		

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
