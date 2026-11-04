package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.entity.Comentario;

public record ComentarioResponse(Long id, String texto, String autor, LocalDateTime data) {
    public static ComentarioResponse de(Comentario comentario) {
        return new ComentarioResponse(comentario.getId(), comentario.getTexto(), comentario.getAutor(), comentario.getData());
    }
}
