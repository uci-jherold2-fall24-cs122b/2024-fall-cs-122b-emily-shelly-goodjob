$(document).ready(function () {
    // Fetch metadata on page load
    $.ajax({
        url: "/cs122b_project1_api_example_war/_dashboard/metadata",
        method: "GET",
        success: function (metadata) {
            displayMetadata(metadata);
        },
        error: function () {
            $("#login_error_message").text("Failed to load database metadata.");
        }
    });
});

function displayMetadata(metadata) {
    // Assuming there is a div to hold all the tables
    const tablesContainer = $("<div class='tables-container'></div>");

    metadata.forEach(table => {
        // Create a section for each table
        const tableSection = $("<div class='table-section'></div>");
        tableSection.append(`<div class="table-header">${table.tableName}</div>`);

        // Create a table element
        const tableElement = $("<table class='table table-bordered'></table>");
        const tableHeader = `
            <thead>
                <tr>
                    <th class="th-col">Attribute</th>
                    <th class="th-col">Type</th>
                </tr>
            </thead>`;
        tableElement.append(tableHeader);

        // Add each column's details as rows
        const tableBody = $("<tbody></tbody>");
        table.columns.forEach(column => {
            const rowHTML = `
                <tr>
                    <td class="td-col">${column.columnName}</td>
                    <td class="td-col">${column.typeName}</td>
                </tr>`;
            tableBody.append(rowHTML);
        });
        tableElement.append(tableBody);

        // Append table to the section
        tableSection.append(tableElement);
        tablesContainer.append(tableSection);
    });

    // Append all tables to the main container on the HTML page
    $("#metadata-container").html(tablesContainer);
}
