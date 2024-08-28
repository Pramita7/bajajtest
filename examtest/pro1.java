import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class HashGenerator {

    private static final int RANDOM_STRING_LENGTH = 8;
    private static final Random random = new Random();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar test.jar <PRN Number> <path to json file>");
            System.exit(1);
        }

        String prnNumber = args[0].trim().toLowerCase();
        String jsonFilePath = args[1].trim();

        String destinationValue = getDestinationValue(jsonFilePath);
        if (destinationValue == null) {
            System.err.println("Key 'destination' not found in the JSON file.");
            System.exit(1);
        }

        String randomString = generateRandomString(RANDOM_STRING_LENGTH);
        String concatenatedString = prnNumber + destinationValue + randomString;
        String md5Hash = DigestUtils.md5Hex(concatenatedString);

        System.out.println(md5Hash + ";" + randomString);
    }

    private static String getDestinationValue(String jsonFilePath) {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            return findDestinationValue(jsonElement);
        } catch (IOException e) {
            System.err.println("Error reading JSON file.");
            System.exit(1);
        }
        return null;
    }

    private static String findDestinationValue(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("destination")) {
                return jsonObject.get("destination").getAsString();
            }
            for (JsonElement childElement : jsonObject.values()) {
                String result = findDestinationValue(childElement);
                if (result != null) {
                    return result;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                String result = findDestinationValue(arrayElement);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
