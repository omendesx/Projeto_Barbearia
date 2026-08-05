const $ = (s) => document.querySelector(s),
  state = { professionals: [], appointments: [], slot: null };
const labels = {
  PENDING: "Pendente",
  CONFIRMED: "Confirmado",
  IN_PROGRESS: "Em atendimento",
  COMPLETED: "Concluído",
  CANCELLED: "Cancelado",
  NO_SHOW: "Não compareceu",
};
const fmt = (v) =>
  new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(v));
const money = (v) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    v,
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
    const [me, pros, apps] = await Promise.all([
      api("/api/auth/me"),
      api("/api/portal/professionals"),
      api("/api/portal/appointments"),
    ]);
    if (me.role !== "CLIENT") throw Error();
    $("#clientName").textContent = `Olá, ${me.name}`;
    state.professionals = pros;
    state.appointments = apps;
    $("#professional").innerHTML =
      '<option value="">Selecione</option>' +
      pros.map((p) => `<option value="${p.id}">${p.name}</option>`).join("");
    const tomorrow = new Date(Date.now() + 86400000);
    $("#bookingDate").min = new Date().toISOString().slice(0, 10);
    $("#bookingDate").value = tomorrow.toISOString().slice(0, 10);
    renderAppointments();
  } catch (e) {
    location.href = "login.html";
  }
}
$("#professional").onchange = () => {
  const p = state.professionals.find(
    (x) => x.id === Number($("#professional").value),
  );
  $("#services").className = p?.services.length ? "checks" : "checks muted";
  $("#services").innerHTML =
    p?.services
      .map(
        (s) =>
          `<label><input type="checkbox" value="${s.id}"> ${s.name} · ${money(s.price)}</label>`,
      )
      .join("") || "Este profissional não possui serviços.";
  resetSlots();
};
function selectedServices() {
  return [...document.querySelectorAll("#services input:checked")].map((x) =>
    Number(x.value),
  );
}
function resetSlots() {
  state.slot = null;
  $("#slots").innerHTML = "";
  $("#bookButton").disabled = true;
}
$("#services").onchange = resetSlots;
$("#bookingDate").onchange = resetSlots;
$("#findSlots").onclick = async () => {
  const pro = $("#professional").value,
    ids = selectedServices(),
    day = $("#bookingDate").value;
  if (!pro || !ids.length || !day)
    return toast("Escolha profissional, serviço e data.");
  try {
    const qs = new URLSearchParams({
        professionalId: pro,
        serviceIds: ids.join(","),
        date: day,
      }),
      slots = await api("/api/portal/availability?" + qs);
    $("#slots").innerHTML = slots.length
      ? slots
          .map(
            (v) =>
              `<button type="button" class="slot" data-value="${v}">${v.slice(11, 16)}</button>`,
          )
          .join("")
      : '<span class="muted">Não há horários disponíveis nesta data.</span>';
  } catch (e) {
    toast(e.message);
  }
};
$("#slots").onclick = (e) => {
  const b = e.target.closest(".slot");
  if (!b) return;
  document
    .querySelectorAll(".slot")
    .forEach((x) => x.classList.toggle("selected", x === b));
  state.slot = b.dataset.value;
  $("#bookButton").disabled = false;
};
$("#bookingForm").onsubmit = async (e) => {
  e.preventDefault();
  try {
    await api("/api/portal/appointments", {
      method: "POST",
      body: JSON.stringify({
        clientId: 0,
        professionalId: Number($("#professional").value),
        serviceIds: selectedServices(),
        startTime: state.slot,
        notes: $("#notes").value || null,
      }),
    });
    toast("Agendamento realizado!");
    state.appointments = await api("/api/portal/appointments");
    renderAppointments();
    e.target.reset();
    resetSlots();
  } catch (err) {
    toast(err.message);
  }
};
function renderAppointments() {
  $("#myAppointments").innerHTML = state.appointments.length
    ? state.appointments
        .map(
          (a) =>
            `<article class="appointment"><div><span class="badge">${labels[a.status]}</span><strong>${fmt(a.startTime)} · ${a.professionalName}</strong><small>${a.services.map((s) => s.name).join(", ")} · ${money(a.totalPrice)}</small></div>${["PENDING", "CONFIRMED", "IN_PROGRESS"].includes(a.status) ? `<button data-cancel="${a.id}">Cancelar</button>` : ""}</article>`,
        )
        .join("")
    : '<p class="muted">Você ainda não possui agendamentos.</p>';
}
$("#myAppointments").onclick = async (e) => {
  const id = e.target.dataset.cancel;
  if (!id || !confirm("Cancelar este agendamento?")) return;
  try {
    await api(`/api/portal/appointments/${id}/cancel`, { method: "PATCH" });
    state.appointments = await api("/api/portal/appointments");
    renderAppointments();
    toast("Agendamento cancelado.");
  } catch (err) {
    toast(err.message);
  }
};
$("#logout").onclick = async () => {
  await fetch("/api/auth/logout", { method: "POST" });
  location.href = "login.html";
};
init();
