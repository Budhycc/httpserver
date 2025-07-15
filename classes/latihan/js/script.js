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
    tbody.appendChild(row);
    i++;
  });
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
  const data = {};
  ["nis", "nama", "alamat", "tempat_lahir", "tanggal_lahir"].forEach((key) => {
    data[key] = document.getElementById(key).value;
    document.getElementById(key).value = "";
  });
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
});
