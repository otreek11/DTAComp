import { NavContext } from "./NavContext.jsx";

export function NavProvider({ link, pathname, children }) {
  return <NavContext.Provider value={{ Link: link, pathname }}>{children}</NavContext.Provider>;
}
