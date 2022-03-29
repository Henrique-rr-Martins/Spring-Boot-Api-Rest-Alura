package br.com.alura.forum.modelo.dto;

public class ErroDeFormularioDTO {
	
	private String campo;
	private String mensagem;
	
	public ErroDeFormularioDTO(String campo, String mensagem) {
		this.campo = campo;
		this.mensagem = mensagem;
	}

	public String getCampo() {
		return campo;
	}

	public String getMensagem() {
		return mensagem;
	}
}
