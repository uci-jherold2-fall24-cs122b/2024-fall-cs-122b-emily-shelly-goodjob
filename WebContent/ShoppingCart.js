
function loadCartItems() {
    $.ajax({
        url: "api/getCart",
        method: "GET",
        success: function (resultData) {
            console.log(resultData);
            displayCartItems(resultData);
        },
        error: function () {
            alert("Failed to load cart items.");
        }
    });
}

function displayCartItems(cartItems) {
    const tableBody = $("#checkout_table_body");
    tableBody.empty(); // Clear existing rows
    let totalAmount = 0;

    cartItems.forEach(item => {
        const itemTotal = item.quantity * item.price;
        totalAmount += itemTotal;

        const rowHTML = `
                <tr>
                    <td>${item.title}</td>
                    <td>
                        <button class="quantity-btn" onclick="updateQuantity('${item.movieId}', -1)">-</button>
                        ${item.quantity}
                        <button class="quantity-btn" onclick="updateQuantity('${item.movieId}', 1)">+</button>
                    </td>
                    <td>
                        <button class="delete-btn" onclick="deleteCartItem('${item.movieId}')">Delete</button>
                    </td>
                    <td>$${item.price.toFixed(2)}</td>
                    <td>$${itemTotal.toFixed(2)}</td>
                </tr>`;
        tableBody.append(rowHTML);
    });

    // Update total amount in the footer
    $("#total-amount").text(`$${totalAmount.toFixed(2)}`);
}

function goBackToResults() {
    // navigate to the Movie List page
    sessionStorage.setItem("navigateToResults", "true");
    window.location.href = "result.html";
}

// Function to update quantity
window.updateQuantity = function (movieId, delta) {
    $.ajax({
        url: "api/updateCartItem",
        method: "POST",
        data: { movieId: movieId, delta: delta },
        success: function (resultData) {
            loadCartItems(); // refresh cart display
        },
        error: function () {
            alert("Failed to update item quantity.");
        }
    });
};

// Function to delete a cart item
window.deleteCartItem = function (movieId) {
    $.ajax({
        url: "api/deleteCartItem",
        method: "POST",
        data: { movieId: movieId },
        success: function () {
            loadCartItems(); // refresh cart display
        },
        error: function () {
            alert("Failed to delete item.");
        }
    });
};

// Handle "Proceed to Payment" button click
$("#proceed-to-pay-btn").click(function () {
    // Redirect to payment page
    window.location.href = "payment.html";
});

$(document).ready(function () {
    loadCartItems();
});
