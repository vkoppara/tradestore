package com.venkata.tradestore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.savedrequest.NullRequestCache;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.requestCache((requestCache) -> requestCache
				.requestCache(new NullRequestCache())
			)
			.httpBasic(Customizer.withDefaults());
	}
	// @formatter:on

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
				.withUser(User.withUsername("user").password("{noop}password").roles("USER").build());
	}

}