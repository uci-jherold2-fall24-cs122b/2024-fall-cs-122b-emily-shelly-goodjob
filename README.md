# CS122B Project

## Project 1
- #### Demo Video Link**: [Project 1 Demo](https://www.youtube.com/watch?v=BbaYB7Rwmbs)

- #### Contribution
- ##### Tasks 1-4: All members
- ##### Tasks 5 & 6: Emily & Shelly

## Project 2
- #### Demo Video Link**: [Project 2 Demo](https://www.youtube.com/watch?v=0p3Lk-sll_0)

- #### Contribution
- ##### Tasks 1 & 4: Emily + Shelly
- ##### Tasks 2 & 3: Emily

### Search Implementation
- #### The search functionality enables users to find movies by:
- #### Title**, **Year**, **Director**, and **Star's Name
  - ##### Substring Matching: Supported for `title`, `director`, and `star` fields using `LIKE` predicate.
  - ##### Example Query: `SELECT * FROM movies WHERE title LIKE '%Term%'` retrieves movies like "Terminator" and "Terminal".
  - ##### Exact Match: Year supports exact matches only.

## Project 3
- #### Demo Video Link**: [Project 3 Demo](https://www.youtube.com/watch?v=r4CbXQOFUdQ)

- #### Contribution
- ##### Tasks 1-6: Emily + Shelly

- #### XML Parsing Optimizations

- ##### Multi-threaded Parsing with Task Queues
  - ###### Description: Utilized Java’s `ExecutorService` to parallelize XML parsing by distributing different XML elements across multiple threads. Each thread processes a chunk of XML data, performing batch inserts into the database.
  - ###### Improvement: Achieved ~50% runtime reduction on large files due to concurrent processing of XML elements.

- ##### Preloaded Cache for Frequent Lookups
  - ###### Description: Loaded existing movies, stars, and genres into an in-memory cache before parsing, enabling the parser to quickly verify existing records without frequent database queries.
  - ###### Improvement: Reduced query load on the database, cutting processing time by around 40% through minimized I/O operations.

- #### Inconsistency Report

- ##### 1. Data Format Errors
- ###### Errors due to unexpected formats in XML data.

- ##### Inconsistent `dod` Format
  - ###### Example: Actor "Bonzo" had `dod` with extra characters (`1992+`).
  - ###### Resolution: Converted to `1992` for parsing and inserted as `birthYear`.

- ##### Invalid Birth Year
  - ###### Example: Actor "Darrell Zwerling" had non-integer characters in `birthYear`.
  - ###### Resolution: Set `birthYear` to NULL to maintain database consistency.

- #### 2. Duplicate Data Conflicts
- ###### Duplicates detected during insertion of entries with the same `id` or `name`.

- ###### Duplicate Star Entries
  - ###### Example: "James Abrahams"
  - ###### Resolution: Skipped duplicate insertion to avoid conflicting `id`.

- #### 3. Missing or Null Values
- ##### Partial entries or records with NULL values due to omitted optional fields.

- ##### Actor Missing `name`
  - ###### Issue: Entries with missing `name` or `stagename` were skipped.

- ##### Actors without `birthYear`
  - ###### Example: "F. Murray Abraham" (missing `birthYear`).
  - ###### Resolution: Inserted with `birthYear` as NULL.

- #### 4. Relationship and Reference Errors
- ##### Orphaned records due to missing references in primary tables.

- ##### Unresolved Movie Reference**
  - ###### Example: Actor "Victoria Abril" referencing a missing movie ID.
  - ###### Resolution: Skipped linking record due to the absence of the referenced movie in `movies` table.

- #### 5. Parsing Exceptions
- ##### Exceptions encountered during parsing due to data integrity or connectivity issues.

- ##### QLIntegrityConstraintViolationException
  - ###### Issue: Attempted duplicate primary key insertion.
  - ###### Resolution: Logged "Already in database" message and skipped insertion.

- ##### Connection Closed Exception**
  - ###### Issue: Unexpected database disconnection.
  - ###### Resolution: Reconnected to the database and resumed parsing.

## Project 4
  - #### Team#:

  - #### Names: Emily, Shelly

  - #### Project 4 Video Demo Link:

  - #### Instruction of deployment:

  - #### Collaborations and Work Distribution:

