import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class DataManager_makeContributorPasswordUpdateRequest_Test {

    private DataManager dataManager;

    @Before
    public void setUp() {
        dataManager = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                if (resource.equals("/updateContributorPassword")) {
                    return "{\"status\":\"success\"}";
                }
                return null;
            }
        });
    }

    @Test
    public void testSuccess() {
        dataManager.makeContributorPasswordUpdateRequest("contrib123", "newPassword123");
    }

    @Test(expected = IllegalStateException.class)
    public void testFailure() {
        dataManager = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                if (resource.equals("/updateContributorPassword")) {
                    return "{\"status\":\"failure\", \"error\": \"Invalid contributor ID\"}";
                }
                return null;
            }
        });

        dataManager.makeContributorPasswordUpdateRequest("invalidContribId", "newPassword123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullId() {
        dataManager.makeContributorPasswordUpdateRequest(null, "newPassword123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPswd() {
        dataManager.makeContributorPasswordUpdateRequest("contrib123", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testNullResponse() {
        dataManager = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return null;
            }
        });

        dataManager.makeContributorPasswordUpdateRequest("contrib123", "newPassword123");
    }
}
