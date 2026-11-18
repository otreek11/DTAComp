/** Itens públicos (visitante). */
export const defaultNav = [{ label: "Explorar", href: "/" }];

/** Menu principal conforme o perfil do usuário autenticado ("membro" | "revisor" | "admin"). */
export function navParaUsuario(usuario) {
  if (!usuario) return defaultNav;
  const itens = [
    { label: "Explorar", href: "/" },
    { label: "Publicar", href: "/publicar" },
    { label: "Meus trabalhos", href: "/dashboard" },
  ];
  if (usuario.perfil === "revisor") itens.push({ label: "Revisão", href: "/revisao" });
  if (usuario.perfil === "admin") itens.push({ label: "Administração", href: "/admin" });
  return itens;
}
