package com.venkata.tradestore.config;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import com.venkata.tradestore.entity.TradeRecord;

@RunWith(MockitoJUnitRunner.class)
public class TradeStoreConfigurationTest {
	
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    private ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
    private MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

    
    private TradeStoreConfiguration config = new TradeStoreConfiguration();

    @Test
    public void testIntercept() throws Throwable {
    	doReturn(methodSignature).when(proceedingJoinPoint).getSignature();
    	doReturn(this.getClass()).when(methodSignature).getDeclaringType();
    	doReturn("MethodName").when(methodSignature).getName();
    		
	
		
    	config.profileAllMethods(proceedingJoinPoint, new Intercepted() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				// TODO Auto-generated method stub
				return null;
			}
		});
    	verify(proceedingJoinPoint, times(1)).proceed();
    	
    }
}
