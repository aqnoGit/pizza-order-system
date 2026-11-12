package com.pizzaria.repository;

import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.StatusComanda;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComandaRepository extends JpaRepository<Comanda, UUID> {

    boolean existsByMesaIdAndStatus(UUID mesaId, StatusComanda status);

    @Query(value = """
            SELECT COALESCE(SUM(i.preco_unitario * i.quantidade), 0)
            FROM itens_pedido i
            JOIN pedidos p ON p.id = i.pedido_id
            WHERE p.comanda_id = :comandaId
            """, nativeQuery = true)
    BigDecimal somarValorDosItens(@Param("comandaId") UUID comandaId);
}