- ### Connection Pooling
  - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - ##### ActorParser: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/527c2cf065199db49b8d0d9661b5d9776a9d9f59/src/ActorParser.java
    - ##### ActorParserMain: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/c4916f07b4540db7268c5a113fface7da8a65e57/src/ActorParserMain.java
    - ##### AddMovieServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/d6e6ba38f246cc85a9b3903f99810daada4cd9c5/src/AddMovieServlet.java
    - ##### AddStarServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/d6e6ba38f246cc85a9b3903f99810daada4cd9c5/src/AddStarServlet.java
    - ##### AddToCartServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/ade5769274632e321876dd8d857381a8745122a6/src/AddToCartServlet.java
    - ##### DeleteCartItemServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7e9ed1e4115016513dc2a20824c18a4e872573e0/src/DeleteCartItemServlet.java
    - ##### FormRecaptcha: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/0c4f03f15914e6f699e38727547f1013afaf867b/src/FormRecaptcha.java
    - ##### GenreServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/1734c44a27d2476ec05389cf21f4f01b56178d86/src/GenreServlet.java
    - ##### GetCartServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7e9ed1e4115016513dc2a20824c18a4e872573e0/src/GetCartServlet.java
    - ##### LoginServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/71c5df8245d72b83cbd994db82a17bdddd76dce2/src/LoginServlet.java
    - ##### MainServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/71c5df8245d72b83cbd994db82a17bdddd76dce2/src/MainServlet.java
    - ##### MovieCastParser: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/fe6bd6e54e19e45c53173db3d9707ab70425a3c2/src/MovieCastParser.java
    - ##### MovieCastParserMain: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/c4916f07b4540db7268c5a113fface7da8a65e57/src/MovieCastParserMain.java
    - ##### MovieSAXParser: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/b00e1982b925fa9bdc65c7d472e9c68577e19968/src/MovieSAXParser.java
    - ##### MovieSAXParserMain: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/c4916f07b4540db7268c5a113fface7da8a65e57/src/MovieSAXParserMain.java
    - ##### MoviesServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7ec0e7ec38b7cecbc1b0078a3173cb6068a6e05f/src/MoviesServlet.java
    - ##### MovieSuggestion: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/24228483744d56b12e25dc109a7013f2e95bb4ff/src/MovieSuggestion.java
    - ##### PlaceOrderServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/f362d20e9dc4b467016e405775bd0326e68665e0/src/PlaceOrderServlet.java
    - ##### SingleMovieServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/5feaed92f82fba135b08963c58d286d54a6faae7/src/SingleMovieServlet.java
    - ##### SingleStarServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/887a84b91a1dd3208f5e339e40e0b69bd1d380ae/src/SingleStarServlet.java
    - ##### TitleServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/1734c44a27d2476ec05389cf21f4f01b56178d86/src/TitleServlet.java
    - ##### UpdateCartItemServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7e9ed1e4115016513dc2a20824c18a4e872573e0/src/UpdateCartItemServlet.java
    - ##### UpdateEmployeePassword: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/1fe84e8e801e558db5af27c826f98836c7ba56ee/src/UpdateEmployeePassword.java

  - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - ##### Connection pooling is implemented in Fabflix to improve database performance and resource management. Instead of creating a new database connection for every request, the application uses a pool of reusable connections. These connections are fetched from the pool when needed and returned after use, significantly reducing the overhead of repeatedly opening and closing connections. This is particularly useful in high-traffic scenarios where multiple requests need to access the database simultaneously.
    - ##### In the Fabflix code: 
      - ###### A DataSource object is used to manage the connection pool. It is configured in the context.xml file for Tomcat. 
      - ###### Servlets, such as SearchServlet, obtain a connection from the pool using DataSource.getConnection() instead of creating a new connection with DriverManager. 
      - ###### Connection pooling ensures that database connections are properly managed and returned to the pool after each use, avoiding resource leaks.
  
  - #### Explain how Connection Pooling works with two backend SQL.
    - ##### Explanation
      - ###### Connection pooling in Fabflix enables efficient database operations by maintaining separate pools of reusable connections for multiple backend SQL databases. This approach ensures optimized resource utilization and avoids the overhead of creating and closing connections repeatedly.
      
    - ##### LoginServlet
      - ###### Connection Pool: Configured as jdbc/moviedb for the primary database.
      - ###### Usage: The servlet retrieves a connection from the pool to validate user credentials.
    - ##### SingleMovieServlet
      - ###### Interacts with the primary database using connection pooling.
      - ###### Retrieves a connection from the jdbc/moviedb pool for querying movie details.

