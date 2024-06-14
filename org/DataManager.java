
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DataManager {

	private final WebClient client;
	private final String SALT;
	public Map<String, String> loginContributorCache;

	public DataManager(WebClient client) {
		this.client = client;
		this.SALT = "PublicSalt2357039275";
		this.loginContributorCache = new HashMap<>();
	}



	/**
	 * Attempt to log the user into an Organization account using the login and password.
	 * This method uses the /findOrgByLoginAndPassword endpoint in the API
	 * @return an Organization object if successful; null if unsuccessful
	 */
	public Organization attemptLogin(String login, String password) {

		try {
			String digest = hashSaltedPassword(password);
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			map.put("password", digest);
			String response = client.makeRequest("/findOrgByLoginAndPassword", map);

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
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

//						System.out.println(fundName);
//						System.out.println(contributorId);
//						System.out.println(contributorName + "\n");

						long amount = (Long) donation.get("amount");
						String date = (String) donation.get("date");
						donationList.add(new Donation(fundId, contributorName, amount, date));
					}

					newFund.setDonations(donationList);
					org.addFund(newFund);
				}
				return org;
			} else {
				return null;
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
		// Task 2.1 Check the cache for the contributor name
		if (loginContributorCache.containsKey(id)) {
			return loginContributorCache.get(id);
		}
		// Contributor not found. Make a request, add it to the cache, and return it
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			String response = client.makeRequest("/findContributorNameById", map);

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject dataObject = (JSONObject) json.get("data");
				String name = (String) dataObject.get("name");
				loginContributorCache.put(id, name);
				return name;
			} else {
				// Return a default or error message when contributor is not found
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	/**
	 * This method creates a new fund in the database using the /createFund endpoint in the API
	 * @return a new Fund object if successful; null if unsuccessful
	 */
	public Fund createFund(String orgId, String name, String description, long target) {
		try {

			Map<String, Object> map = new HashMap<>();
			map.put("orgId", orgId);
			map.put("name", name);
			map.put("description", description);
			map.put("target", target);
			String response = client.makeRequest("/createFund", map);

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String)json.get("status");

			if (status.equals("success")) {
				JSONObject fund = (JSONObject)json.get("data");
				String fundId = (String)fund.get("_id");
				return new Fund(fundId, name, description, target);
			}
			else return null;

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}

	public String hashSaltedPassword(String password) {
		// based on the tutorial found here - https://www.javaguides.net/2020/02/java-sha-256-hash-with-salt-example.html
		try {
			String combinedShaInput = this.SALT + password;
			MessageDigest md = MessageDigest.getInstance("SHA-256");
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

}
