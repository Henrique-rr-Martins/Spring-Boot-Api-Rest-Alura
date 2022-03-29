package br.com.alura.forum.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.modelo.dto.DetalhesTopicoDTO;
import br.com.alura.forum.modelo.dto.TopicoDTO;
import br.com.alura.forum.modelo.form.AtualizacaoTopicoForm;
import br.com.alura.forum.modelo.form.TopicoForm;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;
	@Autowired 
	CursoRepository cursoRepository;
	
	/* DTO - Dados que saem do servidor para o cliente. */
	@GetMapping
	public List<TopicoDTO> lista(String nomeCurso){
		if(nomeCurso == null) {
			List<Topico> topicos = this.topicoRepository.findAll();
		
			return TopicoDTO.convert(topicos);
		}
		List<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso);
		
		return TopicoDTO.convert(topicos);
	}
	/* FORM - Dados que chegam do cliente para o servidor. */
	@PostMapping
	// de acordo com documentação do JPA, todo método com escrita deve ter o @Transactional
	@Transactional
	public ResponseEntity<TopicoDTO> cadastrar(@RequestBody @Valid TopicoForm topicoForm, UriComponentsBuilder uriBuilder) {
		Topico topico = topicoForm.converter(cursoRepository);
		topicoRepository.save(topico);
		
		URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
		
		return ResponseEntity.created(uri).body(new TopicoDTO(topico));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<DetalhesTopicoDTO> detalhar(@PathVariable("id") Long id) {
		
		Optional<Topico> topico = topicoRepository.findById(id);
		
		if(topico.isEmpty())
			return ResponseEntity.notFound().build();
		
		return ResponseEntity.ok(new DetalhesTopicoDTO(topico.get()));
	}
	
	
	// put: sobrescrever o recurso inteiro.
	// patch: fazer apenas uma pequena atualização.
	@PutMapping("/{id}")
	// avisa para o spring commitar transação no final do método, atualizando a entidade no banco.
	// de acordo com documentação do JPA, todo método com escrita deve ter o @Transactional
	@Transactional
	public ResponseEntity<TopicoDTO> atualizar(@PathVariable("id") Long id, @RequestBody @Valid AtualizacaoTopicoForm form){
		
		Optional<Topico> optional = topicoRepository.findById(id);
		
		if(optional.isEmpty())
			return ResponseEntity.notFound().build();
		
		Topico topico = form.atualizar(id, topicoRepository);
		
		return ResponseEntity.ok(new TopicoDTO(topico));
	}
	
	@DeleteMapping("/{id}")
	// de acordo com documentação do JPA, todo método com escrita deve ter o @Transactional
	@Transactional
	public ResponseEntity<?> remover(@PathVariable("id") Long id){
		
		Optional<Topico> topico = topicoRepository.findById(id);
		
		if(topico.isEmpty())
			return ResponseEntity.notFound().build();
		
		topicoRepository.deleteById(id);
		
		return ResponseEntity.ok().build();
	}
}