- ### Prepared Statements
  - #### Include the filename/path of all code/configuration files in GitHub of using Prepared Statements.
    - ##### ActorParser: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/527c2cf065199db49b8d0d9661b5d9776a9d9f59/src/ActorParser.java
    - ##### AddStarServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/d6e6ba38f246cc85a9b3903f99810daada4cd9c5/src/AddStarServlet.java
    - ##### AddToCartServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/ade5769274632e321876dd8d857381a8745122a6/src/AddToCartServlet.java
    - ##### DeleteCartItemServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7e9ed1e4115016513dc2a20824c18a4e872573e0/src/DeleteCartItemServlet.java
    - ##### FormRecaptcha: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/0c4f03f15914e6f699e38727547f1013afaf867b/src/FormRecaptcha.java
    - ##### GenreServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/1734c44a27d2476ec05389cf21f4f01b56178d86/src/GenreServlet.java
    - ##### GetCartServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7e9ed1e4115016513dc2a20824c18a4e872573e0/src/GetCartServlet.java
    - ##### LoginServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/71c5df8245d72b83cbd994db82a17bdddd76dce2/src/LoginServlet.java
    - ##### MainServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/71c5df8245d72b83cbd994db82a17bdddd76dce2/src/MainServlet.java
    - ##### MovieCastParser: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/fe6bd6e54e19e45c53173db3d9707ab70425a3c2/src/MovieCastParser.java
    - ##### MovieSAXParser: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/b00e1982b925fa9bdc65c7d472e9c68577e19968/src/MovieSAXParser.java
    - ##### MoviesServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7ec0e7ec38b7cecbc1b0078a3173cb6068a6e05f/src/MoviesServlet.java
    - ##### MovieSuggestion: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/24228483744d56b12e25dc109a7013f2e95bb4ff/src/MovieSuggestion.java
    - ##### PlaceOrderServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/f362d20e9dc4b467016e405775bd0326e68665e0/src/PlaceOrderServlet.java
    - ##### SingleMovieServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/5feaed92f82fba135b08963c58d286d54a6faae7/src/SingleMovieServlet.java
    - ##### SingleStarServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/887a84b91a1dd3208f5e339e40e0b69bd1d380ae/src/SingleStarServlet.java
    - ##### UpdateCartItemServlet: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/7e9ed1e4115016513dc2a20824c18a4e872573e0/src/UpdateCartItemServlet.java
    - ##### UpdateEmployeePassword: https://github.com/uci-jherold2-fall24-cs122b/2024-fall-cs-122b-emily-shelly-goodjob/blob/1fe84e8e801e558db5af27c826f98836c7ba56ee/src/UpdateEmployeePassword.java

  - #### Explain how Prepared Statements is utilized in the Fabflix code.
    - ##### Prepared Statements automatically escape user inputs, preventing SQL injection attacks.
    - ##### Example: In SearchServlet, user-provided search terms are passed as parameters to a PreparedStatement instead of being concatenated into a raw SQL query.

  - #### Explain how Prepared Statements works with two backend SQL.
    - ##### Example: SearchServlet
      - ###### In SearchServlet, a PreparedStatement is used to perform a full-text search for movies:
      ```
      String sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE)";
      try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ftQuery.toString());
            ResultSet rs = ps.executeQuery();

          // Process the results
      }
      ```
      - ###### The ? placeholder in the query is replaced by the user input using ps.setString(1, ftQuery.toString()), ensuring the input is safely handled.
      
    - ##### Example: SingleMovieServlet
      - ###### This servlet retrieves movie details from the archived database using a PreparedStatement:
      ```
      String query = "SELECT title, year, director FROM archived_movies WHERE id = ?";
      try (Connection conn = archivedDataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();
            // Process the result
      }
      ```
      - ###### PreparedStatement ensures consistent query execution and parameter handling, regardless of the database.
