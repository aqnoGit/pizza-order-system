package com.pizzaria.repository;

import com.pizzaria.entity.Comanda;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComandaRepository extends JpaRepository<Comanda, UUID> {
}
