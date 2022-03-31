package br.com.alura.forum.config.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.alura.forum.modelo.Usuario;
import br.com.alura.forum.repository.UsuarioRepository;

@Service
// implementa UserDetailsService para ser reconhecido como Service responsável por consultar o usuário no banco de dados.
public class AutenticacaoService implements UserDetailsService {
	
	@Autowired
	UsuarioRepository repository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Usuario> optional = repository.findByEmail(username);
		
		if(optional.isEmpty())
			throw new UsernameNotFoundException("Dados inválidos!");
		
		return optional.get();
	}
	
}
