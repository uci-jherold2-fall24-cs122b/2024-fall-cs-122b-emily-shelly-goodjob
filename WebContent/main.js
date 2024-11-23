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

// Navigate to the Movie List page, which loads cached data on load
function goBackToResults() {
    sessionStorage.setItem("navigateToResults", "true");
    window.location.href = "result.html";
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log(data)

    // TODO: if you want to cache the result into a global variable you can do it here

    doneCallback( { suggestions: data } );
}

function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"]);
    const movieID = suggestion["data"]["movieID"];
    window.location.href = `single-movie.html?id=${encodeURIComponent(movieID)}`;
}

// Autocomplete
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    jQuery.ajax({
        "method": "GET",
        "url": "movie-suggestion?query=" + escape(query),
        "success": function(data) {
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

// Full Text Search
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
}

// Execute fetching on page load
jQuery(document).ready(function () {
    fetchGenres();
    fetchTitleInitials();
});

$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },

    deferRequestBy: 300,
    minChars: 3,
});

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

// // TODO: if you have a "search" button, you may want to bind the onClick event as well of that button
// $('#search-button').click(function () {
//     const query = $('#autocomplete').val();
//     handleNormalSearch(query);
// });
