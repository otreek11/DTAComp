/** variant: "primary" | "danger" | undefined. */
export function Button({ variant, block, className = "", ...props }) {
  const cls = [
    "dta-btn",
    variant === "primary" && "dta-btn--primary",
    variant === "danger" && "dta-btn--danger",
    block && "dta-btn--block",
    className,
  ].filter(Boolean).join(" ");
  return <button type="button" className={cls} {...props} />;
}
