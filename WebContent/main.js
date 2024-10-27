/**
 * Fetch and display genres and title initials for browsing.
 */

// Fetch genres and display them as links
function fetchGenres() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "/cs122b_project1_api_example_war/genres",
        success: function (data) {
            let genreLinks = jQuery("#genre-links");
            genreLinks.empty(); // Clear existing links
            data.forEach(genre => {
                let genreLink = `<a href="result.html?genre=${encodeURIComponent(genre.genre_name)}" class="btn btn-link browse-link">${genre.genre_name}</a>`;
                genreLinks.append(genreLink);
            });
        }
    });
}

// Fetch title initials and display them as links
function fetchTitleInitials() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "/cs122b_project1_api_example_war/titles",
        success: function (data) {
            let titleLinks = jQuery("#title-links");
            titleLinks.empty(); // Clear existing links
            data.forEach(titleInitial => {
                let titleLink = `<a href="result.html?titleInitial=${encodeURIComponent(titleInitial)}" class="btn btn-link browse-link">${titleInitial}</a>`;
                titleLinks.append(titleLink);
            });
        }
    });
}

function goBackToResults() {
    // Navigate to the Movie List page, which loads cached data on load
    sessionStorage.setItem("navigateToResults", "true"); // store whether click on hyperlink
    window.location.href = "result.html";
}

// Execute fetching on page load
jQuery(document).ready(function () {
    fetchGenres();
    fetchTitleInitials();
});
