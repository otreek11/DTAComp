import { createContext, useContext } from "react";

/**
 * Navegação plugável. Por padrão os links são <a href> comuns. Para usar um router
 * (ex.: react-router), envolva o app em <NavProvider link={...} pathname={...}>.
 */
export const NavContext = createContext({
  Link: (props) => <a {...props} />,
  pathname: typeof window !== "undefined" ? window.location.pathname : "/",
});

export const useNav = () => useContext(NavContext);
