
function placeOrder() {
    const formData = $("#payment-form").serialize();
    console.log("Form data being sent:", formData);

    $.ajax({
        url: "api/placeOrder",
        method: "POST",
        data: formData,
        success: function(resultData) {
            if (resultData.status === "success") {
                alert(resultData.message);
                window.location.href = "confirmation.html";
            } else {
                alert(resultData.message);
            }
        },
        error: function(xhr, status, errorThrown) {
            alert("Error: " + xhr.responseText);
        }
    });
}

function goBackToResults() {
    // load data
    sessionStorage.setItem("navigateToResults", "true");
    window.location.href = "result.html";
}

$(document).ready(function() {
    // get data in session
    const totalAmount = sessionStorage.getItem("totalAmount") || "0.00";
    $("#total-price").text(totalAmount);
});

