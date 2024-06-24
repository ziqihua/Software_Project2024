import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_createOrganization_Test {

    @Test
    public void testSuccessfulCreation() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"success\",\"data\":{\"_id\":\"12345\",\"name\":\"new organization\",\"description\":\"new organization\"}}";
            }
        });

        Organization org = dm.createOrganization("new organization", "new organization");

        assertNotNull(org);
        assertEquals("new organization", org.getName());
        assertEquals("new organization", org.getDescription());
        assertEquals("12345", org.getId());
    }

    @Test
    public void testNullName() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        assertThrows(IllegalArgumentException.class, () -> {
            dm.createOrganization(null, "description");
        });
    }

    @Test
    public void testNullDescription() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        assertThrows(IllegalArgumentException.class, () -> {
            dm.createOrganization("name", null);
        });
    }

    @Test
    public void testMalformedJSONResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "invalid JSON";
            }
        });

        assertThrows(IllegalStateException.class, () -> {
            dm.createOrganization("name", "description");
        });
    }

    @Test
    public void testWebClientReturnsNull() {

        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return null;
            }
        });
        assertThrows(IllegalStateException.class, () -> {
            dm.createOrganization("name", "description");
        });
    }

    @Test
    public void WebClientCannotConnectToServer() {

        // this assumes no server is running on port 3002
        DataManager dm = new DataManager(new WebClient("localhost", 3002));
        assertThrows(IllegalStateException.class, () -> {
            dm.createOrganization("name", "description");
        });
    }

    @Test
    public void testMakePasswordUpdateRequest_WebClientReturnsError() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"error\",\"error\":\"An unexpected database error occurred\"}";
            }
        });
        assertThrows(IllegalStateException.class, () -> {
            dm.createOrganization("name", "description");
        });
    }
}
