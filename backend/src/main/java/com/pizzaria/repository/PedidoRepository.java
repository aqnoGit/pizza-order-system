package com.pizzaria.repository;

import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.StatusPedido;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByStatusOrderByRegistradoEmAsc(StatusPedido status);

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByComandaId(UUID comandaId);
}
