const $ = (id) => document.getElementById(id);

let currentUser = null;
let malls = [];
let availableSlots = [];
let myBookings = [];

const api = async (url, options = {}) => {
  const response = await fetch(url, {
    credentials: "same-origin",
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {})
    }
  });

  const text = await response.text();
  let data = {};

  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { message: text };
  }

  if (!response.ok) {
    throw new Error(
      data.error || data.message || `Request failed (${response.status})`
    );
  }

  return data;
};

const post = (url, body) =>
  api(url, {
    method: "POST",
    body: JSON.stringify(body)
  });

function showMessage(id, message, type = "") {
  const element = $(id);
  if (!element) return;

  element.textContent = message || "";
  element.className = `message ${type}`.trim();
}

function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;"
  })[char]);
}

function showPage(page) {
  $("authPage").hidden = page !== "auth";
  $("userDashboard").hidden = page !== "user";
  $("adminDashboard").hidden = page !== "admin";
}

function showAuthTab(tab) {
  const login = tab === "login";

  $("loginForm").hidden = !login;
  $("registerForm").hidden = login;
  $("loginTab").classList.toggle("active", login);
  $("registerTab").classList.toggle("active", !login);

  showMessage("loginMessage", "");
  showMessage("registerMessage", "");
}

function dateValue(offsetMinutes = 0) {
  const date = new Date(Date.now() + offsetMinutes * 60000);
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 16);
}

function setDefaultTimes() {
  $("startTime").value = dateValue(15);
  $("endTime").value = dateValue(135);
}

function validTimeRange() {
  const startValue = $("startTime").value;
  const endValue = $("endTime").value;

  if (!startValue || !endValue) {
    showMessage("slotMessage", "Please select both entry and exit times.", "error");
    return false;
  }

  const start = new Date(startValue).getTime();
  const end = new Date(endValue).getTime();

  if (!Number.isFinite(start) || !Number.isFinite(end)) {
    showMessage("slotMessage", "Please select valid entry and exit times.", "error");
    return false;
  }

  if (start <= Date.now()) {
    showMessage("slotMessage", "Entry time must be in the future.", "error");
    return false;
  }

  if (end <= start) {
    showMessage("slotMessage", "Exit time must be after entry time.", "error");
    return false;
  }

  return true;
}

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? escapeHTML(value)
    : date.toLocaleString([], {
        dateStyle: "medium",
        timeStyle: "short"
      });
}

function slotId(slot) {
  return slot.id ?? slot._id ?? "";
}

function mallId(mall) {
  return mall.id ?? mall._id ?? "";
}

function slotName(slot) {
  return slot.slotNumber ?? slot.name ?? slot.number ?? slotId(slot);
}

function mallName(mall) {
  return mall.name ?? "Parking location";
}

function selectedMallId() {
  return $("mallSelect").value;
}

async function loadMalls() {
  malls = await api("/api/malls");

  if (!Array.isArray(malls)) malls = [];

  const options =
    '<option value="">Select a mall or location</option>' +
    malls.map(m =>
      `<option value="${escapeHTML(mallId(m))}">${escapeHTML(mallName(m))}</option>`
    ).join("");

  $("mallSelect").innerHTML = options;
  $("adminMallSelect").innerHTML = options;

  $("locationCount").textContent = malls.length;
  $("adminMallCount").textContent = malls.length;

  renderAdminMalls();
}

function renderAdminMalls() {
  $("adminMalls").innerHTML = malls.length
    ? malls.map(m => `
        <article class="booking-card">
          <h3>${escapeHTML(mallName(m))}</h3>
          <p><strong>Address:</strong> ${escapeHTML(m.address || "Not provided")}</p>
          <p><strong>Location ID:</strong> ${escapeHTML(mallId(m))}</p>
        </article>
      `).join("")
    : "<p>No parking locations have been added yet.</p>";
}

