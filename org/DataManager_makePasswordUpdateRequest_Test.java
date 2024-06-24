import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataManager_makePasswordUpdateRequest_Test {


    @Test
    public void testSuccessPath() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"success\"}";
            }
        });
        dm.makePasswordUpdateRequest("id", "newPassword");
    }

    @Test(expected=IllegalStateException.class)
    public void testMakePasswordUpdateRequest_WebClientIsNull() {
        DataManager dm = new DataManager(null);
        dm.makePasswordUpdateRequest("login", "password");
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalStateException when WebClient is null");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMakePasswordUpdateRequest_OrgIdIsNull() {

        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.makePasswordUpdateRequest(null, "password");
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalArgumentxception when login is null");

    }

    @Test(expected=IllegalArgumentException.class)
    public void testMakePasswordUpdateRequest_PasswordIsNull() {

        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.makePasswordUpdateRequest("login", null);
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalArgumentxception when password is null");

    }

    @Test(expected=IllegalStateException.class)
    public void testMakePasswordUpdateRequest_WebClientCannotConnectToServer() {

        // this assumes no server is running on port 3002
        DataManager dm = new DataManager(new WebClient("localhost", 3002));
        dm.makePasswordUpdateRequest("login", "password");
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalStateException when WebClient cannot connect to server");

    }

    @Test(expected=IllegalStateException.class)
    public void testMakePasswordUpdateRequest_WebClientReturnsNull() {

        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return null;
            }
        });
        dm.makePasswordUpdateRequest("login", "password");
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalStateException when WebClient returns null");

    }

    @Test(expected=IllegalStateException.class)
    public void testMakePasswordUpdateRequest_WebClientReturnsError() {

        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"error\",\"error\":\"An unexpected database error occurred\"}";
            }
        });
        dm.makePasswordUpdateRequest("login", "password");
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalStateException when WebClient returns error");

    }

    @Test(expected=IllegalStateException.class)
    public void testMakePasswordUpdateRequest_WebClientReturnsMalformedJSON() {

        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "I AM NOT JSON!";
            }
        });
        dm.makePasswordUpdateRequest("login", "password");
        fail("DataManager.makePasswordUpdateRequest does not throw IllegalStateException when WebClient returns malformed JSON");

    }

}
