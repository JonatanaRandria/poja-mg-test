package school.hei.poja.service.rest.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteConverter {
  public void byteToFile(byte[] byteArray, String filePath) {
    try {
      // Create a new File object with the specified file path
      File file = new File(filePath);

      // Create a new FileOutputStream with the File object
      FileOutputStream fos = new FileOutputStream(file);

      // Write the bytes from the byte array to the FileOutputStream
      fos.write(byteArray);

      // Close the FileOutputStream
      fos.close();

      // Print a message indicating successful conversion

      // Now you can use the 'file' object for further processing,
      // such as uploading to a server or performing other operations

    } catch (IOException e) {
      // Handle any IO exceptions
      e.printStackTrace();
    }
  }
}
