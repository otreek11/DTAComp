CREATE EXTENSION IF NOT EXISTS pgcrypto; -- necessário para gen_random_uuid()

-- =====================================================================
-- instituicao
-- =====================================================================
CREATE TABLE instituicao (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome  VARCHAR(255) NOT NULL,
    sigla VARCHAR(50)  NOT NULL
);

-- =====================================================================
-- usuario
-- =====================================================================
CREATE TABLE usuario (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    senha_hash     VARCHAR(255) NOT NULL,
    perfil         INTEGER NOT NULL,                 -- 0: Membro | 1: Revisor | 2: Admin
    id_instituicao UUID NOT NULL REFERENCES instituicao (id),
    id_externo     VARCHAR(255),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN (0, 1, 2))
);

CREATE INDEX idx_usuario_instituicao ON usuario (id_instituicao);

-- =====================================================================
-- tag
-- =====================================================================
CREATE TABLE tag (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL UNIQUE
);

-- =====================================================================
-- conteudo (superclasse)
-- =====================================================================
CREATE TABLE conteudo (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo     VARCHAR(255) NOT NULL,
    resumo     TEXT,
    tipo       INTEGER NOT NULL,                     -- 0: Texto | 1: PDF
    status     INTEGER NOT NULL,                     -- 0: Em revisão | 1: Devolvido | 2: Publicado
    data       TIMESTAMP WITH TIME ZONE NOT NULL,
    id_externo VARCHAR(255),
    CONSTRAINT ck_conteudo_tipo CHECK (tipo IN (0, 1)),
    CONSTRAINT ck_conteudo_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_conteudo_status ON conteudo (status);
CREATE INDEX idx_conteudo_tipo ON conteudo (tipo);

-- =====================================================================
-- conteudo_texto (subtipo de conteudo)
-- =====================================================================
CREATE TABLE conteudo_texto (
    id_conteudo UUID PRIMARY KEY REFERENCES conteudo (id) ON DELETE CASCADE,
    corpo_html  TEXT NOT NULL
);

-- =====================================================================
-- conteudo_pdf (subtipo de conteudo)
-- =====================================================================
CREATE TABLE conteudo_pdf (
    id_conteudo   UUID PRIMARY KEY REFERENCES conteudo (id) ON DELETE CASCADE,
    url_arquivo   VARCHAR(500) NOT NULL,
    tamanho_bytes REAL
);

-- =====================================================================
-- imagem (N:1 com conteudo_texto)
-- =====================================================================
CREATE TABLE imagem (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conteudo_texto UUID NOT NULL REFERENCES conteudo_texto (id_conteudo) ON DELETE CASCADE,
    url_image         VARCHAR(500) NOT NULL,
    legenda           VARCHAR(500)
);

CREATE INDEX idx_imagem_conteudo_texto ON imagem (id_conteudo_texto);

-- =====================================================================
-- conteudo_tag (associação N:N entre conteudo e tag)
-- =====================================================================
CREATE TABLE conteudo_tag (
    id_conteudo UUID NOT NULL REFERENCES conteudo (id) ON DELETE CASCADE,
    id_tag      UUID NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (id_conteudo, id_tag)
);

-- =====================================================================
-- usuario_conteudo (associação N:N entre usuario e conteudo, com atributo)
-- =====================================================================
CREATE TABLE usuario_conteudo (
    id_usuario  UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    id_conteudo UUID NOT NULL REFERENCES conteudo (id) ON DELETE CASCADE,
    idx_posicao INTEGER NOT NULL, -- posição do autor na autoria do conteúdo
    PRIMARY KEY (id_usuario, id_conteudo)
);

-- =====================================================================
-- revisao
-- =====================================================================
CREATE TABLE revisao (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conteudo    UUID NOT NULL REFERENCES conteudo (id) ON DELETE CASCADE,
    id_revisor     UUID NOT NULL REFERENCES usuario (id),
    sugestoes_ia   TEXT,
    parecer_humano TEXT,
    status         INTEGER NOT NULL
);

CREATE INDEX idx_revisao_conteudo ON revisao (id_conteudo);
CREATE INDEX idx_revisao_revisor ON revisao (id_revisor);

schema.sql
Exibindo schema.sql.
