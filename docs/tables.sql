-- ATENÇÃO: cópia de referência. A fonte da verdade do schema são as migrations Flyway em
-- api/src/main/resources/db/migration (V1__init.sql, V2__..., ...). Não execute este arquivo manualmente.

-- =====================================================================
-- V1 - Schema inicial do DTAComp (PostgreSQL / Supabase)
--
-- Os UUIDs são gerados pela aplicação (GenerationType.UUID), por isso
-- não há DEFAULT gen_random_uuid(). Enums são persistidos como ordinal
-- (SMALLINT), conforme o mapeamento padrão do Hibernate.
-- =====================================================================

-- instituicao
CREATE TABLE instituicao (
    id    UUID PRIMARY KEY,
    nome  VARCHAR(255) NOT NULL,
    sigla VARCHAR(50)  NOT NULL
);

-- usuario
CREATE TABLE usuario (
    id             UUID PRIMARY KEY,
    nome           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    senha_hash     VARCHAR(255) NOT NULL,
    perfil         SMALLINT     NOT NULL,            -- 0: Membro | 1: Revisor | 2: Admin
    id_instituicao UUID         NOT NULL REFERENCES instituicao (id),
    id_externo     VARCHAR(255),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN (0, 1, 2))
);

CREATE INDEX idx_usuario_instituicao ON usuario (id_instituicao);

-- tag
CREATE TABLE tag (
    id   UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

-- conteudo (superclasse)
CREATE TABLE conteudo (
    id         UUID PRIMARY KEY,
    titulo     VARCHAR(255) NOT NULL,
    resumo     TEXT,
    tipo       SMALLINT     NOT NULL,                -- 0: Texto | 1: PDF
    status     SMALLINT     NOT NULL,                -- 0: Em revisão | 1: Devolvido | 2: Publicado
    data       DATE         NOT NULL,
    id_externo VARCHAR(255),
    CONSTRAINT ck_conteudo_tipo   CHECK (tipo IN (0, 1)),
    CONSTRAINT ck_conteudo_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_conteudo_status ON conteudo (status);
CREATE INDEX idx_conteudo_tipo   ON conteudo (tipo);

-- conteudo_texto (subtipo de conteudo)
CREATE TABLE conteudo_texto (
    id_conteudo UUID PRIMARY KEY REFERENCES conteudo (id) ON DELETE CASCADE,
    corpo_html  TEXT NOT NULL
);

-- conteudo_pdf (subtipo de conteudo)
CREATE TABLE conteudo_pdf (
    id_conteudo   UUID PRIMARY KEY REFERENCES conteudo (id) ON DELETE CASCADE,
    url_arquivo   VARCHAR(500) NOT NULL,
    tamanho_bytes REAL
);

-- imagem (N:1 com conteudo_texto)
CREATE TABLE imagem (
    id                UUID PRIMARY KEY,
    id_conteudo_texto UUID         NOT NULL REFERENCES conteudo_texto (id_conteudo) ON DELETE CASCADE,
    url_image         VARCHAR(500) NOT NULL,
    legenda           VARCHAR(500)
);

CREATE INDEX idx_imagem_conteudo_texto ON imagem (id_conteudo_texto);

-- conteudo_tag (N:N entre conteudo e tag)
CREATE TABLE conteudo_tag (
    id_conteudo UUID NOT NULL REFERENCES conteudo (id) ON DELETE CASCADE,
    id_tag      UUID NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (id_conteudo, id_tag)
);

-- usuario_conteudo (N:N entre usuario e conteudo, com posição do autor)
CREATE TABLE usuario_conteudo (
    id_usuario  UUID    NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    id_conteudo UUID    NOT NULL REFERENCES conteudo (id) ON DELETE CASCADE,
    idx_posicao INTEGER NOT NULL,
    PRIMARY KEY (id_usuario, id_conteudo)
);

-- revisao
CREATE TABLE revisao (
    id             UUID PRIMARY KEY,
    id_conteudo    UUID     NOT NULL REFERENCES conteudo (id) ON DELETE CASCADE,
    id_revisor     UUID     NOT NULL REFERENCES usuario (id),
    parecer_humano TEXT,
    status         SMALLINT NOT NULL,                -- 0: Em revisão | 1: Devolvido | 2: Publicado
    data_revisao   DATE,
    CONSTRAINT ck_revisao_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_revisao_conteudo ON revisao (id_conteudo);
CREATE INDEX idx_revisao_revisor  ON revisao (id_revisor);
