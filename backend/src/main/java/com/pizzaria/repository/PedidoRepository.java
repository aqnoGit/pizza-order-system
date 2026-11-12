package com.pizzaria.repository;

import com.pizzaria.entity.Pedido;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
}
