package com.pizzaria.repository;

import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByNomeAndPerfilIn(String nome, Collection<Perfil> perfis);

    Optional<Usuario> findByTelefoneAndPerfilIn(String telefone, Collection<Perfil> perfis);

    boolean existsByNomeAndPerfil(String nome, Perfil perfil);
}
