package ch.hsr.servicestoolkit.editor.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.csrf.CsrfFilter;

import ch.hsr.servicestoolkit.editor.security.AjaxAuthenticationFailureHandler;
import ch.hsr.servicestoolkit.editor.security.AjaxAuthenticationSuccessHandler;
import ch.hsr.servicestoolkit.editor.security.AjaxLogoutSuccessHandler;
import ch.hsr.servicestoolkit.editor.security.AuthoritiesConstants;
import ch.hsr.servicestoolkit.editor.security.Http401UnauthorizedEntryPoint;
import ch.hsr.servicestoolkit.editor.web.filter.CsrfCookieGeneratorFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Inject
	private Environment env;

	@Inject
	private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

	@Inject
	private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

	@Inject
	private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

	@Inject
	private Http401UnauthorizedEntryPoint authenticationEntryPoint;

	@Inject
	private UserDetailsService userDetailsService;

	@Inject
	private RememberMeServices rememberMeServices;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Inject
	public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Override
	public void configure(final WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/scripts/**/*.{js,html}").antMatchers("/bower_components/**").antMatchers("/i18n/**").antMatchers("/assets/**")
				.antMatchers("/swagger-ui/index.html").antMatchers("/test/**").antMatchers("/console/**");
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.csrf().ignoringAntMatchers("/websocket/**").and().addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class).exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint).and().rememberMe().rememberMeServices(rememberMeServices).rememberMeParameter("remember-me")
				.key(env.getProperty("jhipster.security.rememberme.key")).and().formLogin().loginProcessingUrl("/api/authentication")
				.successHandler(ajaxAuthenticationSuccessHandler).failureHandler(ajaxAuthenticationFailureHandler).usernameParameter("j_username").passwordParameter("j_password")
				.permitAll().and().logout().logoutUrl("/api/logout").logoutSuccessHandler(ajaxLogoutSuccessHandler).deleteCookies("JSESSIONID").permitAll().and().headers()
				.frameOptions().disable().and().authorizeRequests().antMatchers("/api/register").permitAll().antMatchers("/api/activate").permitAll()
				.antMatchers("/api/authenticate").permitAll().antMatchers("/api/account/reset_password/init").permitAll().antMatchers("/api/account/reset_password/finish")
				.permitAll().antMatchers("/api/logs/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/api/audits/**").hasAuthority(AuthoritiesConstants.ADMIN)
				.antMatchers("/api/**").authenticated().antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/websocket/**").permitAll()
				.antMatchers("/metrics/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/health/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/trace/**")
				.hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/dump/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/shutdown/**")
				.hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/beans/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/configprops/**")
				.hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/info/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/autoconfig/**")
				.hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/env/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/trace/**")
				.hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/mappings/**").hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/v2/api-docs/**").permitAll()
				.antMatchers("/configuration/security").permitAll().antMatchers("/configuration/ui").permitAll().antMatchers("/swagger-ui/index.html")
				.hasAuthority(AuthoritiesConstants.ADMIN).antMatchers("/protected/**").authenticated();

	}

	@Bean
	public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
		return new SecurityEvaluationContextExtension();
	}
}
