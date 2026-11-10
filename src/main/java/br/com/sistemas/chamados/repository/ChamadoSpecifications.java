package br.com.sistemas.chamados.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import jakarta.persistence.criteria.Predicate;

public final class ChamadoSpecifications {
    private ChamadoSpecifications() {
    }

    public static Specification<Chamado> filtrar(StatusChamado status, Prioridade prioridade, Long clienteId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (prioridade != null) predicates.add(criteriaBuilder.equal(root.get("prioridade"), prioridade));
            if (clienteId != null) predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), clienteId));
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
