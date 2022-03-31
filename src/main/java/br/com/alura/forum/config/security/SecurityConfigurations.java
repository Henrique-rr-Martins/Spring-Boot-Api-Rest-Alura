package br.com.alura.forum.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfigurations extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private AutenticacaoService autenticacaoService;
	
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
		// indica o bloqueio de todos os endpoints que não foram liberados anteriormente com o método permitAll().
		.anyRequest().authenticated()
		/* 
		 * ativa o formLogin do spring.
		 * quando acessa o endpoint pelo web browser, aparecerá uma tela de login do spring.
		 */
		.and().formLogin();
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
	}
	
	/*
	 * só para gerar o encode do password do data.sql para testes.
	 */
//	public static void main(String[] args) {
//		System.out.println(new BCryptPasswordEncoder().encode("123456"));
//	}
}
