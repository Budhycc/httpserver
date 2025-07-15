function buildTable(dataSiswa) {
  if (dataSiswa.length < 1) return;
  const table = document.getElementById("table-data");
  const tbody = table.querySelector("tbody");
  tbody.replaceChildren();
  let i = 1;
  dataSiswa.forEach((siswa) => {
    const col = document.createElement("td");
    col.appendChild(document.createTextNode(i));
    const row = document.createElement("tr");
    row.appendChild(col);
    ["nis", "nama", "alamat", "tempat_lahir", "tanggal_lahir"].forEach(
      (key) => {
        const c = document.createElement("td");
        if (Object.hasOwn(siswa, key)) {
          c.appendChild(document.createTextNode(siswa[key]));
        }
        row.appendChild(c);
      }
    );
    const actionCol = document.createElement("td");
    const editButton = document.createElement("button");
    editButton.textContent = "Edit";
    editButton.className = "btn btn-sm btn-warning me-2";
    editButton.addEventListener("click", () => showEditForm(siswa));
    actionCol.appendChild(editButton);

    const deleteButton = document.createElement("button");
    deleteButton.textContent = "Delete";
    deleteButton.className = "btn btn-sm btn-danger";
    deleteButton.addEventListener("click", () => deleteSiswa(siswa.nis));
    actionCol.appendChild(deleteButton);

    row.appendChild(actionCol);
    tbody.appendChild(row);
    i++;
  });
}

function showEditForm(siswa) {
  document.getElementById("nis").value = siswa.nis;
  document.getElementById("nama").value = siswa.nama;
  document.getElementById("alamat").value = siswa.alamat;
  document.getElementById("tempat_lahir").value = siswa.tempat_lahir;
  document.getElementById("tanggal_lahir").value = siswa.tanggal_lahir;

  document.getElementById("btn-save").textContent = "Update";
  document.getElementById("form-modal").dataset.mode = "update";
  document.getElementById("form-modal").dataset.nis = siswa.nis;

  const modal = new bootstrap.Modal(document.getElementById("form-modal"));
  modal.show();
}

function deleteSiswa(nis) {
  if (confirm("Are you sure you want to delete this data?")) {
    fetch(`/api/data-siswa/${nis}`, { method: "DELETE" }).then(() => {
      initialize()
        .then((data) => store(data))
        .then((data) => buildTable(data));
    });
  }
}

function initialize() {
  return new Promise((resolve) => {
    const data = JSON.parse(sessionStorage.getItem("data-siswa") || "[]");
    if (data.length > 1) {
      resolve(data);
    } else {
      resolve(fetch("/api/data-siswa").then((res) => res.json()));
    }
  });
}

function append(row) {
  return new Promise((resolve) => {
    let data = JSON.parse(sessionStorage.getItem("data-siswa") || "[]");
    data.push(row);
    resolve(data);
  });
}

function store(data) {
  return new Promise((resolve) => {
    sessionStorage.setItem("data-siswa", JSON.stringify(data));
    resolve(data);
  });
}

initialize()
  .then((data) => store(data))
  .then((data) => buildTable(data));

document.getElementById("btn-save").addEventListener("click", (e) => {
  const formModal = document.getElementById("form-modal");
  const mode = formModal.dataset.mode;
  const nis = formModal.dataset.nis;

  const data = {};
  ["nis", "nama", "alamat", "tempat_lahir", "tanggal_lahir"].forEach((key) => {
    data[key] = document.getElementById(key).value;
    document.getElementById(key).value = "";
  });

  if (mode === "update") {
    const opt = {
      method: "PUT",
      body: new URLSearchParams(data),
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    };
    fetch(`/api/data-siswa/${nis}`, opt)
      .then(() => {
        initialize()
          .then((data) => store(data))
          .then((data) => buildTable(data));
      });
  } else {
    const opt = {
      method: "POST",
      body: new URLSearchParams(data),
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    };
    fetch("/api/data-siswa", opt)
      .then((response) => response.json())
      .then((siswa) => append(siswa))
      .then((dataSiswa) => store(dataSiswa))
      .then((dataSiswa) => buildTable(dataSiswa));
  }

  const modal = bootstrap.Modal.getInstance(formModal);
  modal.hide();
  formModal.dataset.mode = "create";
  document.getElementById("btn-save").textContent = "Save";
});
