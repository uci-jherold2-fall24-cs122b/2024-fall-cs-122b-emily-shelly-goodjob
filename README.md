# CS122B Project

## Project 1
**Demo Video Link**: [Project 1 Demo](https://www.youtube.com/watch?v=BbaYB7Rwmbs)

#### Contribution
- Tasks 1-4: All members
- Tasks 5 & 6: Emily & Shelly

## Project 2
**Demo Video Link**: [Project 2 Demo](https://www.youtube.com/watch?v=0p3Lk-sll_0)

#### Contribution
- Tasks 1 & 4: Emily + Shelly
- Tasks 2 & 3: Emily

### Search Implementation
The search functionality enables users to find movies by:
- **Title**, **Year**, **Director**, and **Star's Name**
  - Substring Matching: Supported for `title`, `director`, and `star` fields using `LIKE` predicate.
  - Example Query: `SELECT * FROM movies WHERE title LIKE '%Term%'` retrieves movies like "Terminator" and "Terminal".
  - Exact Match: Year supports exact matches only.

## Project 3
**Demo Video Link**: [Project 3 Demo](https://www.youtube.com/watch?v=r4CbXQOFUdQ)

#### Contribution
- Tasks 1-6: Emily + Shelly

### XML Parsing Optimizations

1. **Multi-threaded Parsing with Task Queues**
  - Description: Utilized Java’s `ExecutorService` to parallelize XML parsing by distributing different XML elements across multiple threads. Each thread processes a chunk of XML data, performing batch inserts into the database.
  - Improvement: Achieved ~50% runtime reduction on large files due to concurrent processing of XML elements.

2. **Preloaded Cache for Frequent Lookups**
  - Description: Loaded existing movies, stars, and genres into an in-memory cache before parsing, enabling the parser to quickly verify existing records without frequent database queries.
  - Improvement: Reduced query load on the database, cutting processing time by around 40% through minimized I/O operations.

### Inconsistency Report

#### 1. Data Format Errors
Errors due to unexpected formats in XML data.

- **Inconsistent `dod` Format**
  - Example: Actor "Bonzo" had `dod` with extra characters (`1992+`).
  - Resolution: Converted to `1992` for parsing and inserted as `birthYear`.

- **Invalid Birth Year**
  - Example: Actor "Darrell Zwerling" had non-integer characters in `birthYear`.
  - Resolution: Set `birthYear` to NULL to maintain database consistency.

#### 2. Duplicate Data Conflicts
Duplicates detected during insertion of entries with the same `id` or `name`.

- **Duplicate Star Entries**
  - Example: "James Abrahams"
  - Resolution: Skipped duplicate insertion to avoid conflicting `id`.

#### 3. Missing or Null Values
Partial entries or records with NULL values due to omitted optional fields.

- **Actor Missing `name`**
  - Issue: Entries with missing `name` or `stagename` were skipped.

- **Actors without `birthYear`**
  - Example: "F. Murray Abraham" (missing `birthYear`).
  - Resolution: Inserted with `birthYear` as NULL.

#### 4. Relationship and Reference Errors
Orphaned records due to missing references in primary tables.

- **Unresolved Movie Reference**
  - Example: Actor "Victoria Abril" referencing a missing movie ID.
  - Resolution: Skipped linking record due to the absence of the referenced movie in `movies` table.

#### 5. Parsing Exceptions
Exceptions encountered during parsing due to data integrity or connectivity issues.

- **SQLIntegrityConstraintViolationException**
  - Issue: Attempted duplicate primary key insertion.
  - Resolution: Logged "Already in database" message and skipped insertion.

- **Connection Closed Exception**
  - Issue: Unexpected database disconnection.
  - Resolution: Reconnected to the database and resumed parsing.
