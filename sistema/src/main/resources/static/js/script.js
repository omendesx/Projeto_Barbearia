const state = {
  clients: [],
  services: [],
  professionals: [],
  appointments: [],
  modal: null,
  editing: null,
};
const $ = (s) => document.querySelector(s);
const statusLabels = {
  PENDING: "Pendente",
  CONFIRMED: "Confirmado",
  IN_PROGRESS: "Em atendimento",
  COMPLETED: "Concluído",
  CANCELLED: "Cancelado",
  NO_SHOW: "Não compareceu",
};
const transitions = {
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["IN_PROGRESS", "CANCELLED", "NO_SHOW"],
  IN_PROGRESS: ["COMPLETED", "CANCELLED"],
  COMPLETED: [],
  CANCELLED: [],
  NO_SHOW: [],
};
const titles = {
  dashboard: "Visão geral",
  appointments: "Agenda",
  clients: "Clientes",
  professionals: "Profissionais",
  services: "Serviços",
};
const money = (v) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    v || 0,
  );
const date = (v) =>
  v
    ? new Intl.DateTimeFormat("pt-BR", { dateStyle: "short" }).format(
        new Date(v),
      )
    : "—";
const datetime = (v) =>
  v
    ? new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short",
      }).format(new Date(v))
    : "—";
const time = (v) =>
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

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(
      body.message ||
        body.error ||
        `Erro ${response.status} ao comunicar com o servidor`,
    );
  }
  return response.status === 204 ? null : response.json();
}
function toast(message, error = false) {
  const el = $("#toast");
  el.textContent = message;
  el.className = `toast show${error ? " error" : ""}`;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (el.className = "toast"), 3200);
}

