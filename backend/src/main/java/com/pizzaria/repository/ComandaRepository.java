package com.pizzaria.repository;

import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.StatusComanda;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComandaRepository extends JpaRepository<Comanda, UUID> {

    boolean existsByMesaIdAndStatus(UUID mesaId, StatusComanda status);
}
