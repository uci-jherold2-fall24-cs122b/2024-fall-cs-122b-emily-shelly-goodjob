// Cache suggestions in SessionStorage
var suggestionCache = new Map();

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

    // Cache the result into SessionStorage
    suggestionCache.set(query, data);
    sessionStorage.setItem("autocompleteCache", JSON.stringify([...suggestionCache]));

    doneCallback( { suggestions: data } );
}

function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"]);
    const movieID = suggestion["data"]["movieID"];
    window.location.href = `single-movie.html?id=${encodeURIComponent(movieID)}`;
}

function retrieveCacheFromSessionStorage(query) {
    const cachedData = sessionStorage.getItem("autocompleteCache");
    if (cachedData) {
        const cachedMap = new Map(JSON.parse(cachedData));
        suggestionCache.clear();
        cachedMap.forEach((value, key) => suggestionCache.set(key, value)); // Repopulate the in-memory cache
        return suggestionCache.get(query);
    }
    return null;
}

// Autocomplete
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    const cachedSuggestions = suggestionCache.get(query) || retrieveCacheFromSessionStorage(query);
    if (cachedSuggestions) {
        console.log("Using cached results for query:", query);
        console.log("Cached suggestions:", cachedSuggestions);
        doneCallback({ suggestions: cachedSuggestions });
        return;
    }

    console.log("sending AJAX request to backend Java Servlet")

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

// Handle form submission
$('form.fulltext-search-form').on('submit', function(event) {
    event.preventDefault();

    const query = $('#autocomplete').val().trim();

    if (query) {
        window.location.href = `result.html?query=${encodeURIComponent(query)}`;
    } else {
        alert("Please enter a search term.");
    }
});

// Bind the "Enter" key to the same behavior
$('#autocomplete').keypress(function(event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        const query = $(this).val().trim();

        if (query) {
            window.location.href = `result.html?query=${encodeURIComponent(query)}`;
        } else {
            alert("Please enter a search term.");
        }
    }
});
