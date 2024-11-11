import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ActorParser extends DefaultHandler {

    private Map<String, String> existingActorsCache;
    private Map<String, String> existingMoviesCache;
    private String tempVal;
    private Connection dbConnection;
    private ExecutorService executorService;
    private Actor currentActor;

    public ActorParser(Connection dbConnection, Map<String, String> actorsCache, Map<String, String> moviesCache) {
        this.dbConnection = dbConnection;
        this.existingActorsCache = actorsCache;
        this.existingMoviesCache = moviesCache;
        this.executorService = Executors.newFixedThreadPool(8);
    }

    public void awaitTermination() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate in the specified time.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while awaiting termination.");
            executorService.shutdownNow();
        }
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

        if (qName.equalsIgnoreCase("actor")) {
            currentActor = new Actor();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            if (currentActor.isValid()) {
                executorService.submit(() -> addActorToDatabase(currentActor));
            } else {
                System.err.println("Skipping actor due to missing data: " + currentActor);
            }
        } else if (qName.equalsIgnoreCase("stagename")) {
            currentActor.setName(tempVal.trim());
        } else if (qName.equalsIgnoreCase("dod")) {
                String dod = tempVal.trim();
                if (dod.matches("\\d{4}(\\+)?")) {  // Regex to match a 4-digit year with optional '+'
                    try {
                        // Remove '+' if it exists before parsing to Integer
                        currentActor.setBirthYear(Integer.parseInt(dod.replace("+", "")));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid dod format for actor: " + currentActor.getName() + ". Using NULL for birthYear.");
                        currentActor.setBirthYear(null);
                    }
                } else {
                    System.err.println("Invalid dod format for actor: " + currentActor.getName() + ". Using NULL for birthYear.");
                    currentActor.setBirthYear(null);
                }
            }

    }

    private void addActorToDatabase(Actor actor) {
        String actorKey = actor.getKey();

        if (!existingActorsCache.containsKey(actorKey)) {
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
                        // Actor already exists
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
            } catch (SQLIntegrityConstraintViolationException e) {
                // Handle duplicate entry error
                System.out.println("Actor already in database: " + actor.getName());
            } catch (SQLException e) {
                System.err.println("Error checking or inserting actor: " + actor);
                e.printStackTrace();
            }
        } else {
            actor.setId(existingActorsCache.get(actorKey));
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
