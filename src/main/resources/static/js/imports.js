"use strict";
const body = document.getElementById("import-lines");
function renumber() {
  [...body.rows].forEach((row, index) => row.querySelectorAll("input,select").forEach(field => {
    field.name = field.name.replace(/lines\[\d+\]/, "lines[" + index + "]");
    field.removeAttribute("id");
  }));
}
document.getElementById("add-line").addEventListener("click", () => {
  if (body.rows.length >= 100) return;
  const row = body.rows[0].cloneNode(true);
  row.querySelectorAll("input,select").forEach(field => field.value = "");
  row.querySelectorAll("small").forEach(el => el.remove());
  body.append(row); renumber();
});
body.addEventListener("click", event => {
  if (event.target.classList.contains("remove-line") && body.rows.length > 1) {
    event.target.closest("tr").remove(); renumber();
  }
});

