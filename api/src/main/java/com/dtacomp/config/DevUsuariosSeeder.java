package com.dtacomp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.api.schema.repositories.InstituicaoRepository;
import com.dtacomp.api.schema.repositories.UsuarioRepository;

/**
 * Usuários de teste para desenvolvimento local (somente com o perfil "dev").
 * Cria um ADMIN e um REVISOR, já que o cadastro público só gera MEMBROs.
 * Senha de ambos: dev12345
 */
@Component
@Profile("dev")
@Order(2)
public class DevUsuariosSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUsuariosSeeder.class);
    static final String SENHA_DEV = "dev12345";

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PasswordEncoder passwordEncoder;

    public DevUsuariosSeeder(UsuarioRepository usuarioRepository,
                             InstituicaoRepository instituicaoRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Instituicao instituicao = instituicaoRepository.findAll().stream().findFirst().orElse(null);
        criarSeNaoExiste("Administrador DTA", "admin@dta.dev", TipoPerfil.ADMIN, instituicao);
        criarSeNaoExiste("Revisor DTA", "revisor@dta.dev", TipoPerfil.REVISOR, instituicao);
    }

    private void criarSeNaoExiste(String nome, String email, TipoPerfil perfil, Instituicao instituicao) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash(passwordEncoder.encode(SENHA_DEV))
                .perfil(perfil)
                .instituicao(instituicao)
                .build());
        log.info("[dev] Usuário {} criado: {} / {}", perfil, email, SENHA_DEV);
    }
}
