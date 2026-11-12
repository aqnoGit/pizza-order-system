package com.pizzaria.service;

import com.pizzaria.dto.LoginFuncionarioRequest;
import com.pizzaria.dto.TokenResponse;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.exception.CredenciaisInvalidasException;
import com.pizzaria.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final List<Perfil> PERFIS_FUNCIONARIO = List.of(Perfil.GARCOM, Perfil.COZINHA);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse loginFuncionario(LoginFuncionarioRequest requisicao) {
        Usuario funcionario = usuarioRepository
                .findByNomeAndPerfilIn(requisicao.usuario(), PERFIS_FUNCIONARIO)
                .filter(usuario -> senhaConfere(requisicao.senha(), usuario))
                .orElseThrow(CredenciaisInvalidasException::new);
        return new TokenResponse(jwtService.gerarToken(funcionario));
    }

    private boolean senhaConfere(String senha, Usuario usuario) {
        return usuario.getCredenciais() != null && passwordEncoder.matches(senha, usuario.getCredenciais());
    }
}
