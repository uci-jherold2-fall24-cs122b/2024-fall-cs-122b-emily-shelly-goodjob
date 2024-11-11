import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieCastParser extends DefaultHandler {

    private Map<String, String> existingActorsCache; // Stores <name_birthYear, id>
    private Map<String, String> existingRolesCache;
    private String tempVal;
    private Connection dbConnection;
    private ExecutorService executorService;
    private Actor currentActor;
    private boolean isRole = false;
    private String currentMovieId;

    public MovieCastParser(Connection dbConnection, Map<String, String> actorsCache, Map<String, String> rolesCache) {
        this.dbConnection = dbConnection;
        this.existingActorsCache = actorsCache;
        this.existingRolesCache = rolesCache;
        this.executorService = Executors.newFixedThreadPool(8);
    }

    public void parseDocument(String filePath) {
        System.out.println("Parsing document: " + filePath);
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse(new File(filePath), this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            System.out.println("New movie entry detected.");
        } else if (qName.equalsIgnoreCase("a")) {
            currentActor = new Actor();
            System.out.println("New actor detected.");
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("a")) {
            if (currentActor.isValid()) {
                System.out.println("Valid actor: " + currentActor);
                executorService.submit(() -> addActorAndLinkToMovie(currentActor, currentMovieId));
            } else {
                System.err.println("Skipping actor due to missing data: " + currentActor);
            }
        } else if (qName.equalsIgnoreCase("f")) {
            currentMovieId = tempVal.trim();
            System.out.println("Movie ID: " + currentMovieId);
        }
    }

    private void addActorAndLinkToMovie(Actor actor, String movieId) {
        String actorKey = actor.getKey();

        if (!existingActorsCache.containsKey(actorKey)) {
            // If the actor is not in the cache, check and insert into database
            try (PreparedStatement checkStmt = dbConnection.prepareStatement(
                    "SELECT id FROM stars WHERE name = ? AND (birthYear = ? OR birthYear IS NULL AND ? IS NULL)")) {
                checkStmt.setString(1, actor.getName());
                if (actor.getBirthYear() != null) {
                    checkStmt.setInt(2, actor.getBirthYear());
                    checkStmt.setInt(3, actor.getBirthYear());
                } else {
                    checkStmt.setNull(2, java.sql.Types.INTEGER);
                    checkStmt.setNull(3, java.sql.Types.INTEGER);
                }

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Actor already exists, update cache with their ID
                        String existingId = rs.getString("id");
                        existingActorsCache.put(actorKey, existingId);
                        actor.setId(existingId);
                    } else {
                        // Insert new actor
                        actor.setId(generateUniqueId());
                        try (PreparedStatement insertStmt = dbConnection.prepareStatement(
                                "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)")) {
                            insertStmt.setString(1, actor.getId());
                            insertStmt.setString(2, actor.getName());
                            if (actor.getBirthYear() != null) {
                                insertStmt.setInt(3, actor.getBirthYear());
                            } else {
                                insertStmt.setNull(3, java.sql.Types.INTEGER);
                            }
                            insertStmt.executeUpdate();
                            System.out.println("Inserted new actor: " + actor);
                            existingActorsCache.put(actorKey, actor.getId());
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error checking or inserting actor: " + actor);
                e.printStackTrace();
            }
        } else {
            actor.setId(existingActorsCache.get(actorKey));
        }

        // Link actor to movie in stars_in_movies
        linkActorToMovie(actor.getId(), movieId);
    }

    private void linkActorToMovie(String starId, String movieId) {
        try (PreparedStatement checkLinkStmt = dbConnection.prepareStatement(
                "SELECT COUNT(*) FROM stars_in_movies WHERE starId = ? AND movieId = ?")) {
            checkLinkStmt.setString(1, starId);
            checkLinkStmt.setString(2, movieId);
            try (ResultSet rs = checkLinkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // If no link exists, create one
                    try (PreparedStatement insertLinkStmt = dbConnection.prepareStatement(
                            "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)")) {
                        insertLinkStmt.setString(1, starId);
                        insertLinkStmt.setString(2, movieId);
                        insertLinkStmt.executeUpdate();
                        System.out.println("Linked actor ID " + starId + " to movie ID " + movieId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error linking actor to movie: " + starId + " -> " + movieId);
            e.printStackTrace();
        }
    }

    private String generateUniqueId() {
        String id;
        boolean isUnique;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            isUnique = checkIdUniqueness(id);
        } while (!isUnique);
        return id;
    }

    private boolean checkIdUniqueness(String id) {
        try (PreparedStatement ps = dbConnection.prepareStatement("SELECT COUNT(*) FROM stars WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private class Actor {
        private String id;
        private String name;
        private Integer birthYear;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getBirthYear() { return birthYear; }
        public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

        public boolean isValid() { return name != null && !name.isEmpty(); }

        public String getKey() {
            return name + "_" + (birthYear != null ? birthYear : "NULL");
        }

        @Override
        public String toString() { return name + (birthYear != null ? " (" + birthYear + ")" : ""); }
    }
}
