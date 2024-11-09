$(document).ready(function () {
    $(".proceed-to-pay-btn").on("click", function () {
        const starName = $("#startname").val();
        const birthYear = $("#birthYear").val();

        if (!starName.trim()) {
            $("#add-info").text("Star name is required.");
            return;
        }

        $.ajax({
            url: "/cs122b_project1_api_example_war/_dashboard/add_star",
            method: "POST",
            data: {
                startname: starName,
                birthYear: birthYear || ""
            },
            success: function (response) {
                if (response.status === "success") {
                    $("#add-info").text(response.message);
                } else {
                    $("#add-info").text("Error: " + response.message);
                }
            },
            error: function () {
                $("#add-info").text("Error: Failed to connect to the server.");
            }
        });
    });
});
