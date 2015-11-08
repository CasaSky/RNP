package base64Encode;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

/**
 * Created by Talal on 01.11.2015.
 */
public class EncodeWithBase64 {

    private EncodeWithBase64(){}

    public static String encodeFile(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        byte[] binaryData = Files.readAllBytes(path);
        return Base64.encodeBytes(binaryData);
        //return Base64.encodeFromFile("Z:\\Dokumente\\RN\\Praktikum1");
    }

    public static String encodeString(String text) {
        return Base64.encodeBytes(text.getBytes());
    }
    public static byte[] writeInBytes(String text) { return text.getBytes();}
}
