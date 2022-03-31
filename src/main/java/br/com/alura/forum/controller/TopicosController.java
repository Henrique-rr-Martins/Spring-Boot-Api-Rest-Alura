package br.com.alura.forum.controller;

import java.net.URI;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	/*
	 * cache: necessário declarar a dependência de cache no pom.xml
	 *        necessário usar a annotation @EnableCaching na main class que usa o método main.
	 * parametro value: usado para nomear o cache no Spring Boot para que ele diferencie os caches.
	 *                  funciona como um id do cache.
	 * habilitando log do cache: necessário aplicar uma propriedade no Application.properties/yml:
	 *                           "spring.jpa.properties.hibernate.show_sql=true"
	 *                           use a propriedade format_sql abaixo para formatar o sql. coloque junto no Application.properties/yml.
	 *                           "spring.jpa.properties.hibernate.format_sql=true"
	 * IMPORTANTE: NECESSARIO CONFIGURAR MÉTODOS DE ATUALIZAÇÃO E DELEÇÃO PARA RECONHECER QUANDO O CACHE É INVALIDADO POR FALTA DE NOVOS
	 *             DADOS. EXEMPLO NO METODO "CADASTRAR"
	 */
	@Cacheable(value = "listaDeTopicos")
	/*
	 *  1. @RequestParam torna obrigatório o paramentro
	 *  2. Se não usar annotation o parametro é considerado request param mas não é obrigatório
	 *  3. Se usar @RequestParam com required false, também não será obrigatório
	 */
	public Page<TopicoDTO> listar(@RequestParam(required = false) String nomeCurso, 
			@RequestParam int pagina, @RequestParam int qtd, @RequestParam String ordenacao){
		
		 /*
		 * 1. Pageable é uma interface.
		 * 2. PageRequest é uma classe.
		 * 3. Método of recebe os seguintes parametros:
		 * 	  a) pagina: nº da página que está.
		 *    b) quantidade: quantidade de objetos por página.
		 *    c) Direction.ASC: enum para indicar se é ASC ou DESC.
		 *    d) ordenacao: varArgs de string (conjunto de strings) que tem o nome do atributo para filtrar.
		 */
		Pageable paginacao = PageRequest.of(pagina, qtd, Direction.DESC, ordenacao);
		
		if(nomeCurso == null) {
			Page<Topico> topicos = this.topicoRepository.findAll(paginacao);
		
			return TopicoDTO.convert(topicos);
		}
		Page<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
		
		return TopicoDTO.convert(topicos);
	}
	/* FORM - Dados que chegam do cliente para o servidor. */
	@PostMapping
	// de acordo com documentação do JPA, todo método com escrita deve ter o @Transactional.
	@Transactional
	/*
	 *  1. @CacheEvict: invalida cache após alteração de dados.
	 *     a) param value: identifica o cache que será limpo através do seu nome.
	 *     b) param allEntries: pergunta se é para limpar todos os registros.
	 *        I. true: limpa todos registros.
	 *        II. false: não sei.
	 */
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
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
	
	
	/*
	 * put: sobrescrever o recurso inteiro.
	 * patch: fazer apenas uma pequena atualização.
	 */
	@PutMapping("/{id}")
	/*
	 * avisa para o spring commitar transação no final do método, atualizando a entidade no banco.
	 * de acordo com documentação do JPA, todo método com escrita deve ter o @Transactional.
	 */
	@Transactional
	/*
	 *  1. @CacheEvict: invalida cache após alteração de dados.
	 *     a) param value: identifica o cache que será limpo através do seu nome.
	 *     b) param allEntries: pergunta se é para limpar todos os registros.
	 *        I. true: limpa todos registros.
	 *        II. false: não sei.
	 */
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<TopicoDTO> atualizar(@PathVariable("id") Long id, @RequestBody @Valid AtualizacaoTopicoForm form){
		
		Optional<Topico> optional = topicoRepository.findById(id);
		
		if(optional.isEmpty())
			return ResponseEntity.notFound().build();
		
		Topico topico = form.atualizar(id, topicoRepository);
		
		return ResponseEntity.ok(new TopicoDTO(topico));
	}
	
	@DeleteMapping("/{id}")
	// de acordo com documentação do JPA, todo método com escrita deve ter o @Transactional.
	@Transactional
	/*
	 *  1. @CacheEvict: invalida cache após alteração de dados.
	 *     a) param value: identifica o cache que será limpo através do seu nome.
	 *     b) param allEntries: pergunta se é para limpar todos os registros.
	 *        I. true: limpa todos registros.
	 *        II. false: não sei.
	 */
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<?> remover(@PathVariable("id") Long id){
		
		Optional<Topico> topico = topicoRepository.findById(id);
		
		if(topico.isEmpty())
			return ResponseEntity.notFound().build();
		
		topicoRepository.deleteById(id);
		
		return ResponseEntity.ok().build();
	}
}
