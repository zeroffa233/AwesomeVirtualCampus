package app.vcampus.server.utility;

import org.json.JSONObject;
import java.util.UUID;

public class MessageEntry {
    private final UUID id;
    private final JSONObject message;

    public MessageEntry(UUID id, JSONObject message) {
        this.id = id;
        this.message = message;
    }

    public UUID getId() { return id; }
    public JSONObject getMessage() { return message; }
}