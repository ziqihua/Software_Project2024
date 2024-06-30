import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataManager {

	private final WebClient client;
	public String HASH_ALGORITHM;
	private final String SALT;
	public Map<String, String> loginContributorCache;
	private Map<String, ContributorAggregate> aggregateContributor = null;
	private Map<String, FundAggregation> aggregationFund = null;

	public DataManager(WebClient client) {
		if (client == null) {
			throw new IllegalStateException("WebClient cannot be null");
		}
		this.client = client;
		this.SALT = "PublicSalt2357039275";
		this.HASH_ALGORITHM = "SHA-256";
		this.loginContributorCache = new HashMap<>();
	}

	/**
	 * Attempt to log the user into an Organization account using the login and password.
	 * This method uses the /findOrgByLoginAndPassword endpoint in the API
	 * @return an Organization object if successful; null if unsuccessful
	 */
	public Organization attemptLogin(String login, String password) {
		if (login == null || password == null) {
			throw new IllegalArgumentException("Login and password cannot be null");
		}

		try {
			JSONObject json = makeLoginRequest(login, password);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject data = (JSONObject) json.get("data");
				String orgId = (String) data.get("_id");
				String name = (String) data.get("name");
				String description = (String) data.get("description");
				Organization org = new Organization(orgId, name, description);

				JSONArray funds = (JSONArray) data.get("funds");
				Iterator<?> it = funds.iterator();
				while (it.hasNext()) {
					JSONObject fund = (JSONObject) it.next();
					String fundId = (String) fund.get("_id");
					String fundName = (String) fund.get("name");
					String fundDescription = (String) fund.get("description");
					long target = (Long) fund.get("target");

					Fund newFund = new Fund(fundId, fundName, fundDescription, target);

					JSONArray donations = (JSONArray) fund.get("donations");
					List<Donation> donationList = new LinkedList<>();
					Iterator<?> it2 = donations.iterator();
					while (it2.hasNext()) {
						JSONObject donation = (JSONObject) it2.next();
						String contributorId = (String) donation.get("contributor");
						String contributorName = this.getContributorName(contributorId);

						long amount = (Long) donation.get("amount");
						String date = (String) donation.get("date");
						donationList.add(new Donation(fundId, contributorName, amount, date));
					}

					newFund.setDonations(donationList);
					org.addFund(newFund);
				}
				return org;
			} else if (status.equals("error")) {
				throw new IllegalStateException((String) json.get("error"));
			} else {
				return null;
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Malformed JSON response", e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}

	public String hashSaltedPassword(String password) {
		// based on the tutorial found here - https://www.javaguides.net/2020/02/java-sha-256-hash-with-salt-example.html
		try {
			String combinedShaInput = this.SALT + password;
			MessageDigest md = MessageDigest.getInstance(this.HASH_ALGORITHM);
			byte[] bytes = md.digest(combinedShaInput.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte aByte : bytes) {
				sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public JSONObject makeLoginRequest(String login, String password) throws ParseException {
		if (login == null || password == null) {
			throw new IllegalArgumentException("Login and password cannot be null");
		}

		String digest = hashSaltedPassword(password);
		Map<String, Object> map = new HashMap<>();
		map.put("login", login);
		map.put("password", digest);
		String response = client.makeRequest("/findOrgByLoginAndPassword", map);

		if (response == null) {
			throw new IllegalStateException("WebClient returned null response");
		}

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(response);
		return json;
	}

	// makes an API call to update an organization's password
	// returns whether the attempt was successful
	public void makePasswordUpdateRequest(String orgId, String newPassword) {
		if (orgId == null) {
			throw new IllegalArgumentException("orgId cannot be null");
		}
		if (newPassword == null) {
			throw new IllegalArgumentException("newPassword cannot be null");
		}

		String digest = hashSaltedPassword(newPassword);
		Map<String, Object> map = new HashMap<>();
		map.put("_id", orgId);
		map.put("password", digest);
		String response = client.makeRequest("/updateOrganizationPassword", map);

		if (response == null) {
			System.out.println("WebClient returned null response");
			throw new IllegalStateException("WebClient returned null response");
		}

		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");
			if (!status.equals("success")) {
				throw new IllegalStateException((String) json.get("error"));
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}

	public void makeContributorPasswordUpdateRequest(String contrId, String newPassword) {
		if (contrId == null) {
			throw new IllegalArgumentException("contributorId cannot be null");
		}
		if (newPassword == null) {
			throw new IllegalArgumentException("newPassword cannot be null");
		}

		String digest = hashSaltedPassword(newPassword);
		Map<String, Object> map = new HashMap<>();
		map.put("_id", contrId);
		map.put("password", digest);
		String response = client.makeRequest("/updateContributorPassword", map);

		if (response == null) {
			System.out.println("WebClient returned null response");
			throw new IllegalStateException("WebClient returned null response");
		}

		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");
			if (!status.equals("success")) {
				throw new IllegalStateException((String) json.get("error"));
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}

	/**
	 * Look up the name of the contributor with the specified ID.
	 * This method uses the /findContributorNameById endpoint in the API.
	 * @return the name of the contributor on success; null if no contributor is found
	 */
	public String getContributorName(String id) {
		if (id == null) {
			throw new IllegalArgumentException("ID cannot be null");
		}

		// Task 2.1 Check the cache for the contributor name
		if (loginContributorCache.containsKey(id)) {
			return loginContributorCache.get(id);
		}

		// Contributor not found. Make a request, add it to the cache, and return it
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			String response = client.makeRequest("/findContributorNameById", map);

			if (response == null) {
				throw new IllegalStateException("WebClient returned null response");
			}

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject dataObject = (JSONObject) json.get("data");
				String name = (String) dataObject.get("name");
				loginContributorCache.put(id, name);
				return name;
			} else if (status.equals("error")) {
				throw new IllegalStateException((String) json.get("error"));
			} else {
				// Return a default or error message when contributor is not found
				return null;
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Malformed JSON response", e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}

	/**
	 * This method creates a new fund in the database using the /createFund endpoint in the API
	 * @return a new Fund object if successful; null if unsuccessful
	 */
	public Fund createFund(String orgId, String name, String description, long target) {
		if (orgId == null || name == null || description == null) {
			throw new IllegalArgumentException("orgId, name, and description cannot be null");
		}

		try {
			Map<String, Object> map = new HashMap<>();
			map.put("orgId", orgId);
			map.put("name", name);
			map.put("description", description);
			map.put("target", target);
			String response = client.makeRequest("/createFund", map);

			if (response == null) {
				throw new IllegalStateException("WebClient returned null response");
			}

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject fund = (JSONObject) json.get("data");
				String fundId = (String) fund.get("_id");
				return new Fund(fundId, name, description, target);
			} else if (status.equals("error")) {
				throw new IllegalStateException((String) json.get("error"));
			} else {
				return null;
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Malformed JSON response", e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}

	public Organization createOrganization(String name, String description) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		if (description == null) {
			throw new IllegalArgumentException("description cannot be null");
		}

		try {
			Map<String, Object> map = new HashMap<>();
			map.put("name", name);
			map.put("description", description);
			String response = client.makeRequest("/createOrg", map);

			if (response == null) {
				throw new IllegalStateException("WebClient returned null response");
			}

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject orgJson = (JSONObject) json.get("data");
				String orgId = (String) orgJson.get("_id");
				return new Organization(orgId, name, description);
			} else if (status.equals("error")) {
				throw new IllegalStateException((String) json.get("error"));
			} else {
				return null;
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Malformed JSON response", e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}



	public Map<String, ContributorAggregate> aggregateDonationByContributor(Fund fund) {
		if (aggregateContributor != null) {
			return aggregateContributor;
		}
		aggregateContributor = new HashMap<>();
		for (Donation donation : fund.getDonations()) {
			String contributorName = donation.getContributorName();
			if (!aggregateContributor.containsKey(contributorName)) {
				aggregateContributor.put(contributorName, new ContributorAggregate(contributorName));
			}
			ContributorAggregate aggregate = aggregateContributor.get(contributorName);
			aggregate.addDonation(donation.getAmount());
		}
		return aggregateContributor;
	}

	public Map<String, FundAggregation> aggregateDonationsByFund(List<Donation> donations) {
		if (aggregationFund != null) {
			return aggregationFund;
		}
		aggregationFund = new HashMap<>();
		for (Donation donation : donations) {
			String fundId = donation.getFundId();
			FundAggregation aggregation = aggregationFund.getOrDefault(fundId, new FundAggregation(fundId));
			aggregation.addDonation(donation);
			aggregationFund.put(fundId, aggregation);
		}
		return aggregationFund;
	}

	public static class ContributorAggregate {
		private String name;
		private int donationCount;
		private long totalAmount;

		public ContributorAggregate(String name) {
			this.name = name;
			this.donationCount = 0;
			this.totalAmount = 0;
		}

		public void addDonation(long amount) {
			this.donationCount++;
			this.totalAmount += amount;
		}

		public String getName() {
			return name;
		}

		public int getDonationCount() {
			return donationCount;
		}

		public long getTotalAmount() {
			return totalAmount;
		}
	}

	public static class FundAggregation {
		private String fundId;
		private int numberOfDonations;
		private long totalAmount;

		public FundAggregation(String fundId) {
			this.fundId = fundId;
			this.numberOfDonations = 0;
			this.totalAmount = 0;
		}

		public void addDonation(Donation donation) {
			this.numberOfDonations++;
			this.totalAmount += donation.getAmount();
		}

		public String getFundId() {
			return fundId;
		}

		public int getNumberOfDonations() {
			return numberOfDonations;
		}

		public long getTotalAmount() {
			return totalAmount;
		}
	}

	public boolean createContributor(String login, String password, String name, String email, String creditCardNumber,
									 String cvv, String expMonth, String expYear, String zipCode) {
		// Check for null values and throw an IllegalArgumentException if any are null
		if (login == null || password == null || name == null || email == null || creditCardNumber == null ||
				cvv == null || expMonth == null || expYear == null || zipCode == null) {
			throw new IllegalArgumentException("All fields must be provided.");
		}

		try {
			String digest = hashSaltedPassword(password);
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			map.put("password", digest);
			map.put("name", name);
			map.put("email", email);
			map.put("creditCardNumber", creditCardNumber);
			map.put("cvv", cvv);
			map.put("expMonth", expMonth);
			map.put("expYear", expYear);
			map.put("zipCode", zipCode);

			String response = client.makeRequest("/createContributor", map);

			if (response == null) {
				throw new IllegalStateException("WebClient returned null response");
			}

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				return true;
			} else {
				String message = (String) json.get("message");
				if (message == null) {
					message = "An unknown error occurred.";
				}
				throw new IllegalStateException(message);
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Malformed JSON response", e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server", e);
		}
	}
}
