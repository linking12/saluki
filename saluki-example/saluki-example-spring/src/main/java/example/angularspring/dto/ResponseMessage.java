package example.angularspring.dto;

/**
 * Used to transport messages back to the client.
 */
public class ResponseMessage {
    public enum Type {
        success, warn, error, info;
    }

    private final Type type;
    private final String text;

    public ResponseMessage(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }
}
