import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

public class DataManager_attemptLogin_Test {

    /*
     * This is a test class for the DataManager.attemptLogin method.
     * Add more tests here for this method as needed.
     *
     * When writing tests for other methods, be sure to put them into separate
     * JUnit test classes.
     */

    @Test
    public void testSuccessfulLogin() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"success\",\"data\":{\"_id\":\"org123\",\"name\":\"Test Org\",\"description\":\"A test organization\",\"funds\":[]}}";
            }
        });

        Organization org = dm.attemptLogin("testLogin", "testPassword");

        assertNotNull(org);
        assertEquals("org123", org.getId());
        assertEquals("Test Org", org.getName());
        assertEquals("A test organization", org.getDescription());
    }

    @Test
    public void testFailedLogin() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"failure\"}";
            }
        });

        Organization org = dm.attemptLogin("testLogin", "testPassword");

        assertNull(org);
    }

    @Test
    public void testExceptionHandling() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                throw new RuntimeException("Simulated exception");
            }
        });

        Organization org = dm.attemptLogin("testLogin", "testPassword");

        assertNull(org);
    }

    @Test
    public void testSuccessfulLoginWithFunds() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"success\",\"data\":{\"_id\":\"org123\",\"name\":\"Test Org\",\"description\":\"A test organization\",\"funds\":[{\"_id\":\"fund123\",\"name\":\"Test Fund\",\"description\":\"A test fund\",\"target\":1000,\"donations\":[]}]}}";
            }
        });

        Organization org = dm.attemptLogin("testLogin", "testPassword");

        assertNotNull(org);
        assertEquals("org123", org.getId());
        assertEquals("Test Org", org.getName());
        assertEquals("A test organization", org.getDescription());
        assertEquals(1, org.getFunds().size());

        Fund fund = org.getFunds().get(0);
        assertEquals("fund123", fund.getId());
        assertEquals("Test Fund", fund.getName());
        assertEquals("A test fund", fund.getDescription());
        assertEquals(1000, fund.getTarget());
        assertTrue(fund.getDonations().isEmpty());
    }

    @Test
    public void testSuccessfulLoginWithFundsAndDonations() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                if (resource.equals("/findOrgByLoginAndPassword")) {
                    return "{\"status\":\"success\",\"data\":{\"_id\":\"org123\",\"name\":\"Test Org\",\"description\":\"A test organization\",\"funds\":[{\"_id\":\"fund123\",\"name\":\"Test Fund\",\"description\":\"A test fund\",\"target\":1000,\"donations\":[{\"contributor\":\"contrib123\",\"amount\":500,\"date\":\"2024-01-01\"}]}]}}";
                } else if (resource.equals("/findContributorNameById")) {
                    return "{\"status\":\"success\",\"data\":{\"name\":\"Test Contributor\"}}";
                }
                return null;
            }
        });

        Organization org = dm.attemptLogin("testLogin", "testPassword");

        assertNotNull(org);
        assertEquals("org123", org.getId());
        assertEquals("Test Org", org.getName());
        assertEquals("A test organization", org.getDescription());
        assertEquals(1, org.getFunds().size());

        Fund fund = org.getFunds().get(0);
        assertEquals("fund123", fund.getId());
        assertEquals("Test Fund", fund.getName());
        assertEquals("A test fund", fund.getDescription());
        assertEquals(1000, fund.getTarget());
        assertEquals(1, fund.getDonations().size());

        Donation donation = fund.getDonations().get(0);
        assertEquals("fund123", donation.getFundId());
        assertEquals("Test Contributor", donation.getContributorName());
        assertEquals(500, donation.getAmount());
        assertEquals("2024-01-01", donation.getDate());
    }
}
