/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    // console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append(
        "<div class='title-container'>" +
        "<div class='page-title-text'>" + resultData["movie_title"] + "</div>" +
        "<button class='update-btn' onclick=\"addToCart('" + resultData["movie_id"] + "')\">Add</button>" +
        "</div>" +
        "<div class='page-subtitle-text'>Year: " + resultData["movie_year"] + "</div>" +
        "<div class='page-subtitle-text'>Director: " + resultData["movie_director"] + "</div>" +
        "<div class='page-subtitle-text'>Genres: " + resultData["movie_genres"] + "</div>" +
        "<div class='page-subtitle-text'>Rating: " + resultData["movie_rating"] + "</div>"
    );

    // console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    let movieStars = resultData["movie_stars"];
    for (let i = 0; i < movieStars.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th><a href='single-star.html?id=" + movieStars[i]["star_id"] + "'>"
            + movieStars[i]["star_name"] + "</a></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}

function goBackToResults() {
    // Navigate to the Movie List page, which loads cached data on load
    sessionStorage.setItem("navigateToResults", "true"); // store whether click on hyperlink
    window.location.href = "result.html";
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",     // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request URL, which is mapped by SingleMovieServlet
    success: (resultData) => handleResult(resultData), // Setting callback function to handle data returned successfully by the SingleMovieServlet
    error: (xhr, status, error) => {
        console.error("Error: " + status + " " + error); // Log any errors
    }
});

function addToCart(movieId) {
    console.log("Adding to cart: " + movieId);
    jQuery.ajax({
        url: "api/addToCart",
        method: "POST",
        data: {
            movieId: movieId,
            quantity: 1
        },
        success: function(resultData) {
            console.log("Success: " + resultData);
            alert("Movie added to cart!");
        },
        error: function(error) {
            console.log("Error adding to cart: " + error);
            alert("Error adding to cart.");
        }
    });
}