async function searchSlots() {
  if (!selectedMallId()) {
    showMessage("slotMessage", "Please select a mall or location.", "error");
    return;
  }

  if (!validTimeRange()) return;

  const id = encodeURIComponent(selectedMallId());
  const start = encodeURIComponent($("startTime").value);
  const end = encodeURIComponent($("endTime").value);

  showMessage("slotMessage", "Searching for available slots...");
  $("slotsGrid").innerHTML = "";

  try {
    const result = await api(
      `/api/malls/${id}/slots?start=${start}&end=${end}`
    );

    availableSlots = Array.isArray(result) ? result : [];
    $("availableCount").textContent = availableSlots.length;

    if (!availableSlots.length) {
      $("slotsGrid").innerHTML =
        "<p>No available slots. Try another time or location.</p>";
      showMessage("slotMessage", "No available slots found.");
      return;
    }

    showMessage(
      "slotMessage",
      `${availableSlots.length} available slot(s) found.`,
      "success"
    );

    $("slotsGrid").innerHTML = availableSlots.map(slot => `
      <article class="slot-card">
        <span class="status">Available</span>
        <h3>${escapeHTML(slotName(slot))}</h3>
        <p>Reserve this slot for your selected time.</p>
        <button class="primary-btn" type="button"
          data-book-slot="${escapeHTML(slotId(slot))}">
          Book this slot
        </button>
      </article>
    `).join("");
  } catch (error) {
    showMessage("slotMessage", error.message, "error");
  }
}

async function bookSlot(id) {
  if (!currentUser) {
    showMessage("slotMessage", "Please log in first.", "error");
    return;
  }

  const selectedId = selectedMallId();

  if (!selectedId) {
    showMessage("slotMessage", "Please select a mall first.", "error");
    return;
  }

  if (!validTimeRange()) return;

  const vehicleType = $("vehicleType").value;
  const vehicleNumber = $("vehicleNumber").value.trim();
  const vehicleModel = $("vehicleModel").value.trim();
  const vehicleColor = $("vehicleColor").value.trim();

  if (!vehicleType || !vehicleNumber || !vehicleModel || !vehicleColor) {
    showMessage("slotMessage", "Please complete all vehicle details.", "error");
    return;
  }

  const button = document.querySelector(
    `[data-book-slot="${CSS.escape(String(id))}"]`
  );

  try {
    if (button) {
      button.disabled = true;
      button.textContent = "Booking...";
    }

    showMessage("slotMessage", "Processing your booking...");

    await post("/api/bookings", {
      userName: currentUser.name,
      userEmail: currentUser.email,
      userPhone: currentUser.phone,
      mallId: selectedId,
      slotId: id,
      vehicleType,
      vehicleNumber: vehicleNumber.toUpperCase(),
      vehicleModel,
      vehicleColor,
      startTime: $("startTime").value,
      endTime: $("endTime").value
    });

    showMessage("slotMessage", "Booking successful! Your slot is reserved.", "success");

    await searchSlots();
    await loadMyBookings();
  } catch (error) {
    showMessage("slotMessage", error.message || "Booking failed.", "error");
  } finally {
    if (button) {
      button.disabled = false;
      button.textContent = "Book this slot";
    }
  }
}

async function loadMyBookings() {
  if (!currentUser) return;

  try {
    const result = await api("/api/bookings");
    myBookings = Array.isArray(result) ? result : [];

    $("myBookingCount").textContent = myBookings.length;
    renderMyBookings();
    showMessage("bookingMessage", "");
  } catch (error) {
    showMessage("bookingMessage", `Unable to load bookings: ${error.message}`, "error");
  }
}

function renderMyBookings() {
  $("myBookings").innerHTML = myBookings.length
    ? myBookings.map(b => {
        const status = String(b.status || "BOOKED").toUpperCase();
        const isActive = status === "BOOKED" || status === "ACTIVE";
        const id = b.id ?? b._id ?? "";

        return `
          <article class="booking-card">
            <h3>${escapeHTML(b.mallName || findMallName(b.mallId))}
              — Slot ${escapeHTML(b.slotNumber || b.slotId || "—")}</h3>
            <p><strong>Vehicle:</strong> ${escapeHTML(b.vehicleType || "—")} · ${escapeHTML(b.vehicleNumber || "—")}</p>
            <p><strong>Model / color:</strong> ${escapeHTML(b.vehicleModel || "—")} / ${escapeHTML(b.vehicleColor || "—")}</p>
            <p><strong>Entry:</strong> ${formatDate(b.startTime)}</p>
            <p><strong>Scheduled exit:</strong> ${formatDate(b.endTime)}</p>
            <p><strong>Status:</strong> <span class="status">${escapeHTML(status)}</span></p>
            ${isActive ? `
              <button class="outline-btn" type="button"
                data-exit-booking="${escapeHTML(id)}">Mark early exit</button>
              <button class="outline-btn" type="button"
                data-cancel-booking="${escapeHTML(id)}">Cancel booking</button>
            ` : ""}
          </article>
        `;
      }).join("")
    : "<p>You don't have any bookings yet. Search for an available slot to get started.</p>";
}