async function loadAll() {
  try {
    const [clients, services, professionals, appointments] = await Promise.all([
      api("/api/clients"),
      api("/api/services?onlyActive=false"),
      api("/api/professionals?onlyActive=false"),
      api("/api/appointments"),
    ]);
    Object.assign(state, { clients, services, professionals, appointments });
    renderAll();
  } catch (e) {
    toast(e.message, true);
    document
      .querySelectorAll(".loading-block")
      .forEach(
        (el) =>
          (el.innerHTML =
            '<div class="empty">Não foi possível carregar os dados.</div>'),
      );
  }
}
function renderAll() {
  renderDashboard();
  renderAppointments();
  renderClients();
  renderProfessionals();
  renderServices();
}
function renderDashboard() {
  const todayKey = new Date().toDateString(),
    today = state.appointments.filter(
      (a) => new Date(a.startTime).toDateString() === todayKey,
    );
  $("#metricToday").textContent = today.length;
  $("#metricCompleted").textContent = state.appointments.filter(
    (a) => a.status === "COMPLETED",
  ).length;
  $("#metricClients").textContent = state.clients.filter(
    (c) => c.active !== false,
  ).length;
  $("#metricProfessionals").textContent = state.professionals.filter(
    (p) => p.active !== false,
  ).length;
  const upcoming = today
    .filter(
      (a) =>
        new Date(a.endTime) > new Date() &&
        !["CANCELLED", "NO_SHOW"].includes(a.status),
    )
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
  $("#todayAppointments").innerHTML = upcoming.length
    ? upcoming
        .map(
          (a) =>
            `<div class="schedule-item"><div class="schedule-time">${time(a.startTime)}<small>${time(a.endTime)}</small></div><div class="person">${esc(a.clientName)}<small>Cliente</small></div><div class="person">${esc(a.professionalName)}<small>Profissional</small></div><div>${esc(a.services.map((s) => s.name).join(", "))}</div><span class="badge ${a.status}">${statusLabels[a.status]}</span></div>`,
        )
        .join("")
    : '<div class="empty">Nenhum atendimento restante para hoje.</div>';
}
function renderAppointments() {
  const q = $("#appointmentSearch").value.toLowerCase(),
    status = $("#appointmentStatusFilter").value;
  const rows = state.appointments.filter(
    (a) =>
      (!q ||
        a.clientName.toLowerCase().includes(q) ||
        a.professionalName.toLowerCase().includes(q)) &&
      (!status || a.status === status),
  );
  $("#appointmentsTable").innerHTML = rows.length
    ? rows
        .map(
          (a) =>
            `<tr><td><strong>${datetime(a.startTime)}</strong><small>até ${time(a.endTime)}</small></td><td>${esc(a.clientName)}</td><td>${esc(a.professionalName)}</td><td>${esc(a.services.map((s) => s.name).join(", "))}</td><td><strong>${money(a.totalPrice)}</strong></td><td><span class="badge ${a.status}">${statusLabels[a.status]}</span></td><td>${transitions[a.status].length ? `<select class="status-select" data-id="${a.id}"><option value="">Alterar…</option>${transitions[a.status].map((s) => `<option value="${s}">${statusLabels[s]}</option>`).join("")}</select>` : ""}${!["COMPLETED", "CANCELLED", "NO_SHOW"].includes(a.status) ? `<button class="icon-button edit" data-type="appointment" data-id="${a.id}" title="Reagendar">✎</button>` : ""}</td></tr>`,
        )
        .join("")
    : '<tr><td colspan="7"><div class="empty">Nenhum agendamento encontrado.</div></td></tr>';
}
function renderClients() {
  const q = $("#clientSearch").value.toLowerCase(),
    rows = state.clients.filter((c) => c.name.toLowerCase().includes(q));
  $("#clientsTable").innerHTML = rows.length
    ? rows
        .map(
          (c) =>
            `<tr><td><strong>${esc(c.name)}</strong><small>${esc(c.email)}</small></td><td>${esc(c.phone || "—")}</td><td><span class="badge ${c.active === false ? "inactive" : "active"}">${c.active === false ? "Inativo" : "Ativo"}</span></td><td>${date(c.dateRegister)}</td><td><button class="icon-button edit" data-type="client" data-id="${c.id}">✎</button>${c.active !== false ? `<button class="icon-button deactivate" data-type="client" data-id="${c.id}">Desativar</button>` : ""}</td></tr>`,
        )
        .join("")
    : '<tr><td colspan="5"><div class="empty">Nenhum cliente encontrado.</div></td></tr>';
}
function renderProfessionals() {
  $("#professionalsGrid").innerHTML = state.professionals.length
    ? state.professionals
        .map(
          (p) =>
            `<article class="entity-card"><div class="entity-card-head"><div><h3>${esc(p.name)}</h3><p>${esc(p.email)} · ${esc(p.phone || "Sem telefone")}</p></div><span class="badge ${p.active === false ? "inactive" : "active"}">${p.active === false ? "Inativo" : "Ativo"}</span></div><div class="tags">${p.services.length ? p.services.map((s) => `<span class="tag">${esc(s.name)}</span>`).join("") : '<span class="tag">Sem serviços</span>'}</div><div class="card-actions"><button class="icon-button edit" data-type="professional" data-id="${p.id}">Editar</button>${p.active !== false ? `<button class="icon-button deactivate" data-type="professional" data-id="${p.id}">Desativar</button>` : ""}</div></article>`,
        )
        .join("")
    : '<div class="empty">Nenhum profissional cadastrado.</div>';
}
function renderServices() {
  $("#servicesGrid").innerHTML = state.services.length
    ? state.services
        .map(
          (s) =>
            `<article class="entity-card"><div class="entity-card-head"><div><h3>${esc(s.name)}</h3><p>${esc(s.description || "Sem descrição")}</p></div><span class="badge ${s.active === false ? "inactive" : "active"}">${s.active === false ? "Inativo" : "Ativo"}</span></div><div class="price">${money(s.price)}</div><p>${s.durationMinutes} minutos</p><div class="card-actions"><button class="icon-button edit" data-type="service" data-id="${s.id}">Editar</button>${s.active !== false ? `<button class="icon-button deactivate" data-type="service" data-id="${s.id}">Desativar</button>` : ""}</div></article>`,
        )
        .join("")
    : '<div class="empty">Nenhum serviço cadastrado.</div>';
}

const field = (label, name, type = "text", value = "", extra = "", full = "") =>
  `<div class="field ${full}"><label for="${name}">${label}</label><input id="${name}" name="${name}" type="${type}" value="${esc(value)}" ${extra}></div>`;
