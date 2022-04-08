package br.com.alura.forum.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.alura.forum.repository.UsuarioRepository;

@EnableWebSecurity
@Configuration
public class SecurityConfigurations extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private AutenticacaoService autenticacaoService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Override
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	// configurações de autenticação
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		/*
		 *  definei o tipo de encriptação hashing que utilizaremos na API.
		 *  nesse caso usamos o BCryptPasswordEncoder().
		 *  
		 *  o password deve estar salvo encriptado no banco de dados.
		 *  podemos usar o método new BCryptPasswordEncoder().encode("123456") para verificar a hash gerada e salvar ou enviar.
		 */
		auth.userDetailsService(autenticacaoService).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	// configurações de autorização.
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		// indica o tipo de método e o endpoint e permite que todas requisições sejam liberadas.
		.antMatchers(HttpMethod.GET, "/topicos").permitAll()
		/*
		 * "*" no path: caractere coringa.
		 * como o security não vai saber que ele espera o id, usa-se o coringa para dizer /topicos/"alguma coisa".
		 */
		.antMatchers(HttpMethod.GET, "/topicos/*").permitAll()
		.antMatchers(HttpMethod.POST, "/auth").permitAll()
		// actuator
		.antMatchers(HttpMethod.GET, "/actuator/**").permitAll()
		// indica o bloqueio de todos os endpoints que não foram liberados anteriormente com o método permitAll().
		.anyRequest().authenticated()
		/*
		 * csrf(): Abreviação para Cross site request forgery. Um tipo de ataque hacker em web apps.
		 * autenticação via token deixa a aplicação livre desse tipo de ataque.
		 * desabilita para o security não fazer a validação do token do csrf.
		 */
		.and().csrf().disable()
		/*
		 * avisou para o security quando fizer autenticação não é para criar session, pois vai usar token de maneira STATELESS.
		 */
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and().addFilterBefore(new AutenticacaoViaTokenFilter(tokenService, usuarioRepository), UsernamePasswordAuthenticationFilter.class);
	}
	
	/*
	 *  configurações de arquivos estáticos.
	 *  1. HTML.
	 *  2. CSS.
	 *  3. JavaScript.
	 *  4. Imagens.
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		
		web.ignoring().antMatchers("/**.html", "/v2/api-docs", "/webjars/**", "/configuration/**", "/swagger-resources/**");
	}
	
	/*
	 * só para gerar o encode do password do data.sql para testes.
	 */
//	public static void main(String[] args) {
//		System.out.println(new BCryptPasswordEncoder().encode("123456"));
//	}
}