function findMallName(id) {
  const mall = malls.find(m => String(mallId(m)) === String(id));
  return mall ? mallName(mall) : "Parking location";
}

async function cancelBooking(id) {
  if (!confirm("Are you sure you want to cancel this booking?")) return;

  try {
    await api(`/api/bookings/${encodeURIComponent(id)}/cancel`, {
      method: "PATCH"
    });

    showMessage("bookingMessage", "Booking cancelled successfully.", "success");
    await loadMyBookings();
    await searchSlotsIfPossible();
  } catch (error) {
    showMessage("bookingMessage", error.message, "error");
  }
}

async function earlyExit(id) {
  try {
    showMessage("bookingMessage", "Recording your early exit...");

    await api(`/api/bookings/${encodeURIComponent(id)}/early-exit`, {
      method: "PATCH"
    });

    showMessage("bookingMessage", "Early exit recorded successfully.", "success");
    await loadMyBookings();
    await searchSlotsIfPossible();
  } catch (error) {
    showMessage("bookingMessage", error.message || "Could not record early exit.", "error");
  }
}

async function searchSlotsIfPossible() {
  if (selectedMallId() && $("startTime").value && $("endTime").value) {
    await searchSlots();
  }
}

async function loadAdminBookings() {
  try {
    const result = await api("/api/admin/bookings");
    const bookings = Array.isArray(result) ? result : (result.bookings || []);

    $("adminBookingCount").textContent = bookings.length;

    $("allBookings").innerHTML = bookings.length
      ? bookings.map(b => `
          <article class="booking-card">
            <h3>${escapeHTML(b.userName || "User")} —
              ${escapeHTML(b.mallName || findMallName(b.mallId))}</h3>
            <p><strong>Email:</strong> ${escapeHTML(b.userEmail || "—")}</p>
            <p><strong>Phone:</strong> ${escapeHTML(b.userPhone || "—")}</p>
            <p><strong>Vehicle type:</strong> ${escapeHTML(b.vehicleType || "—")}</p>
            <p><strong>Registration:</strong> ${escapeHTML(b.vehicleNumber || "—")}</p>
            <p><strong>Model / color:</strong> ${escapeHTML(b.vehicleModel || "—")} / ${escapeHTML(b.vehicleColor || "—")}</p>
            <p><strong>Slot:</strong> ${escapeHTML(b.slotNumber || b.slotId || "—")}</p>
            <p><strong>Entry:</strong> ${formatDate(b.startTime)}</p>
            <p><strong>Exit:</strong> ${formatDate(b.endTime)}</p>
            <p><strong>Status:</strong> ${escapeHTML(b.status || "BOOKED")}</p>
          </article>
        `).join("")
      : "<p>No bookings are recorded yet.</p>";

    showMessage("adminBookingMessage", "");
  } catch (error) {
    $("adminBookingCount").textContent = "—";
    $("allBookings").innerHTML = "";
    showMessage("adminBookingMessage", `Unable to load bookings: ${error.message}`, "error");
  }
}

async function loadAdminSlotCount() {
  let count = 0;

  for (const mall of malls) {
    try {
      const result = await api(
        `/api/malls/${encodeURIComponent(mallId(mall))}/slots?start=${encodeURIComponent(dateValue(15))}&end=${encodeURIComponent(dateValue(135))}`
      );

      if (Array.isArray(result)) count += result.length;
    } catch (error) {
      console.error("Unable to count slots:", error);
    }
  }

  $("adminSlotCount").textContent = count;
}

async function refreshAdmin() {
  try {
    await loadMalls();
    await loadAdminSlotCount();
    await loadAdminBookings();
  } catch (error) {
    showMessage("adminBookingMessage", error.message, "error");
  }
}

