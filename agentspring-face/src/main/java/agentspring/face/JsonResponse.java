package agentspring.face;

import java.util.HashMap;

public class JsonResponse extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public JsonResponse() {
        super();
    }

    public JsonResponse(boolean success) {
        this();
        this.put("success", success);
    }

    public void setError(String error) {
        this.put("error", error);
        this.put("success", false);
    }

    public void setSuccess(boolean success) {
        this.put("success", success);
    }
}
