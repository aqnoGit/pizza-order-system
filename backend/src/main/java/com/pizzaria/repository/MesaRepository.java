package com.pizzaria.repository;

import com.pizzaria.entity.Mesa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, UUID> {

    List<Mesa> findAllByOrderByNumeroAsc();
}
