import { NavContext } from "./NavContext.jsx";

export function NavProvider({ link, pathname, search = "", navigate, children }) {
  return <NavContext.Provider value={{ Link: link, pathname, search, navigate }}>{children}</NavContext.Provider>;
}
