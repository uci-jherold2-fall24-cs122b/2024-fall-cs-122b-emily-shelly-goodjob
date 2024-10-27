/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function addToCart(movieId) {
    console.log("Adding to cart: " + movieId);
    jQuery.ajax({
        url: "/api/addToCart",
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

function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
        rowHTML += `<td><button onclick="addToCart('${resultData[i]['movie_id']}')">Add to Cart</button></td>`;
        // console.log(resultData);
        // display stars
        let starsArray = resultData[i]["movie_stars"];
        let starsHTML = "";
        for (let j = 0; j < starsArray.length; j++) {
            starsHTML += '<a href="single-star.html?id=' + starsArray[j]["star_id"] + '">'
                + starsArray[j]["star_name"] + '</a>';
            // starsHTML += starsArray[j]["star_name"] + '</a>';
            if (j < starsArray.length - 1) {
                starsHTML += ", ";
            }
        }
        rowHTML += "<th>" + starsHTML + "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] +
            " <span class='star'>" +
            "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='16' height='16' fill='none' stroke='#FB83AA' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'>" +
            "<polygon points='12 2 15 8.6 22 9.3 17 14.3 18.5 21 12 17.8 5.5 21 7 14.3 2 9.3 9 8.6 12 2' />" +
            "</svg></span></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});