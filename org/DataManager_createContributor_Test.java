import static org.junit.Assert.*;

import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Test;

public class DataManager_createContributor_Test {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateContributorWithNullFields() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return null;
            }
        });
        dm.createContributor(null, "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
    }

    @Test
    public void testCreateContributorSuccess() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                JSONObject response = new JSONObject();
                response.put("status", "success");
                return response.toString();
            }
        });

        boolean result = dm.createContributor("login", "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
        assertTrue(result);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateContributorWebClientNullResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return null;
            }
        });

        dm.createContributor("login", "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateContributorWebClientErrorStatus() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                JSONObject response = new JSONObject();
                response.put("status", "error");
                response.put("message", "Some error message");
                return response.toString();
            }
        });

        dm.createContributor("login", "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateContributorWebClientErrorStatusNoMessage() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                JSONObject response = new JSONObject();
                response.put("status", "error");
                return response.toString();
            }
        });

        dm.createContributor("login", "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateContributorMalformedJSONResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "invalid JSON";
            }
        });

        dm.createContributor("login", "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateContributorExceptionInCommunication() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                throw new RuntimeException("Test exception");
            }
        });

        dm.createContributor("login", "password", "name", "email", "creditCardNumber", "cvv", "expMonth", "expYear", "zipCode");
    }
}
