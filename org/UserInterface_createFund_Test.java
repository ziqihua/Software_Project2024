import org.junit.Test;

import java.util.Scanner;
import java.io.ByteArrayInputStream;
import java.util.List;

public class UserInterface_createFund_Test {

    // default stuff
    protected WebClient mockClient = new WebClient("host", 0);
    protected DataManager mockDataManager = new DataManager(mockClient){
        @Override
        public Fund createFund(String orgId, String name, String description, long target) {
            return new Fund(orgId, name, description, target);
        }
    };
    protected Organization mockOrg = new Organization("44", "msk", "description tbd");


    @Test
    public void testDefaultGoodPath() {
        String input = "fund1\nfund1 desc\n1000\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ui.createFund();
        List<Fund> funds = mockOrg.getFunds();
        Fund expected = new Fund("44", "fund1", "fund1 desc", 1000);
        assert(funds.size() == 1);
        assert(funds.get(0).getId().equals(expected.getId()));
        assert(funds.get(0).getName().equals(expected.getName()));
        assert(funds.get(0).getDescription().equals(expected.getDescription()));
        assert(funds.get(0).getTarget() == expected.getTarget());
    }

    @Test
    public void testBlankName() {
        String input = "\nfund1\nfund1 desc\n1000\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ui.createFund();
        List<Fund> funds = mockOrg.getFunds();
        Fund expected = new Fund("44", "fund1", "fund1 desc", 1000);
        assert(funds.size() == 1);
        assert(funds.get(0).getId().equals(expected.getId()));
        assert(funds.get(0).getName().equals(expected.getName()));
        assert(funds.get(0).getDescription().equals(expected.getDescription()));
        assert(funds.get(0).getTarget() == expected.getTarget());
    }
    @Test
    public void testBlankDescription() {
        String input = "fund1\n\nfund1 desc\n1000\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ui.createFund();
        List<Fund> funds = mockOrg.getFunds();
        Fund expected = new Fund("44", "fund1", "fund1 desc", 1000);
        assert(funds.size() == 1);
        assert(funds.get(0).getId().equals(expected.getId()));
        assert(funds.get(0).getName().equals(expected.getName()));
        assert(funds.get(0).getDescription().equals(expected.getDescription()));
        assert(funds.get(0).getTarget() == expected.getTarget());
    }
    @Test
    public void testNegativeTarget() {
        String input = "fund1\n\nfund1 desc\n-1000\n1000\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ui.createFund();
        List<Fund> funds = mockOrg.getFunds();
        Fund expected = new Fund("44", "fund1", "fund1 desc", 1000);
        assert(funds.size() == 1);
        assert(funds.get(0).getId().equals(expected.getId()));
        assert(funds.get(0).getName().equals(expected.getName()));
        assert(funds.get(0).getDescription().equals(expected.getDescription()));
        assert(funds.get(0).getTarget() == expected.getTarget());
    }

    @Test
    public void testNonNumericTarget() {
        String input = "fund1\n\nfund1 desc\n%800\n1000\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ui.createFund();
        List<Fund> funds = mockOrg.getFunds();
        Fund expected = new Fund("44", "fund1", "fund1 desc", 1000);
        assert(funds.size() == 1);
        assert(funds.get(0).getId().equals(expected.getId()));
        assert(funds.get(0).getName().equals(expected.getName()));
        assert(funds.get(0).getDescription().equals(expected.getDescription()));
        assert(funds.get(0).getTarget() == expected.getTarget());
    }




}
