package br.com.alura.forum.modelo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import br.com.alura.forum.modelo.Resposta;

public class RespostaDTO {
	
	private Long id;
	private String mensagem;
	private LocalDateTime dataCriacao;
	
	public RespostaDTO(Resposta resposta) {
		this.id = resposta.getId();
		this.mensagem = resposta.getMensagem();
		this.dataCriacao = resposta.getDataCriacao();
	}
	
	public static List<RespostaDTO> convert(List<Resposta> respostas){
		return respostas.stream().map(RespostaDTO::new).collect(Collectors.toList());
	}
	
	// getters.
	public Long getId() {
		return id;
	}

	public String getMensagem() {
		return mensagem;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}
}
