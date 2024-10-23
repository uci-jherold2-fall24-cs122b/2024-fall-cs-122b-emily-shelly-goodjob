/**
 * This JavaScript file fetches search results from the backend and populates the result.html page.
 */

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSearchResult(resultData) {
    console.log("handleSearchResult: populating result table from resultData");

    // Log the entire resultData to check its structure
    console.log(resultData);

    // Populate the result table
    let resultTableBodyElement = jQuery("#result_table_body");

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
        console.log(rowHTML);

        resultTableBodyElement.append(rowHTML);
    }
}

// Send an AJAX request to the backend API to get the search results
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "/cs122b_project1_api_example_war/search" + window.location.search, // Use query parameters from the URL
    success: (resultData) => handleSearchResult(resultData),
    error: (jqXHR, textStatus, errorThrown) => {
        // Log the error for debugging
        console.error("AJAX request failed:", textStatus, errorThrown);
    }
});
