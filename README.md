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

### Contribution:

### XML Parsing Optimizations
1. **Multi-threaded Parsing with Task Queues**:
    - **Description**: Used Java’s ExecutorService to parse the XML in parallel by distributing different XML elements to multiple threads. Each thread processes a chunk of XML data and performs batch inserts into the database.
    - **Improvement**: Improved runtime by approximately 50% on large files, as multiple threads handle XML elements concurrently.

2. **Preloaded Cache for Frequent Lookups**:
    - **Description**: Loaded existing movies, stars, and genres into an in-memory cache before parsing. This allows the parser to quickly check for existing records without querying the database repeatedly.
    - **Improvement**: Reduced database query load significantly, improving processing time by around 40% due to minimized I/O operations.

