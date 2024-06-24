import org.json.simple.JSONObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class UserInterface_changePassword_Test {
    // default stuff
    protected WebClient mockClient = new WebClient("host", 0) {
        @Override
        public String makeRequest(String resource, Map<String, Object> queryParams) {
            return "{\"status\":\"success\"}";
        }
    };
    protected Organization mockOrg = new Organization("44", "msk", "description tbd");

    @Test
    public void test_currentPasswordEnteredIncorrectly() {
        DataManager mockDataManager = new DataManager(mockClient) {
            @Override
            public JSONObject makeLoginRequest(String orgId, String newPassword) {
                Map map = new HashMap<String, String>();
                map.put("status", "error");
                return new JSONObject(map);
            }
        };
        String input = "oldPassword\nq\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        ui.changePassword();
        System.setOut(originalOut);

        String output = outputStream.toString();
        String expected = "Enter your current password\n" +
                "Entered password is not correct\n";
        assertEquals(expected, output);
    }

    @Test
    public void test_newPasswordsDoNotMatch() {
        DataManager mockDataManager = new DataManager(mockClient) {
            @Override
            public JSONObject makeLoginRequest(String orgId, String newPassword) {
                Map map = new HashMap<String, String>();
                map.put("status", "success");
                return new JSONObject(map);
            }
        };
        String input = "oldPassword\nnewPassword\nNOT!!!newPassword\nq\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        ui.changePassword();
        System.setOut(originalOut);

        String output = outputStream.toString();
        String expected = "Enter your current password\n" +
                "Enter your new password\n" +
                "Re-Enter your new password\n" +
                "Entered new passwords do not match\n";
        assertEquals(expected, output);
    }

    @Test
    public void test_dm_throws_illegalArgumentException() {
        DataManager mockDataManager = new DataManager(mockClient) {
            @Override
            public JSONObject makeLoginRequest(String orgId, String newPassword) {
                Map map = new HashMap<String, String>();
                map.put("status", "success");
                return new JSONObject(map);
            }
            @Override
            public void makePasswordUpdateRequest(String orgId, String newPassword) {
                throw new IllegalArgumentException();
            }
        };
        String input = "oldPassword\nnewPassword\nnewPassword\nq\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        ui.changePassword();
        System.setOut(originalOut);

        String output = outputStream.toString();
        String expected = "Enter your current password\n" +
                "Enter your new password\n" +
                "Re-Enter your new password\n" +
                "Something went wrong processing your organization's information or new password. Please try again.\n";
        assertEquals(expected, output);
    }

    @Test
    public void test_dm_throws_illegalStateException() {
        DataManager mockDataManager = new DataManager(mockClient){
            @Override
            public JSONObject makeLoginRequest(String orgId, String newPassword) {
                Map map = new HashMap<String, String>();
                map.put("status", "success");
                return new JSONObject(map);
            }
            @Override
            public void makePasswordUpdateRequest(String orgId, String newPassword) {
                throw new IllegalStateException();
            }
        };
        String input = "oldPassword\nnewPassword\nnewPassword\nq\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        ui.changePassword();
        System.setOut(originalOut);

        String output = outputStream.toString();
        String expected = "Enter your current password\n" +
                "Enter your new password\n" +
                "Re-Enter your new password\n" +
                "Something went wrong communicating with the server. Please try again.\n";
        assertEquals(expected, output);
    }

    @Test
    public void test_successful_update() {
        DataManager mockDataManager = new DataManager(mockClient){
            @Override
            public JSONObject makeLoginRequest(String orgId, String newPassword) {
                Map map = new HashMap<String, String>();
                map.put("status", "success");
                return new JSONObject(map);
            }
            @Override
            public void makePasswordUpdateRequest(String orgId, String newPassword) {
                return;
            }
        };
        String input = "oldPassword\nnewPassword\nnewPassword\nq\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Scanner testScanner = new Scanner(inputStream);
        UserInterface ui = new UserInterface(mockDataManager, mockOrg);
        ui.setScanner(testScanner);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        ui.changePassword();
        System.setOut(originalOut);

        String output = outputStream.toString();
        String expected = "" +
                "Enter your current password\n" +
                "Enter your new password\n" +
                "Re-Enter your new password\n" +
                "Password successfully updated. Returning to start menu\n";
        assertEquals(expected, output);
    }
}
