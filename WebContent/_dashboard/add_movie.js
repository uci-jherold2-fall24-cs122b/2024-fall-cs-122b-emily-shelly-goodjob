$(document).ready(function () {
    $(".proceed-to-pay-btn").on("click", function () {
        const movieTitle = $("#movieTitle").val();
        const year = $("#year").val();
        const director = $("#director").val();
        const starName = $("#starName").val();
        const birthYear = $("#birthYear").val();
        const genre = $("#genre").val();

        if (!movieTitle.trim()) {
            $("#add-info").text("Movie title is required.");
            return;
        }

        $.ajax({
            url: "/cs122b_project1_api_example_war/_dashboard/add_movie",
            method: "POST",
            data: {
                movieTitle: movieTitle,
                year: year,
                director: director,
                starName: starName,
                birthYear: birthYear || "",
                genre: genre
            },
            success: function (response) {
                if (response.status === "success") {
                    $("#add-info").text(`${response.message}. Movie ID: ${response.movie_id}, Star ID: ${response.star_id}, Genre ID: ${response.genre_id}`);
                } else if (response.status === "duplicate") {
                    $("#add-info").text("Error: " + response.message);
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
