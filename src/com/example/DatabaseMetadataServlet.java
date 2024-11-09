package com.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(name = "DatabaseMetadataServlet", urlPatterns = "/_dashboard/metadata")
public class DatabaseMetadataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonArray metadataArray = new JsonArray();

        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData databaseMetaData = conn.getMetaData();

                // Get all tables
                ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    JsonObject tableObject = new JsonObject();
                    tableObject.addProperty("tableName", tableName);

                    // Get columns for each table
                    JsonArray columnsArray = new JsonArray();
                    ResultSet columns = databaseMetaData.getColumns(null, null, tableName, "%");
                    while (columns.next()) {
                        JsonObject columnObject = new JsonObject();
                        columnObject.addProperty("columnName", columns.getString("COLUMN_NAME"));
                        columnObject.addProperty("typeName", columns.getString("TYPE_NAME"));
                        columnsArray.add(columnObject);
                    }
                    tableObject.add("columns", columnsArray);
                    metadataArray.add(tableObject);
                }
            }
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }

        PrintWriter out = response.getWriter();
        out.write(metadataArray.toString());
        out.flush();
    }
}
