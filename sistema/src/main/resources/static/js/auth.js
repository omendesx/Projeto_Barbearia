const $ = (s) => document.querySelector(s),
  message = $("#authMessage");
document.querySelectorAll("[data-tab]").forEach(
  (b) =>
    (b.onclick = () => {
      document
        .querySelectorAll("[data-tab]")
        .forEach((x) => x.classList.toggle("active", x === b));
      $("#loginForm").hidden = b.dataset.tab !== "login";
      $("#registerForm").hidden = b.dataset.tab !== "register";
      message.textContent = "";
    }),
);
async function send(path, body) {
  message.textContent = "Aguarde…";
  message.className = "auth-message";
  try {
    const r = await fetch(path, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      }),
      data = await r.json().catch(() => ({}));
    if (!r.ok) throw Error(data.message || "Não foi possível continuar");
    location.href = data.role === "ADMIN" ? "paineladmin.html" : data.role === "PROFESSIONAL" ? "barbeiro.html" : "cliente.html";
  } catch (e) {
    message.textContent = e.message;
    message.className = "auth-message error";
  }
}
$("#loginForm").onsubmit = (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  send("/api/auth/login", {
    email: f.get("email"),
    password: f.get("password"),
  });
};
$("#registerForm").onsubmit = (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  send("/api/auth/register", {
    name: f.get("name"),
    email: f.get("email"),
    phone: f.get("phone").trim(),
    password: f.get("password"),
  });
};
