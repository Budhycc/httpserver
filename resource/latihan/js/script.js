let categories = [];

function buildTable(transactions) {
  const table = document.getElementById("table-transactions");
  const tbody = table.querySelector("tbody");
  tbody.replaceChildren();
  let i = 1;
  transactions.forEach((transaction) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${i}</td>
      <td>${transaction.description}</td>
      <td>${transaction.amount}</td>
      <td>${transaction.date}</td>
      <td>${getCategoryName(transaction.categoryId)}</td>
      <td>
        <button class="btn btn-sm btn-warning me-2" onclick="showEditForm(${transaction.id})">Edit</button>
        <button class="btn btn-sm btn-danger" onclick="deleteTransaction(${transaction.id})">Delete</button>
      </td>
    `;
    tbody.appendChild(row);
    i++;
  });
}

function getCategoryName(categoryId) {
  const category = categories.find((c) => c.id === categoryId);
  return category ? category.name : "Unknown";
}

function populateCategories() {
  const select = document.getElementById("category");
  select.replaceChildren();
  categories.forEach((category) => {
    const option = document.createElement("option");
    option.value = category.id;
    option.textContent = category.name;
    select.appendChild(option);
  });
}

function showEditForm(id) {
  const transaction = transactions.find((t) => t.id === id);
  document.getElementById("id").value = transaction.id;
  document.getElementById("description").value = transaction.description;
  document.getElementById("amount").value = transaction.amount;
  document.getElementById("date").value = transaction.date;
  document.getElementById("category").value = transaction.categoryId;

  document.getElementById("btn-save").textContent = "Update";
  document.getElementById("form-modal").dataset.mode = "update";

  const modal = new bootstrap.Modal(document.getElementById("form-modal"));
  modal.show();
}

function deleteTransaction(id) {
  if (confirm("Are you sure you want to delete this transaction?")) {
    fetch(`/api/transactions/${id}`, { method: "DELETE" }).then(() => {
      loadData();
    });
  }
}

function loadData() {
  Promise.all([
    fetch("/api/categories").then((res) => res.json()),
    fetch("/api/transactions").then((res) => res.json()),
  ]).then(([cats, trans]) => {
    categories = cats;
    transactions = trans;
    populateCategories();
    buildTable(transactions);
  });
}

document.getElementById("btn-save").addEventListener("click", (e) => {
  const formModal = document.getElementById("form-modal");
  const mode = formModal.dataset.mode;
  const id = document.getElementById("id").value;

  const data = {
    description: document.getElementById("description").value,
    amount: document.getElementById("amount").value,
    date: document.getElementById("date").value,
    categoryId: document.getElementById("category").value,
  };

  let method = "POST";
  let url = "/api/transactions";
  if (mode === "update") {
    method = "PUT";
    data.id = id;
  }

  fetch(url, {
    method: method,
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  })
    .then(() => {
      loadData();
      const modal = bootstrap.Modal.getInstance(formModal);
      modal.hide();
      formModal.dataset.mode = "create";
      document.getElementById("btn-save").textContent = "Save";
      document.getElementById("id").value = "";
      document.getElementById("description").value = "";
      document.getElementById("amount").value = "";
      document.getElementById("date").value = "";
      document.getElementById("category").value = "";
    });
});

loadData();