async function addMall(event) {
  event.preventDefault();
  showMessage("addMallMessage", "");

  try {
    await post("/api/malls", {
      name: $("newMallName").value.trim(),
      address: $("newMallAddress").value.trim()
    });

    $("addMallForm").reset();
    showMessage("addMallMessage", "Location added successfully.", "success");
    await refreshAdmin();
  } catch (error) {
    showMessage("addMallMessage", error.message, "error");
  }
}

async function addSlot(event) {
  event.preventDefault();
  showMessage("addSlotMessage", "");

  const id = $("adminMallSelect").value;
  const number = $("newSlotNumber").value.trim();

  if (!id || !number) {
    showMessage("addSlotMessage", "Select a location and enter a slot name.", "error");
    return;
  }

  try {
    await post(`/api/malls/${encodeURIComponent(id)}/slots`, {
      slotNumber: number
    });

    $("addSlotForm").reset();
    showMessage("addSlotMessage", "Parking slot added successfully.", "success");
    await refreshAdmin();
  } catch (error) {
    showMessage("addSlotMessage", error.message, "error");
  }
}

/* LOGIN */

async function login(event) {
  event.preventDefault();
  showMessage("loginMessage", "");

  const form = $("loginForm");

  if (!form.reportValidity()) return;

  const email = $("loginEmail").value.trim().toLowerCase();
  const password = $("loginPassword").value;

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showMessage("loginMessage", "Enter a valid email address.", "error");
    return;
  }

  if (!password) {
    showMessage("loginMessage", "Please enter your password.", "error");
    return;
  }

  try {
    const formData = new URLSearchParams({ email, password });

    const response = await fetch("/api/auth/login", {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body: formData
    });

    if (!response.ok) {
      let error = {};

      try {
        error = await response.json();
      } catch {}

      throw new Error(error.error || error.message || "Invalid email or password.");
    }

    await establishSession();
  } catch (error) {
    showMessage("loginMessage", error.message, "error");
  }
}

/* REGISTRATION */

async function register(event) {
  event.preventDefault();
  showMessage("registerMessage", "");

  const form = $("registerForm");

  if (!form.reportValidity()) return;

  const name = $("registerName").value.trim();
  const email = $("registerEmail").value.trim().toLowerCase();
  const phone = $("registerPhone").value.trim();
  const password = $("registerPassword").value;
  const confirmPassword = $("confirmPassword").value;

  // Allow a single-letter first name such as "S".
  if (!/^[A-Za-z]{4,14}$/.test(name)) {
  showMessage(
    "registerMessage",
    "Name must contain 4–14 letters only.",
    "error"
  );
  return;
}

  // Validate email once, after trimming whitespace.
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailPattern.test(email) || email.length > 254) {
    showMessage(
      "registerMessage",
      "Enter a valid email address, such as srinidhi@gmail.com.",
      "error"
    );
    return;
  }

  // Indian mobile numbers: exactly 10 digits, starting with 6–9.
  if (!/^[6-9][0-9]{9}$/.test(phone)) {
    showMessage(
      "registerMessage",
      "Enter a 10-digit Indian mobile number starting with 6, 7, 8 or 9.",
      "error"
    );
    return;
  }

  // Password must have uppercase, lowercase, digit and special character.
  if (
    password.length < 8 ||
    password.length > 64 ||
    /\s/.test(password) ||
    !/[a-z]/.test(password) ||
    !/[A-Z]/.test(password) ||
    !/[0-9]/.test(password) ||
    !/[^A-Za-z0-9\s]/.test(password)
  ) {
    showMessage(
      "registerMessage",
      "Use 8–64 characters with uppercase, lowercase, a number and a special character. No spaces.",
      "error"
    );
    return;
  }

  if (password !== confirmPassword) {
    showMessage("registerMessage", "Passwords do not match.", "error");
    return;
  }

  const submitButton = form.querySelector('[type="submit"]');

  try {
    if (submitButton) {
      submitButton.disabled = true;
      submitButton.textContent = "Creating account...";
    }

    await post("/api/auth/register", {
      name,
      email,
      phone,
      password
    });

    form.reset();
    $("loginEmail").value = email;
    $("loginPassword").value = "";

    showAuthTab("login");
    showMessage(
      "loginMessage",
      "Account created successfully! Please log in.",
      "success"
    );
  } catch (error) {
    showMessage(
      "registerMessage",
      error.message || "Registration failed. Please try again.",
      "error"
    );
  } finally {
    if (submitButton) {
      submitButton.disabled = false;
      submitButton.textContent = "Create account";
    }
  }
}

