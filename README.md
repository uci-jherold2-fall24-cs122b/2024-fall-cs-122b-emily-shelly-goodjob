### CS122B Project

### Project 1 Demo video link: 
https://www.youtube.com/watch?v=BbaYB7Rwmbs

### Contribution: 
Each goes through task 1 to task 4
Task 5 & 6: Emily & Shelly


### Project 2 Demo video link:
https://www.youtube.com/watch?v=0p3Lk-sll_0

### Contribution:
Task 1 & 4: Emily + Shelly
Task 2 & 3: Emily

### Search Implementation
The search functionality allows users to find movies by title, year, director, and star's name.
- Supports substring matching for `title`, `director`, and `star` fields using `LIKE` predicate.
- Example: `SELECT * FROM movies WHERE title LIKE '%Term%'` returns movies with "Terminator" and "Terminal".
- Year only supports exact matches.


### Project 3 Demo video link:
https://www.youtube.com/watch?v=r4CbXQOFUdQ

### Contribution:
Task 1-6: Emily + Shelly

### XML Parsing Optimizations
1. **Multi-threaded Parsing with Task Queues**:
    - **Description**: Used Java’s ExecutorService to parse the XML in parallel by distributing different XML elements to multiple threads. Each thread processes a chunk of XML data and performs batch inserts into the database.
    - **Improvement**: Improved runtime by approximately 50% on large files, as multiple threads handle XML elements concurrently.

2. **Preloaded Cache for Frequent Lookups**:
    - **Description**: Loaded existing movies, stars, and genres into an in-memory cache before parsing. This allows the parser to quickly check for existing records without querying the database repeatedly.
    - **Improvement**: Reduced database query load significantly, improving processing time by around 40% due to minimized I/O operations.

### Inconsistency Report

## 1. Data Format Errors
These errors occur when the XML data does not match the expected format. For instance, `dod` values containing extraneous characters or unexpected formats.

- **Inconsistent `dod` Format**
   - **Actor:** Bonzo
   - **Issue:** `dod` contained extra characters (`1992+`) which were removed to parse the integer value.
   - **Resolution:** Converted `1992+` to `1992` and inserted it as `birthYear`.

- **Invalid Birth Year**
   - **Actor:** Darrell Zwerling
   - **Issue:** Birth year contains non-integer characters, leading to a parse failure.
   - **Resolution:** Set `birthYear` to NULL for database consistency.

## 2. Duplicate Data Conflicts
Duplicates were identified during the insertion process, where entries with the same `id` or `name` were encountered in the database.

- **Duplicate Star Entries**
   - **Star Name:** James Abrahams
   - **Message:** "Already in database."
   - **Action:** Skipped insertion to avoid duplicate `id`.

## 3. Missing or Null Values
Some entries had missing data, leading to partial insertions or records with NULL values where optional fields were omitted.

- **Actor with Missing `name`**
   - **Actor ID:** [auto-generated ID]
   - **Issue:** Missing `name` or `stagename` in XML file.
   - **Action:** Entry skipped due to incomplete data.

- **Actors without `birthYear`**
   - **Example:** F. Murray Abraham
   - **Issue:** `birthYear` not provided in XML.
   - **Resolution:** Inserted as NULL.

## 4. Relationship and Reference Errors
Some entries attempted to reference data that was missing from the primary tables, leading to orphaned records or inconsistencies.

- **Unresolved Movie Reference**
   - **Actor:** Victoria Abril
   - **Referenced Movie:** Mission Impossible (from `movie` element)
   - **Issue:** The specified movie ID does not exist in the `movies` table.
   - **Action:** Skipped linking record due to missing movie reference.

## 5. Parsing Exceptions
The following exceptions were encountered during parsing, primarily due to database connectivity issues or malformed data.

- **SQLIntegrityConstraintViolationException**
   - **Issue:** Attempted to insert duplicate primary key (`id`).
   - **Resolution:** Printed "Already in database." and skipped insertion.

- **Connection Closed Exception**
   - **Issue:** Database connection closed unexpectedly during the parsing.
   - **Resolution:** Reconnected and resumed parsing.
