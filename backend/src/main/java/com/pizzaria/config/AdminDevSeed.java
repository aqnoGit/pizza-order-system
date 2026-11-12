package com.pizzaria.config;

import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class AdminDevSeed implements CommandLineRunner {

    static final String NOME_ADMIN = "admin";
    static final String TELEFONE_ADMIN = "00000000000";
    static final String SENHA_ADMIN = "admin123";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... argumentos) {
        if (usuarioRepository.existsByNomeAndPerfil(NOME_ADMIN, Perfil.ADMIN)) {
            return;
        }
        usuarioRepository.save(new Usuario(
                UUID.randomUUID(), NOME_ADMIN, TELEFONE_ADMIN, Perfil.ADMIN, passwordEncoder.encode(SENHA_ADMIN)));
    }
}
