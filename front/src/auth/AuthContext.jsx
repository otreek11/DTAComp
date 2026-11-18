import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { EVENTO_LOGOUT, fetchMe, getToken, getUsuarioSalvo, limparSessao, salvarSessao } from "../services/api.js";

/**
 * usuario: { id, nome, email, perfil: "membro" | "revisor" | "admin", instituicao } | null
 * pronto: false enquanto o token salvo ainda está sendo validado em GET /auth/me.
 */
const AuthContext = createContext({
  usuario: null,
  pronto: true,
  entrar: () => {},
  sair: () => {},
  atualizarUsuario: () => {},
  temPerfil: () => false,
});

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => (getToken() ? getUsuarioSalvo() : null));
  const [pronto, setPronto] = useState(() => !getToken());

  // Valida o token salvo ao abrir o app (expirado/revogado → sessão limpa).
  useEffect(() => {
    if (!getToken()) return undefined;
    let ativo = true;
    fetchMe()
      .then((me) => {
        if (!ativo) return;
        salvarSessao(me);
        setUsuario(me);
      })
      .catch((e) => {
        if (!ativo) return;
        // Só derruba a sessão se o servidor recusou o token; falha de rede mantém o cache.
        if (e.status === 401 || e.status === 403) {
          limparSessao();
          setUsuario(null);
        }
      })
      .finally(() => ativo && setPronto(true));
    return () => {
      ativo = false;
    };
  }, []);

  // Qualquer 401 em chamada autenticada encerra a sessão (ver services/api.js).
  useEffect(() => {
    const aoSair = () => setUsuario(null);
    window.addEventListener(EVENTO_LOGOUT, aoSair);
    return () => window.removeEventListener(EVENTO_LOGOUT, aoSair);
  }, []);

  /** Recebe o AuthResponse de /auth/login ou /auth/register. */
  const entrar = useCallback((resposta) => {
    salvarSessao(resposta, resposta?.token);
    const dados = { ...(resposta || {}) };
    delete dados.token;
    setUsuario(dados);
  }, []);

  const sair = useCallback(() => {
    limparSessao();
    setUsuario(null);
  }, []);

  const atualizarUsuario = useCallback((parcial) => {
    setUsuario((atual) => {
      if (!atual) return atual;
      const novo = { ...atual, ...parcial };
      salvarSessao(novo);
      return novo;
    });
  }, []);

  const temPerfil = useCallback((...perfis) => Boolean(usuario && perfis.includes(usuario.perfil)), [usuario]);

  const valor = useMemo(
    () => ({ usuario, pronto, entrar, sair, atualizarUsuario, temPerfil }),
    [usuario, pronto, entrar, sair, atualizarUsuario, temPerfil],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
