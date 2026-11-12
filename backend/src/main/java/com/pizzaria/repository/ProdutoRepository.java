package com.pizzaria.repository;

import com.pizzaria.entity.Produto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
}
