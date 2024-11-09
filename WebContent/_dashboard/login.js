let login_form = $("#login_form");

/**
 * Handle the data returned by DashboardServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataJson) {
    try {
        // If resultDataJson is a string, parse it into an object
        if (typeof resultDataJson === "string") {
            resultDataJson = JSON.parse(resultDataJson);
        }

        if (resultDataJson["status"] === "success") {
            window.location.replace("index.html");
        } else {
            $("#login_error_message").text(resultDataJson["message"]);
        }
    } catch (e) {
        $("#login_error_message").text("Unexpected server response.");
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit employee login form");
    // Prevent default form submission
    formSubmitEvent.preventDefault();

    $.ajax(
        "/cs122b_project1_api_example_war/_dashboard/login", {
            method: "POST",
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to the handler function
login_form.submit(submitLoginForm);
