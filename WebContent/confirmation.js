function goBackToResults() {
    // Navigate to the Movie List page, which loads cached data on load
    sessionStorage.setItem("navigateToResults", "true"); // store whether click on hyperlink
    window.location.href = "result.html";
}

$(document).ready(function () {
    // Fetch order details from session storage or a backend API
    $.ajax({
        url: "api/placeOrder",
        method: "GET", // Adjust this as needed
        success: function (resultData) {
            if (resultData.status === "success") {
                console.log(resultData);
                renderConfirmationTable(resultData.sales);
            } else {
                alert(resultData.message);
            }
        },
        error: function (xhr, status, errorThrown) {
            console.log("Error loading confirmation data:", errorThrown);
            alert("Could not load confirmation data.");
        }
    });
});

function renderConfirmationTable(sales) {
    const tableBody = $("#confirm_table_body");
    tableBody.empty(); // Clear any existing rows

    sales.forEach(sale => {
        const rowHTML = `
            <tr>
                <td>${sale.saleId}</td>
                <td>${sale.title}</td>
                <td>${sale.quantity}</td>
                <td>$${sale.price.toFixed(2)}</td>
                <td>$${sale.total.toFixed(2)}</td>
            </tr>`;
        tableBody.append(rowHTML);
    });
}
