import java.util.*;
import java.text.SimpleDateFormat;

public class UserInterface {

	private DataManager dataManager;
	private Organization org;
	private Scanner in = new Scanner(System.in);

	public UserInterface(DataManager dataManager, Organization org) {
		this.dataManager = dataManager;
		this.org = org;
	}

	// Method to set the scanner for testing purposes
	public void setScanner(Scanner scanner) {
		this.in = scanner;
	}

	public void start() {
		while (true) {
			if (org == null) {
				login();
			}

			System.out.println("\n\n");
			if (org.getFunds().size() > 0) {
				System.out.println("There are " + org.getFunds().size() + " funds in this organization:");

				int count = 1;
				for (Fund f : org.getFunds()) {
					System.out.println(count + ": " + f.getName());
					count++;
				}
				System.out.println("Enter the fund number to see more information.");
			}
			System.out.println("Enter 0 to create a new fund");
			System.out.println("Enter 'logout' to log out");
			String input = in.nextLine();

			if (input.equals("0")) {
				createFund();
			} else if (input.equalsIgnoreCase("logout")) {
				logout();
			} else if (input.toLowerCase().equals("q") || input.toLowerCase().equals("quit")) {
				System.out.println("Good bye!");
				break;
			} else {
				try {
					int option = Integer.parseInt(input);
					if (option > 0 && option <= org.getFunds().size()) {
						displayFund(option);
					} else {
						System.out.println("Invalid input. Please enter a valid fund number or 0 to create a new fund.");
					}
				} catch (NumberFormatException e) {
					System.out.println("Invalid input. Please enter a valid fund number or 0 to create a new fund.");
				}
			}
		}
	}

	public void createFund() {
		while (true) {
			try {
				String name = "";
				boolean nameSuccess = false;
				while (!nameSuccess) {
					System.out.println("Enter the fund name: ");
					name = in.nextLine().trim();
					if (name.isBlank()) {
						System.out.println("Fund names may not be blank.");
						continue;
					} else {
						nameSuccess = true;
					}
				}

				String description = "";
				boolean descriptionSuccess = false;
				while (!descriptionSuccess) {
					System.out.println("Enter the fund description: ");
					description = in.nextLine().trim();
					if (description.isBlank()) {
						System.out.println("Fund descriptions may not be blank.");
						continue;
					} else {
						descriptionSuccess = true;
					}
				}

				long target = 0;
				boolean targetSuccess = false;
				while (!targetSuccess) {
					System.out.println("Enter the fund target:");
					try {
						target = Long.parseLong(in.nextLine());
						if (target < 0) {
							System.out.println("Invalid fund target. Please enter a positive integer.");
						} else {
							targetSuccess = true;
							System.out.println("Fund target set to: " + target);
						}
					} catch (NumberFormatException e) {
						System.out.println("Invalid input. Please enter a numeric value.");
					}
				}

				Fund fund = dataManager.createFund(org.getId(), name, description, target);
				org.getFunds().add(fund);
				break; // Break out of the while loop if the operation is successful
			} catch (IllegalStateException e) {
				System.out.println("Error in communicating with server. Would you like to retry? (yes/no)");
				String retry = in.nextLine();
				if (!retry.equalsIgnoreCase("yes")) {
					break;
				}
			} catch (IllegalArgumentException e) {
				System.out.println("Error in input: " + e.getMessage());
				break;
			}
		}
	}

	public void displayFund(int fundNumber) {
		Fund fund = org.getFunds().get(fundNumber - 1);

		System.out.println("\n\n");
		System.out.println("Here is information about this fund:");
		System.out.println("Name: " + fund.getName());
		System.out.println("Description: " + fund.getDescription());
		System.out.println("Target: $" + fund.getTarget());

		List<Donation> donations = fund.getDonations();
		System.out.println("Number of donations: " + donations.size());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

		System.out.println("Enter 'c' to aggregate donations by contributors, 'f' to aggregate donations by funds: ");
		String input = in.nextLine();
		if (input.equalsIgnoreCase("c")) {
			Map<String, DataManager.ContributorAggregate> aggregates = dataManager.aggregateDonationByContributor(fund);
			aggregates.values().stream()
					.sorted(Comparator.comparingLong(DataManager.ContributorAggregate::getTotalAmount).reversed())
					.forEach(aggregate -> {
						System.out.println(aggregate.getName() + ", " + aggregate.getDonationCount() + " donations, $"
								+ aggregate.getTotalAmount() + " total");
					});
		} else if (input.equalsIgnoreCase("f")) {
			List<Donation> allDonations = new ArrayList<>();
			for (Fund f : org.getFunds()) {
				allDonations.addAll(f.getDonations());
			}
			Map<String, DataManager.FundAggregation> fundAggregates = dataManager.aggregateDonationsByFund(allDonations);
			fundAggregates.values().stream()
					.sorted(Comparator.comparingLong(DataManager.FundAggregation::getTotalAmount).reversed())
					.forEach(aggregate -> {
						System.out.println("Fund ID: " + aggregate.getFundId() + ", " + aggregate.getNumberOfDonations()
								+ " donations, $" + aggregate.getTotalAmount() + " total");
					});
		} else {
			for (Donation donation : donations) {
				String formattedDate = formatDateString(donation.getDate(), dateFormat);
				System.out.println("* " + donation.getContributorName() + ": $" + donation.getAmount() + " on " + formattedDate);
			}
		}

		long totalDonationAmount = donations.stream().mapToLong(Donation::getAmount).sum();
		double percentageAchieved = (double) totalDonationAmount / fund.getTarget() * 100;

		System.out.println("Total donation amount: $" + totalDonationAmount + " (" + String.format("%.2f", percentageAchieved) + "% of target)");

		System.out.println("Press the Enter key to go back to the listing of funds");
		in.nextLine();
	}

	private String formatDateString(String dateStr, SimpleDateFormat formatter) {
		try {
			SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
			Date date = originalFormat.parse(dateStr);
			return formatter.format(date);
		} catch (Exception e) {
			return dateStr;
		}
	}

	private void login() {
		while (true) {
			System.out.println("Enter login: ");
			String login = in.nextLine();
			System.out.println("Enter password: ");
			String password = in.nextLine();

			try {
				Organization org = dataManager.attemptLogin(login, password);

				if (org == null) {
					System.out.println("Login failed. Would you like to retry? (yes/no)");
					String retry = in.nextLine();
					if (!retry.equalsIgnoreCase("yes")) {
						break;
					}
				} else {
					this.org = org;
					break;
				}
			} catch (IllegalStateException e) {
				System.out.println("Error in communicating with server. Would you like to retry? (yes/no)");
				String retry = in.nextLine();
				if (!retry.equalsIgnoreCase("yes")) {
					break;
				}
			}
		}
	}

	private void logout() {
		this.org = null;
		System.out.println("Logged out successfully.");
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("No Arguments (login, password) passed into UserInterface main");
			return;
		}
		if (args.length != 2) {
			System.out.println("Incorrect # of arguments passed into UserInterface main");
			return;
		}

		DataManager ds = new DataManager(new WebClient("localhost", 3001));

		String login = args[0];
		String password = args[1];

		UserInterface ui = new UserInterface(ds, null);

		try {
			ui.org = ds.attemptLogin(login, password);

			if (ui.org == null) {
				System.out.println("Login failed.");
				ui.login();
			} else {
				ui.start();
			}
		} catch (IllegalStateException e) {
			System.out.println("Error in communicating with server");
			ui.login();
		}
	}
}
