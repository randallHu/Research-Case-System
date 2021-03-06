package oil.config;


import oil.component.CustomLoginSuccessHandler;
import oil.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * Created by  waiter on 18-6-18.
 * @author waiter
 *
 * springboot security配置
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceIml;



    /**
     * 配置拦截器
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/admin","/admin/**","/admin/*",
                        "/type","/type/**","/type/*",
                        "/tag","/tag/**","/tag/*",
                        "/lib","/lib/**","/lib/*",
                        "/doc/uploads/*","/doc/uploads/**",
                        "/doc//remove/*",
                        "/case/recovery/*","/case/recovery/*","/case/delete/*",
                        "/case/remove/*","/case/add.html","/case/change.html",
                        "/get","/get/**",
                        "/user/add","/user/change2","/user/change_pwd/*").access("hasRole('ADMIN')")
                .antMatchers("/user").hasAuthority("ROLE_USER")
                .antMatchers("/").permitAll()
                .antMatchers("/static/**", "/static/*").permitAll()
                .antMatchers("/script/*","/font/**", "/css/*", "/images/*", "/js/*","/Wopop_files/*").permitAll()
                .antMatchers("/case/**/case_info.html","/case/**/date.html","/case/**/tag.html","/case/**/type.html").permitAll()
                .antMatchers("/case/**","/search/*","/index").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login").successHandler(customLoginSuccessHandler)
                .permitAll()
                .and()
                .logout()
                .permitAll();
        http.csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceIml);
    }



    /**
     * 配置密码加密工具
     *
     * @return
     */
    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


}