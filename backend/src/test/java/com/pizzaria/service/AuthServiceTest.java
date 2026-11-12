package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pizzaria.dto.LoginFuncionarioRequest;
import com.pizzaria.dto.TokenResponse;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.exception.CredenciaisInvalidasException;
import com.pizzaria.repository.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String HASH = "hash-bcrypt";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void retornaTokenQuandoSenhaConfere() {
        Usuario garcom = funcionario(Perfil.GARCOM, HASH);
        when(usuarioRepository.findByNomeAndPerfilIn(any(), any())).thenReturn(Optional.of(garcom));
        when(passwordEncoder.matches("segredo", HASH)).thenReturn(true);
        when(jwtService.gerarToken(garcom)).thenReturn("token-jwt");

        TokenResponse resposta = authService.loginFuncionario(new LoginFuncionarioRequest("maria", "segredo"));

        assertThat(resposta.token()).isEqualTo("token-jwt");
    }

    @Test
    void buscaApenasPerfisGarcomECozinha() {
        Usuario cozinheiro = funcionario(Perfil.COZINHA, HASH);
        when(usuarioRepository.findByNomeAndPerfilIn(any(), any())).thenReturn(Optional.of(cozinheiro));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        authService.loginFuncionario(new LoginFuncionarioRequest("joao", "segredo"));

        verify(usuarioRepository).findByNomeAndPerfilIn("joao", java.util.List.of(Perfil.GARCOM, Perfil.COZINHA));
    }

    @Test
    void rejeitaQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByNomeAndPerfilIn(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginFuncionario(new LoginFuncionarioRequest("ninguem", "x")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Credenciais incorretas");
        verifyNoInteractions(jwtService);
    }

    @Test
    void rejeitaQuandoSenhaNaoConfere() {
        when(usuarioRepository.findByNomeAndPerfilIn(any(), any()))
                .thenReturn(Optional.of(funcionario(Perfil.GARCOM, HASH)));
        when(passwordEncoder.matches("errada", HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.loginFuncionario(new LoginFuncionarioRequest("maria", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(jwtService);
    }

    @Test
    void rejeitaQuandoUsuarioNaoTemCredenciais() {
        when(usuarioRepository.findByNomeAndPerfilIn(any(), any()))
                .thenReturn(Optional.of(funcionario(Perfil.GARCOM, null)));

        assertThatThrownBy(() -> authService.loginFuncionario(new LoginFuncionarioRequest("maria", "segredo")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    private Usuario funcionario(Perfil perfil, String credenciais) {
        return new Usuario(UUID.randomUUID(), "maria", "11999999999", perfil, credenciais);
    }
}
