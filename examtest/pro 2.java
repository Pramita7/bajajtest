import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Random;

public class ApiTest {
    private static final String API_ENDPOINT = "https://bfhldevapigw.healthrx.co.in/automation-campus/create/user";
    private static final String VALID_ROLL_NUMBER = "1";
    private static final HttpClient httpClient = HttpClientBuilder.create().build();
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("Starting API tests...");
        
        testValidInput();
        testMissingRequiredFields();
        testInvalidDataTypes();
        testDuplicatePhoneNumbers();
        testDuplicateEmailIds();
        testMissingRollNumber();
        testInvalidRollNumberFormat();
        testBoundaryValues();
        testSpecialCharacters();
        testRateLimiting();
        testDifferentHttpMethods();

        System.out.println("All tests completed.");
    }

    private static void testValidInput() {
        JsonObject data = createJsonObject("Test", "User", generateRandomPhone(), generateRandomEmail());
        HttpResponse response = sendPostRequest(data, VALID_ROLL_NUMBER);
        assert response.getStatusLine().getStatusCode() == 201 : "Expected 201, got " + response.getStatusLine().getStatusCode();
        System.out.println("Valid input test passed");
    }

    private static void testMissingRequiredFields() {
        String[] requiredFields = {"firstName", "lastName", "phoneNumber", "emailId"};
        for (String field : requiredFields) {
            JsonObject data = createJsonObject("Test", "User", generateRandomPhone(), generateRandomEmail());
            data.remove(field);

            HttpResponse response = sendPostRequest(data, VALID_ROLL_NUMBER);
            assert response.getStatusLine().getStatusCode() == 400 : "Expected 400 for missing " + field + ", got " + response.getStatusLine().getStatusCode();
        }
        System.out.println("Missing required fields test passed");
    }

    private static void testInvalidDataTypes() {
        JsonObject data = createJsonObject("Test", "User", "invalidPhone", generateRandomEmail());
        HttpResponse response = sendPostRequest(data, VALID_ROLL_NUMBER);
        assert response.getStatusLine().getStatusCode() == 400 : "Expected 400 for invalid phoneNumber, got " + response.getStatusLine().getStatusCode();
        System.out.println("Invalid data types test passed");
    }

    private static void testDuplicatePhoneNumbers() {
        long phoneNumber = generateRandomPhone();
        JsonObject data1 = createJsonObject("Test1", "User1", phoneNumber, generateRandomEmail());
        JsonObject data2 = createJsonObject("Test2", "User2", phoneNumber, generateRandomEmail());

        HttpResponse response1 = sendPostRequest(data1, VALID_ROLL_NUMBER);
        HttpResponse response2 = sendPostRequest(data2, VALID_ROLL_NUMBER);
        assert response2.getStatusLine().getStatusCode() == 400 : "Expected 400 for duplicate phone number, got " + response2.getStatusLine().getStatusCode();
        System.out.println("Duplicate phone number test passed");
    }

    private static void testDuplicateEmailIds() {
        String email = generateRandomEmail();
        JsonObject data1 = createJsonObject("Test1", "User1", generateRandomPhone(), email);
        JsonObject data2 = createJsonObject("Test2", "User2", generateRandomPhone(), email);

        HttpResponse response1 = sendPostRequest(data1, VALID_ROLL_NUMBER);
        HttpResponse response2 = sendPostRequest(data2, VALID_ROLL_NUMBER);
        assert response2.getStatusLine().getStatusCode() == 400 : "Expected 400 for duplicate emailId, got " + response2.getStatusLine().getStatusCode();
        System.out.println("Duplicate email ID test passed");
    }

    private static void testMissingRollNumber() {
        JsonObject data = createJsonObject("Test", "User", generateRandomPhone(), generateRandomEmail());
        HttpResponse response = sendPostRequest(data, null);
        assert response.getStatusLine().getStatusCode() == 401 : "Expected 401 for missing roll number, got " + response.getStatusLine().getStatusCode();
        System.out.println("Missing roll number test passed");
    }

    private static void testInvalidRollNumberFormat() {
        String[] invalidRollNumbers = {"abc", "123abc", "-123", "0"};
        JsonObject data = createJsonObject("Test", "User", generateRandomPhone(), generateRandomEmail());

        for (String rollNumber : invalidRollNumbers) {
            HttpResponse response = sendPostRequest(data, rollNumber);
            assert response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 401 : "Expected 400 or 401 for invalid roll number, got " + response.getStatusLine().getStatusCode();
        }
        System.out.println("Invalid roll number format test passed");
    }

    private static void testBoundaryValues() {
        String longString = generateRandomString(256);
        JsonObject[] boundaryData = {
            createJsonObject(longString, "User", generateRandomPhone(), generateRandomEmail()),
            createJsonObject("Test", longString, generateRandomPhone(), generateRandomEmail()),
            createJsonObject("Test", "User", 99999999999L, generateRandomEmail()),
            createJsonObject("Test", "User", 999999999L, longString + "@example.com")
        };

        for (JsonObject data : boundaryData) {
            HttpResponse response = sendPostRequest(data, VALID_ROLL_NUMBER);
            assert response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 201 : "Expected 400 or 201 for boundary value, got " + response.getStatusLine().getStatusCode();
        }
        System.out.println("Boundary values test passed");
    }

    private static void testSpecialCharacters() {
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        JsonObject data = createJsonObject(
            "Test" + specialChars,
            "User" + specialChars,
            generateRandomPhone(),
            "test" + specialChars + "@example.com"
        );
        HttpResponse response = sendPostRequest(data, VALID_ROLL_NUMBER);
        assert response.getStatusLine().getStatusCode() == 201 : "Expected 201 for special characters, got " + response.getStatusLine().getStatusCode();
        System.out.println("Special characters test passed");
    }

    private static void testRateLimiting() {
        JsonObject data = createJsonObject("Test", "User", generateRandomPhone(), generateRandomEmail());
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            HttpResponse response = sendPostRequest(data, VALID_ROLL_NUMBER);
            if (response.getStatusLine().getStatusCode() == 429) {
                System.out.println("Rate limiting detected");
                return;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Sent 10 requests in " + (endTime - startTime) / 1000.0 + " seconds without rate limiting");
    }

    private static void testDifferentHttpMethods() {
        // Implementing tests for different HTTP methods other than POST
        // For example, you might use a different HTTP client or a tool to test this
        System.out.println("Different HTTP methods test needs to be implemented");
    }

    private static HttpResponse sendPostRequest(JsonObject data, String rollNumber) {
        try {
            HttpPost request = new HttpPost(API_ENDPOINT);
            StringEntity params = new StringEntity(data.toString());
            request.addHeader("Content-Type", "application/json");
            if (rollNumber != null) {
                request.addHeader("roll-number", rollNumber);
            }
            request.setEntity(params);
            return httpClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static long generateRandomPhone() {
        return 1000000000L + random.nextInt(900000000);
    }

    private static String generateRandomEmail() {
        return "test.user" + random.nextInt(10000) + "@example.com";
    }

    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static JsonObject createJsonObject(String firstName, String lastName, long phoneNumber, String emailId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("firstName", firstName);
        jsonObject.addProperty("lastName", lastName);
        jsonObject.addProperty("phoneNumber", phoneNumber);
        jsonObject.addProperty("emailId", emailId);
        return jsonObject;
    }
}
