const $ = (s) => document.querySelector(s),
  state = { profile: null, appointments: [] };
const labels = {
  PENDING: "Pendente",
  CONFIRMED: "Confirmado",
  IN_PROGRESS: "Em atendimento",
  COMPLETED: "Concluído",
  CANCELLED: "Cancelado",
  NO_SHOW: "Não compareceu",
};
const actions = {
  PENDING: [
    ["CONFIRMED", "Confirmar"],
    ["CANCELLED", "Cancelar"],
  ],
  CONFIRMED: [
    ["IN_PROGRESS", "Iniciar"],
    ["NO_SHOW", "Não compareceu"],
    ["CANCELLED", "Cancelar"],
  ],
  IN_PROGRESS: [
    ["COMPLETED", "Concluir"],
    ["CANCELLED", "Cancelar"],
  ],
  COMPLETED: [],
  CANCELLED: [],
  NO_SHOW: [],
};
const sameDay = (a, b) =>
    new Date(a).toDateString() === new Date(b).toDateString(),
  fmtDate = (v) =>
    new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "short" }).format(
      new Date(v),
    ),
  fmtTime = (v) =>
    new Intl.DateTimeFormat("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(v));
const esc = (v) =>
  String(v ?? "").replace(
    /[&<>'"]/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[
        c
      ],
  );
async function api(path, opt = {}) {
  const r = await fetch(path, {
      ...opt,
      headers: { "Content-Type": "application/json" },
    }),
    d = await r.json().catch(() => null);
  if (!r.ok) throw Error(d?.message || "Não foi possível continuar");
  return d;
}
function toast(m) {
  $("#toast").textContent = m;
  $("#toast").className = "show";
  setTimeout(() => ($("#toast").className = ""), 2800);
}
async function init() {
  try {
    const [me, profile, appointments] = await Promise.all([
      api("/api/auth/me"),
      api("/api/barber/profile"),
      api("/api/barber/appointments"),
    ]);
    if (me.role !== "PROFESSIONAL") throw Error();
    state.profile = profile;
    state.appointments = appointments;
    fillProfile();
    render();
  } catch (e) {
    location.href = "login.html";
  }
}
function fillProfile() {
  const p = state.profile,
    initial = p.name.charAt(0).toUpperCase();
  $("#barberName").textContent = p.name;
  $("#avatar").textContent = initial;
  $("#profileAvatar").textContent = initial;
  $("#profileName").textContent = p.name;
  $("#profileEmail").textContent = p.email;
  $("#profilePhone").textContent = p.phone || "Telefone não informado";
  $("#profileServices").innerHTML = p.services.length
    ? p.services.map((s) => `<span class="tag">${esc(s.name)}</span>`).join("")
    : '<span class="tag">Nenhum serviço vinculado</span>';
  $("#greeting").textContent = `Bom trabalho, ${p.name.split(" ")[0]}.`;
}
function appointmentCard(a) {
  const deleteButton = ["COMPLETED", "CANCELLED", "NO_SHOW"].includes(a.status) ? `<button data-delete="${a.id}">Excluir</button>` : "";
  return `<article class="appointment"><div class="date"><strong>${fmtTime(a.startTime)}</strong><small>${fmtDate(a.startTime)} · até ${fmtTime(a.endTime)}</small></div><div class="client"><strong>${esc(a.clientName)}</strong><small>${esc(a.notes || "Sem observações")}</small></div><div class="services">${esc(a.services.map((s) => s.name).join(", "))}<small>${a.services.reduce((n, s) => n + s.durationMinutes, 0)} minutos</small></div><div class="actions"><span class="badge">${labels[a.status]}</span>${actions[a.status].map(([status, label]) => `<button data-id="${a.id}" data-status="${status}">${label}</button>`).join("")}${deleteButton}</div></article>`;
}
function render() {
  const now = new Date(),
    today = state.appointments
      .filter((a) => sameDay(a.startTime, now))
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime)),
    active = today.filter((a) => !["CANCELLED", "NO_SHOW"].includes(a.status));
  $("#todayCount").textContent = active.length;
  $("#doneCount").textContent = today.filter(
    (a) => a.status === "COMPLETED",
  ).length;
  const next = active.find((a) => new Date(a.endTime) > now);
  $("#nextTime").textContent = next ? fmtTime(next.startTime) : "—";
  $("#todayList").innerHTML = today.length
    ? today.map(appointmentCard).join("")
    : '<div class="empty">Nenhum atendimento agendado para hoje.</div>';
  renderAgenda();
}
function renderAgenda() {
  const status = $("#statusFilter").value,
    rows = state.appointments.filter((a) => !status || a.status === status);
  $("#agendaList").innerHTML = rows.length
    ? rows.map(appointmentCard).join("")
    : '<div class="empty">Nenhum atendimento encontrado.</div>';
}
async function updateStatus(id, status) {
  try {
    await api(`/api/barber/appointments/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
    state.appointments = await api("/api/barber/appointments");
    render();
    toast("Atendimento atualizado.");
  } catch (e) {
    toast(e.message);
  }
}
async function deleteAppointment(id) {
  if (!confirm("Excluir este agendamento da sua agenda?")) return;
  try {
    await api(`/api/barber/appointments/${id}`, { method: "DELETE" });
    state.appointments = await api("/api/barber/appointments");
    render(); toast("Agendamento excluído.");
  } catch (e) { toast(e.message); }
}
document.addEventListener("click", (e) => {
  const action = e.target.closest("[data-status]");
  const deleteButton = e.target.closest("[data-delete]");
  if (action) updateStatus(action.dataset.id, action.dataset.status);
  if (deleteButton) deleteAppointment(deleteButton.dataset.delete);
});
$("#statusFilter").onchange = renderAgenda;
document.querySelectorAll("nav button").forEach(
  (b) =>
    (b.onclick = () => {
      document
        .querySelectorAll("nav button")
        .forEach((x) => x.classList.toggle("active", x === b));
      document
        .querySelectorAll(".view")
        .forEach((v) => v.classList.remove("active"));
      $(`#${b.dataset.view}View`).classList.add("active");
      $("#title").textContent = {
        today: "Visão do dia",
        agenda: "Minha agenda",
        profile: "Meu perfil",
      }[b.dataset.view];
      $("aside").classList.remove("open");
    }),
);
$("#menu").onclick = () => $("aside").classList.toggle("open");
$("#logout").onclick = async () => {
  await fetch("/api/auth/logout", { method: "POST" });
  location.href = "login.html";
};
setInterval(
  () =>
    ($("#clock").textContent = new Intl.DateTimeFormat("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date())),
  1000,
);
init();
