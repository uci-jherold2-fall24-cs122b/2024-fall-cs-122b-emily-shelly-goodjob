/**
 * This JavaScript file fetches search results from the backend and populates the result.html page.
 */

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
let currentPage = 0;
let moviesPerPage = 25;
let sortBy = 'ratingDescTitleAsc'

function handleSearchResult(resultData) {
    console.log("handleSearchResult: populating result table from resultData");

    // Log the entire resultData to check its structure
    console.log(resultData);

    // Populate the result table
    let resultTableBodyElement = jQuery("#result_table_body");
    resultTableBodyElement.html(""); // Clear previous results

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_genres"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_stars"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";
        rowHTML += "</tr>";

        resultTableBodyElement.append(rowHTML);
    }
    updatePaginationButtons(resultData.length);
}

function updatePaginationButtons(totalResults) {
    const prevButton = document.getElementById('prev-btn');
    const nextButton = document.getElementById('next-btn');

    // Disable prev button on the first page
    if (currentPage === 0) {
        prevButton.classList.remove("active");
        prevButton.classList.remove("active");
        prevButton.classList.add("disabled");
        prevButton.disabled = true;
    } else {
        prevButton.classList.remove("disabled");
        prevButton.classList.add("active");
        prevButton.classList.add("hover-enabled");
        prevButton.disabled = false;
    }

    // const remainingResults = totalResults - (currentPage * moviesPerPage);
    const isLastPage = moviesPerPage > totalResults;

    // Disable next button if on the last page or fewer than moviesPerPage results are returned
    if (isLastPage) {
        console.log("total result: ", totalResults, "isLastPge: ", isLastPage, "currentPage: ", currentPage);
        nextButton.classList.remove("active");
        nextButton.classList.remove("hover-enabled");
        nextButton.classList.add("disabled");
        nextButton.disabled = true;
        console.log(nextButton.classList);
    } else {
        nextButton.classList.remove("disabled");
        nextButton.classList.add("active");
        nextButton.classList.add("hover-enabled");
        console.log("1");
        nextButton.disabled = false;
    }
}

function cacheSearchResults(resultData, searchParams) {
    // Cache search results and parameters
    sessionStorage.setItem("cachedResults", JSON.stringify(resultData));
    sessionStorage.setItem("searchParams", JSON.stringify(searchParams));
}

// Function to load cached results
function loadCachedResults() {
    const cachedResults = sessionStorage.getItem("cachedResults");
    const searchParams = JSON.parse(sessionStorage.getItem("searchParams"));
    const navigateToResults = sessionStorage.getItem("navigateToResults");

    if (navigateToResults && cachedResults && searchParams) {
        document.getElementById('movie-per-page').value = searchParams.moviesPerPage;
        document.getElementById('sortBy').value = searchParams.sortBy;
        currentPage = searchParams.currentPage;
        moviesPerPage = searchParams.moviesPerPage;
        sortBy = searchParams.sortBy;

        console.log("Displaying cached results from session storage");
        handleSearchResult(JSON.parse(cachedResults));
        sessionStorage.removeItem("navigateToResults"); // clear the navigation flag
    } else {
        // Fetch default or initial results if no cache exists
        console.log("Fetching new results or default results");
        fetchResults(0, 25, sortBy);
    }
}

function clearCache() {
    sessionStorage.removeItem("cachedResults");
    sessionStorage.removeItem("searchParams");
}

function fetchResults(currentPage, moviesPerPage, sortBy) {
    // Send an AJAX request to the backend API to get the search results
    const urlParams = new URLSearchParams(window.location.search);
    const genre = urlParams.get("genre");
    const titleInitial = urlParams.get("titleInitial");
    const title = urlParams.get("title");
    const year = urlParams.get("year");
    const director = urlParams.get("director");
    const star = urlParams.get("star");

    let requestUrl = `/cs122b_project1_api_example_war/search?page=${currentPage}&moviesPerPage=${moviesPerPage}&sortBy=${sortBy}`;
    if (genre) requestUrl += `&genre=${encodeURIComponent(genre)}`;
    if (titleInitial) requestUrl += `&titleInitial=${encodeURIComponent(titleInitial)}`;
    if (title) requestUrl += `&title=${encodeURIComponent(title)}`;
    if (year) requestUrl += `&year=${encodeURIComponent(year)}`;
    if (director) requestUrl += `&director=${encodeURIComponent(director)}`;
    if (star) requestUrl += `&star=${encodeURIComponent(star)}`;

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: requestUrl,
        success: (resultData) => {
            // Check if no data is returned on this page
            if (resultData.length === 0 && currentPage > 0) {
                // Move back to the previous page if no data found
                currentPage -= 1;
                updatePaginationButtons(0);
                return;
            }

            handleSearchResult(resultData);
            cacheSearchResults(resultData, { currentPage, moviesPerPage, sortBy, genre, titleInitial, title, year, director, star });
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("AJAX request failed:", textStatus, errorThrown);
        }
    });
}

function goBackToResults() {
    // Navigate to the Movie List page, which loads cached data on load
    sessionStorage.setItem("navigateToResults", "true"); // store whether click on hyperlink
    window.location.href = "result.html";
}

// Dropdowns
document.getElementById('updateButton').addEventListener('click', function () {
    clearCache();
    moviesPerPage = document.getElementById('movie-per-page').value;
    sortBy = document.getElementById('sortBy').value;
    currentPage = 0;

    fetchResults(currentPage, moviesPerPage, sortBy);
});

// Prev button
document.getElementById('prev-btn').addEventListener('click', function () {
    if (currentPage > 0) {
        currentPage -= 1;
        fetchResults(currentPage, moviesPerPage, sortBy);
    }
});

// Next buttons
document.getElementById('next-btn').addEventListener('click', function () {
    currentPage += 1
    fetchResults(currentPage, moviesPerPage, sortBy);
});

// Load cached data
jQuery(document).ready(() => {
    loadCachedResults();
});
