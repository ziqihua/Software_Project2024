import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DataManager_getContributorName_Test {

    @Test
    public void testGetContributorNameSuccess() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {

            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                if (resource.equals("/findContributorNameById")) {
                    return "{\"status\":\"success\",\"data\":{\"name\":\"Test Contributor\"}}";
                }
                return null;
            }

        });

        String name = dm.getContributorName("contrib123");

        assertNotNull(name);
        assertEquals("Test Contributor", name);

        // Check contributor is successfully added to the cache
        assertEquals(dm.loginContributorCache.get("contrib123"), "Test Contributor");
    }

    @Test
    public void testGetContributorNameFailure() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {

            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                if (resource.equals("/findContributorNameById")) {
                    return "{\"status\":\"fail\"}";
                }
                return null;
            }

        });

        String name = dm.getContributorName("contrib123");

        assertNull(name);
    }

    @Test
    public void testGetContributorNameWithInvalidResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {

            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                if (resource.equals("/findContributorNameById")) {
                    return "invalid response";
                }
                return null;
            }

        });

        String name = dm.getContributorName("contrib123");

        assertNull(name);
    }

    @Test
    public void testGetContributorNameWithException() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {

            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                throw new RuntimeException("Test exception");
            }

        });

        String name = dm.getContributorName("contrib123");

        assertNull(name);
    }

    @Test
    public void testGetContributorNameRepeatName() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                throw new RuntimeException("Request Made, but should have used Cache instead");
            }
        });

        Map<String, String> loginContributorCache = new HashMap<String, String>();
        loginContributorCache.put("contrib123", "Test Contributor");
        dm.loginContributorCache = loginContributorCache;

        String name = dm.getContributorName("contrib123");

        assertNotNull(name);
        assertEquals("Test Contributor", name);
    }
}
