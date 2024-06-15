import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DataManager_aggregateDonationByFund_Test {

    @Test
    public void testAggregateDonationsByFund() {
        // Create a few Donation objects
        Donation donation1 = new Donation("fund1", "Contributor1", 100, "2024-01-01");
        Donation donation2 = new Donation("fund2", "Contributor1", 50, "2024-01-02");
        Donation donation3 = new Donation("fund1", "Contributor2", 200, "2024-01-03");
        Donation donation4 = new Donation("fund2", "Contributor2", 75, "2024-01-04");
        Donation donation5 = new Donation("fund1", "Contributor1", 150, "2024-01-05");

        // Add donations to a list
        List<Donation> donations = Arrays.asList(donation1, donation2, donation3, donation4, donation5);

        // Create a DataManager instance
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        // Call the aggregateDonationsByFund method
        Map<String, DataManager.FundAggregation> fundAggregations = dm.aggregateDonationsByFund(donations);

        // Validate the aggregation results
        assertNotNull(fundAggregations);
        assertEquals(2, fundAggregations.size());

        // Validate fund1 aggregation
        DataManager.FundAggregation fund1Aggregation = fundAggregations.get("fund1");
        assertNotNull(fund1Aggregation);
        assertEquals("fund1", fund1Aggregation.getFundId());
        assertEquals(3, fund1Aggregation.getNumberOfDonations());
        assertEquals(450, fund1Aggregation.getTotalAmount());

        // Validate fund2 aggregation
        DataManager.FundAggregation fund2Aggregation = fundAggregations.get("fund2");
        assertNotNull(fund2Aggregation);
        assertEquals("fund2", fund2Aggregation.getFundId());
        assertEquals(2, fund2Aggregation.getNumberOfDonations());
        assertEquals(125, fund2Aggregation.getTotalAmount());
    }

    @Test
    public void testEmptyDonationsList() {
        // Create an empty list of donations
        List<Donation> donations = Collections.emptyList();

        // Create a DataManager instance
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        // Call the aggregateDonationsByFund method
        Map<String, DataManager.FundAggregation> fundAggregations = dm.aggregateDonationsByFund(donations);

        // Validate the aggregation results
        assertNotNull(fundAggregations);
        assertTrue(fundAggregations.isEmpty());
    }

    @Test
    public void testSingleDonation() {
        // Create a single Donation object
        Donation donation = new Donation("fund1", "Contributor1", 100, "2024-01-01");

        // Add donation to a list
        List<Donation> donations = Collections.singletonList(donation);

        // Create a DataManager instance
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        // Call the aggregateDonationsByFund method
        Map<String, DataManager.FundAggregation> fundAggregations = dm.aggregateDonationsByFund(donations);

        // Validate the aggregation results
        assertNotNull(fundAggregations);
        assertEquals(1, fundAggregations.size());

        // Validate fund1 aggregation
        DataManager.FundAggregation fund1Aggregation = fundAggregations.get("fund1");
        assertNotNull(fund1Aggregation);
        assertEquals("fund1", fund1Aggregation.getFundId());
        assertEquals(1, fund1Aggregation.getNumberOfDonations());
        assertEquals(100, fund1Aggregation.getTotalAmount());
    }

    @Test
    public void testMultipleDonationsToSameFund() {
        // Create Donation objects for the same fund
        Donation donation1 = new Donation("fund1", "Contributor1", 100, "2024-01-01");
        Donation donation2 = new Donation("fund1", "Contributor2", 200, "2024-01-02");

        // Add donations to a list
        List<Donation> donations = Arrays.asList(donation1, donation2);

        // Create a DataManager instance
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        // Call the aggregateDonationsByFund method
        Map<String, DataManager.FundAggregation> fundAggregations = dm.aggregateDonationsByFund(donations);

        // Validate the aggregation results
        assertNotNull(fundAggregations);
        assertEquals(1, fundAggregations.size());

        // Validate fund1 aggregation
        DataManager.FundAggregation fund1Aggregation = fundAggregations.get("fund1");
        assertNotNull(fund1Aggregation);
        assertEquals("fund1", fund1Aggregation.getFundId());
        assertEquals(2, fund1Aggregation.getNumberOfDonations());
        assertEquals(300, fund1Aggregation.getTotalAmount());
    }

    @Test
    public void testDifferentFunds() {
        // Create Donation objects for different funds
        Donation donation1 = new Donation("fund1", "Contributor1", 100, "2024-01-01");
        Donation donation2 = new Donation("fund2", "Contributor1", 200, "2024-01-02");

        // Add donations to a list
        List<Donation> donations = Arrays.asList(donation1, donation2);

        // Create a DataManager instance
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        // Call the aggregateDonationsByFund method
        Map<String, DataManager.FundAggregation> fundAggregations = dm.aggregateDonationsByFund(donations);

        // Validate the aggregation results
        assertNotNull(fundAggregations);
        assertEquals(2, fundAggregations.size());

        // Validate fund1 aggregation
        DataManager.FundAggregation fund1Aggregation = fundAggregations.get("fund1");
        assertNotNull(fund1Aggregation);
        assertEquals("fund1", fund1Aggregation.getFundId());
        assertEquals(1, fund1Aggregation.getNumberOfDonations());
        assertEquals(100, fund1Aggregation.getTotalAmount());

        // Validate fund2 aggregation
        DataManager.FundAggregation fund2Aggregation = fundAggregations.get("fund2");
        assertNotNull(fund2Aggregation);
        assertEquals("fund2", fund2Aggregation.getFundId());
        assertEquals(1, fund2Aggregation.getNumberOfDonations());
        assertEquals(200, fund2Aggregation.getTotalAmount());
    }
}

