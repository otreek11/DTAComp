import { createContext, useContext } from "react";

/**
 * Navegação plugável. Por padrão os links são <a href> comuns. Para usar um router
 * (ex.: react-router), envolva o app em <NavProvider link={...} pathname={...} navigate={...}>.
 * search: query string atual (ex.: "?editar=123").
 */
export const NavContext = createContext({
  Link: (props) => <a {...props} />,
  pathname: typeof window !== "undefined" ? window.location.pathname : "/",
  search: typeof window !== "undefined" ? window.location.search : "",
  navigate: (path) => {
    if (typeof window !== "undefined") window.location.assign(path);
  },
});

export const useNav = () => useContext(NavContext);