function openModal(type, item = null) {
  state.modal = type;
  state.editing = item;
  let html = "",
    title = "";
  if (type === "client") {
    title = item ? "Editar cliente" : "Novo cliente";
    html =
      field("Nome", "name", "text", item?.name, 'required maxlength="100"') +
      field(
        "E-mail",
        "email",
        "email",
        item?.email,
        'required maxlength="150"',
      ) +
      field("Telefone", "phone", "tel", item?.phone, 'required maxlength="30" placeholder="(00) 00000-0000"');
  }
  if (type === "service") {
    title = item ? "Editar serviço" : "Novo serviço";
    html =
      field("Nome", "name", "text", item?.name, 'required maxlength="100"') +
      field(
        "Preço",
        "price",
        "number",
        item?.price,
        'required min="0.01" step="0.01"',
      ) +
      field(
        "Duração em minutos",
        "durationMinutes",
        "number",
        item?.durationMinutes,
        'required min="5" max="1440"',
      ) +
      `<div class="field full"><label for="description">Descrição</label><textarea id="description" name="description" maxlength="500">${esc(item?.description)}</textarea></div>`;
  }
  if (type === "professional") {
    title = item ? "Editar profissional" : "Novo profissional";
    const selected = new Set(item?.services.map((s) => s.id) || []);
    html =
      field("Nome", "name", "text", item?.name, 'required maxlength="100"') +
      field(
        "E-mail",
        "email",
        "email",
        item?.email,
        'required maxlength="150"',
      ) +
      field("Telefone", "phone", "tel", item?.phone, 'maxlength="30"') +
      `<div class="field full"><label>Serviços realizados</label><div class="check-list">${
        state.services
          .filter((s) => s.active !== false || selected.has(s.id))
          .map(
            (s) =>
              `<label><input type="checkbox" name="serviceIds" value="${s.id}" ${selected.has(s.id) ? "checked" : ""}> ${esc(s.name)}</label>`,
          )
          .join("") || "Cadastre um serviço primeiro."
      }</div></div>`;
  }
  if (type === "appointment") {
    title = item ? "Reagendar atendimento" : "Novo agendamento";
    const future = new Date(Date.now() + 3600000);
    future.setMinutes(Math.ceil(future.getMinutes() / 15) * 15, 0, 0);
    const value =
      item?.startTime?.slice(0, 16) ||
      new Date(future.getTime() - future.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);
    html = `<div class="field"><label for="clientId">Cliente</label><select id="clientId" name="clientId" required><option value="">Selecione</option>${state.clients
      .filter((c) => c.active !== false || c.id === item?.clientId)
      .map(
        (c) =>
          `<option value="${c.id}" ${c.id === item?.clientId ? "selected" : ""}>${esc(c.name)}</option>`,
      )
      .join(
        "",
      )}</select></div><div class="field"><label for="professionalId">Profissional</label><select id="professionalId" name="professionalId" required><option value="">Selecione</option>${state.professionals
      .filter((p) => p.active !== false || p.id === item?.professionalId)
      .map(
        (p) =>
          `<option value="${p.id}" ${p.id === item?.professionalId ? "selected" : ""}>${esc(p.name)}</option>`,
      )
      .join(
        "",
      )}</select></div>${field("Data e hora", "startTime", "datetime-local", value, "required", "full")}<div class="field full"><label>Serviços</label><div class="check-list" id="appointmentServices"></div></div><div class="field full"><label for="notes">Observações</label><textarea id="notes" name="notes" maxlength="500">${esc(item?.notes)}</textarea></div>`;
  }
  $("#modalTitle").textContent = title;
  $("#formFields").innerHTML = html;
  $("#modalBackdrop").hidden = false;
  document.body.style.overflow = "hidden";
  if (type === "appointment") {
    updateAppointmentServices(item?.services.map((s) => s.id) || []);
    $("#professionalId").addEventListener("change", () =>
      updateAppointmentServices([]),
    );
  }
}
function updateAppointmentServices(selected) {
  const p = state.professionals.find(
    (x) => x.id === Number($("#professionalId").value),
  );
  $("#appointmentServices").innerHTML = p?.services.length
    ? p.services
        .filter((s) => s.active !== false || selected.includes(s.id))
        .map(
          (s) =>
            `<label><input type="checkbox" name="serviceIds" value="${s.id}" ${selected.includes(s.id) ? "checked" : ""}> ${esc(s.name)} · ${money(s.price)}</label>`,
        )
        .join("")
    : "Selecione um profissional para ver os serviços.";
}
function closeModal() {
  $("#modalBackdrop").hidden = true;
  document.body.style.overflow = "";
  state.modal = null;
  state.editing = null;
}
async function submitForm(event) {
  event.preventDefault();
  const f = new FormData(event.target),
    type = state.modal,
    item = state.editing;
  let body,
    path,
    method = item ? "PUT" : "POST";
  if (type === "client") {
    body = {
      name: f.get("name").trim(),
      email: f.get("email").trim(),
      phone: f.get("phone").trim(),
      active: item?.active ?? true,
    };
    path = `/api/clients${item ? "/" + item.id : ""}`;
  }
  if (type === "service") {
    body = {
      name: f.get("name").trim(),
      description: f.get("description").trim(),
      price: Number(f.get("price")),
      durationMinutes: Number(f.get("durationMinutes")),
      active: item?.active ?? true,
    };
    path = `/api/services${item ? "/" + item.id : ""}`;
  }
  if (type === "professional") {
    body = {
      name: f.get("name").trim(),
      email: f.get("email").trim(),
      phone: f.get("phone").trim(),
      active: item?.active ?? true,
      serviceIds: f.getAll("serviceIds").map(Number),
    };
    path = `/api/professionals${item ? "/" + item.id : ""}`;
  }
  if (type === "appointment") {
    const ids = f.getAll("serviceIds").map(Number);
    if (!ids.length) return toast("Selecione ao menos um serviço.", true);
    body = {
      clientId: Number(f.get("clientId")),
      professionalId: Number(f.get("professionalId")),
      serviceIds: ids,
      startTime: f.get("startTime"),
      notes: f.get("notes").trim() || null,
    };
    path = `/api/appointments${item ? "/" + item.id : ""}`;
  }
  const button = $("#submitButton");
  button.disabled = true;
  button.textContent = "Salvando…";
  try {
    await api(path, { method, body: JSON.stringify(body) });
    closeModal();
    toast(
      item
        ? "Dados atualizados com sucesso."
        : "Cadastro realizado com sucesso.",
    );
    await loadAll();
  } catch (e) {
    toast(e.message, true);
  } finally {
    button.disabled = false;
    button.textContent = "Salvar";
  }
}
async function deactivate(type, id) {
  if (!confirm("Deseja realmente desativar este registro?")) return;
  const path = {
    client: "clients",
    professional: "professionals",
    service: "services",
  }[type];
  try {
    await api(`/api/${path}/${id}`, { method: "DELETE" });
    toast("Registro desativado.");
    await loadAll();
  } catch (e) {
    toast(e.message, true);
  }
}
async function changeStatus(id, status) {
  if (!status) return;
  try {
    await api(`/api/appointments/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
    toast("Status atualizado.");
    await loadAll();
  } catch (e) {
    toast(e.message, true);
  }
}
function navigate(view) {
  document
    .querySelectorAll(".view")
    .forEach((v) => v.classList.remove("active"));
  $(`#${view}View`).classList.add("active");
  document
    .querySelectorAll(".nav-item")
    .forEach((n) => n.classList.toggle("active", n.dataset.view === view));
  $("#pageTitle").textContent = titles[view];
  $(".sidebar").classList.remove("open");
}

document.addEventListener("click", (e) => {
  const open = e.target.closest("[data-open]"),
    go = e.target.closest("[data-go]"),
    edit = e.target.closest(".edit"),
    off = e.target.closest(".deactivate");
  if (open) openModal(open.dataset.open);
  if (go) navigate(go.dataset.go);
  if (edit) {
    const collection =
      edit.dataset.type === "appointment"
        ? "appointments"
        : edit.dataset.type === "client"
          ? "clients"
          : edit.dataset.type === "professional"
            ? "professionals"
            : "services";
    openModal(
      edit.dataset.type,
      state[collection].find((x) => x.id === Number(edit.dataset.id)),
    );
  }
  if (off) deactivate(off.dataset.type, off.dataset.id);
});
document
  .querySelectorAll(".nav-item")
  .forEach((b) => b.addEventListener("click", () => navigate(b.dataset.view)));
$("#appointmentSearch").addEventListener("input", renderAppointments);
$("#appointmentStatusFilter").addEventListener("change", renderAppointments);
$("#clientSearch").addEventListener("input", renderClients);
$("#appointmentsTable").addEventListener("change", (e) => {
  if (e.target.matches(".status-select"))
    changeStatus(e.target.dataset.id, e.target.value);
});
$("#entityForm").addEventListener("submit", submitForm);
$("#modalClose").addEventListener("click", closeModal);
$("#modalCancel").addEventListener("click", closeModal);
$("#modalBackdrop").addEventListener("click", (e) => {
  if (e.target === e.currentTarget) closeModal();
});
$("#menuButton").addEventListener("click", () =>
  $(".sidebar").classList.toggle("open"),
);
const now = new Date();
$("#today").innerHTML =
  `<strong>${new Intl.DateTimeFormat("pt-BR", { weekday: "long" }).format(now)}</strong>${new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "long", year: "numeric" }).format(now)}`;
$("#adminLogout").addEventListener("click", async () => {
  await fetch("/api/auth/logout", { method: "POST" });
  location.href = "login.html";
});
loadAll();