async function establishSession() {
  const user = await api("/api/auth/me");

  const role = String(user.role || "USER")
    .toUpperCase()
    .replace("ROLE_", "");

  if (role !== "ADMIN" && role !== "USER") {
    throw new Error("Your account role is not recognized.");
  }

  currentUser = {
    name: user.name || user.fullName || user.email,
    email: user.email,
    phone: user.phone || "",
    role
  };

  if (role === "ADMIN") {
    showPage("admin");
    await refreshAdmin();
  } else {
    showPage("user");
    $("userDisplayName").textContent = currentUser.name;

    const welcomeText = document.querySelector(
      "#userDashboard .welcome-banner p"
    );

    if (welcomeText) {
      welcomeText.textContent =
        `${currentUser.email} · ${currentUser.phone || "Phone not provided"} · Find a parking space and reserve it.`;
    }

    setDefaultTimes();

    try {
      await loadMalls();
      await loadMyBookings();
    } catch (error) {
      showMessage("bookingMessage", `Could not load parking data: ${error.message}`, "error");
    }
  }
}

async function logout() {
  try {
    await api("/api/auth/logout", { method: "POST" });
  } catch {
    // The server session may already have expired.
  }

  currentUser = null;
  myBookings = [];
  availableSlots = [];

  $("loginForm").reset();
  $("registerForm").reset();

  showAuthTab("login");
  showPage("auth");

  showMessage("loginMessage", "");
  showMessage("registerMessage", "");
  showMessage("slotMessage", "");
  showMessage("bookingMessage", "");
}

function attachEvents() {
  $("loginTab").addEventListener("click", () => showAuthTab("login"));
  $("registerTab").addEventListener("click", () => showAuthTab("register"));

  $("loginForm").addEventListener("submit", login);
  $("registerForm").addEventListener("submit", register);

  $("showLoginPassword").addEventListener("change", event => {
    $("loginPassword").type = event.target.checked ? "text" : "password";
  });

  $("showRegisterPassword").addEventListener("change", event => {
    const type = event.target.checked ? "text" : "password";
    $("registerPassword").type = type;
    $("confirmPassword").type = type;
  });

  document.querySelectorAll(".logoutBtn").forEach(button => {
    button.addEventListener("click", logout);
  });

  $("searchSlots").addEventListener("click", searchSlots);

  $("refreshMalls").addEventListener("click", async () => {
    try {
      await loadMalls();
      showMessage("slotMessage", "Locations refreshed.", "success");
    } catch (error) {
      showMessage("slotMessage", error.message, "error");
    }
  });

  $("refreshBookings").addEventListener("click", loadMyBookings);
  $("adminRefresh").addEventListener("click", refreshAdmin);
  $("addMallForm").addEventListener("submit", addMall);
  $("addSlotForm").addEventListener("submit", addSlot);

  $("slotsGrid").addEventListener("click", event => {
    const button = event.target.closest("[data-book-slot]");
    if (button) bookSlot(button.dataset.bookSlot);
  });

  $("myBookings").addEventListener("click", event => {
    const cancel = event.target.closest("[data-cancel-booking]");
    const exit = event.target.closest("[data-exit-booking]");

    if (cancel) cancelBooking(cancel.dataset.cancelBooking);
    if (exit) earlyExit(exit.dataset.exitBooking);
  });
}

/*
 * Preserve the existing app behavior of showing the login page
 * when the browser loads. This also logs out any previous session,
 * matching the behavior of your supplied code.
 */
async function startApp() {
  attachEvents();

  currentUser = null;
  myBookings = [];
  availableSlots = [];

  showAuthTab("login");
  showPage("auth");

  try {
    await api("/api/auth/logout", { method: "POST" });
  } catch (error) {
    console.error("Could not end previous session:", error);
  }

  showAuthTab("login");
  showPage("auth");
}

startApp();
