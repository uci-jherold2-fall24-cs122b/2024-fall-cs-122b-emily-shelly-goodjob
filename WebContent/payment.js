


function goBackToResults() {
    // Navigate to the Movie List page, which loads cached data on load
    sessionStorage.setItem("navigateToResults", "true"); // store whether click on hyperlink
    window.location.href = "result.html";
}

$(document).ready(function() {
    // Retrieve and display the total price from sessionStorage
    const totalAmount = sessionStorage.getItem("totalAmount") || "0.00";
    $("#total-price").text(totalAmount);
});

