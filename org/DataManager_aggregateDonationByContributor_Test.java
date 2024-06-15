import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class DataManager_aggregateDonationByContributor_Test {

    private DataManager dataManager;
    private Fund fund;

    @Before
    public void setUp() {
        dataManager = new DataManager(new WebClient("localhost", 3001));

        fund = new Fund("1", "Test Fund", "Description", 10000);
        fund.setDonations(Arrays.asList(
                new Donation("1", "Alice", 100, "2023-06-01T12:00:00.000Z"),
                new Donation("1", "Bob", 200, "2023-06-02T12:00:00.000Z"),
                new Donation("1", "Alice", 300, "2023-06-03T12:00:00.000Z"),
                new Donation("1", "Charlie", 400, "2023-06-04T12:00:00.000Z"),
                new Donation("1", "Bob", 500, "2023-06-05T12:00:00.000Z")
        ));
    }

    @Test
    public void testAggregateDonation() {
        Map<String, DataManager.ContributorAggregate> result = dataManager.aggregateDonationByContributor(fund);

        assertEquals(3, result.size());

        DataManager.ContributorAggregate alice = result.get("Alice");
        assertNotNull(alice);
        assertEquals("Alice", alice.getName());
        assertEquals(2, alice.getDonationCount());
        assertEquals(400, alice.getTotalAmount());

        DataManager.ContributorAggregate bob = result.get("Bob");
        assertNotNull(bob);
        assertEquals("Bob", bob.getName());
        assertEquals(2, bob.getDonationCount());
        assertEquals(700, bob.getTotalAmount());

        DataManager.ContributorAggregate charlie = result.get("Charlie");
        assertNotNull(charlie);
        assertEquals("Charlie", charlie.getName());
        assertEquals(1, charlie.getDonationCount());
        assertEquals(400, charlie.getTotalAmount());
    }

    @Test
    public void testAggregateDonationEmptyFund() {
        Fund emptyFund = new Fund("2", "Empty Fund", "No donations", 5000);

        Map<String, DataManager.ContributorAggregate> result = dataManager.aggregateDonationByContributor(emptyFund);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAggregateDonationSingleDonation() {
        Fund singleDonationFund = new Fund("3", "Single Donation Fund", "One donation", 2000);
        singleDonationFund.setDonations(Collections.singletonList(
                new Donation("3", "David", 1000, "2023-07-01T12:00:00.000Z")
        ));

        Map<String, DataManager.ContributorAggregate> result = dataManager.aggregateDonationByContributor(singleDonationFund);

        assertEquals(1, result.size());

        DataManager.ContributorAggregate david = result.get("David");
        assertNotNull(david);
        assertEquals("David", david.getName());
        assertEquals(1, david.getDonationCount());
        assertEquals(1000, david.getTotalAmount());
    }
}

