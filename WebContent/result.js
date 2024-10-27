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
    resultTableBodyElement.html("");

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

        // Log the generated rowHTML to see if it's being created correctly
        // console.log(rowHTML);

        resultTableBodyElement.append(rowHTML);
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

    if (cachedResults && searchParams) {
        handleSearchResult(JSON.parse(cachedResults)); // Display cached results
    } else {
        // Fetch default or initial results if no cache exists
        fetchResults(0, 25, 'ratingDescTitleAsc');
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
            handleSearchResult(resultData);
            cacheSearchResults(resultData, {currentPage, moviesPerPage, sortBy, genre, titleInitial, title, year, director, star});
        },
        error: (jqXHR, textStatus, errorThrown) => {
            // Log the error for debugging
            console.error("AJAX request failed:", textStatus, errorThrown);
        }
    });
}

// Initial results on page
fetchResults(currentPage, moviesPerPage, sortBy);

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

// Next button
document.getElementById('next-btn').addEventListener('click', function () {
    currentPage += 1
    fetchResults(currentPage, moviesPerPage, sortBy);
});

// Load cached data
jQuery(document).ready(() => {
    loadCachedResults();
});
