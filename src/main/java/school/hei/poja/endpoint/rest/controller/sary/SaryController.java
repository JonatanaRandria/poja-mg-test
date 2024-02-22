package school.hei.poja.endpoint.rest.controller.sary;

import static java.util.UUID.randomUUID;
import static school.hei.poja.file.FileHashAlgorithm.NONE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.poja.PojaGenerated;
import school.hei.poja.file.BucketComponent;
import school.hei.poja.file.FileHash;
import school.hei.poja.service.rest.SaryService;

@RestController
@AllArgsConstructor
@PojaGenerated
public class SaryController {
  BucketComponent bucketComponent;
  SaryService service;

  private static final String HEALTH_KEY = "photo/";

  @PutMapping(
      value = "/photo/{Id}",
      consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE},
      produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
  public void addNewPhoto(@PathVariable String Id, @RequestBody byte[] image) throws Exception {
    byte[] GrayImage;

    var file2BucketKey = Id + "-gray";

    GrayImage = service.toGRay(image);

    File imageToUpload = new File(Id);
    FileUtils.writeByteArrayToFile(imageToUpload, image);

    File imageGrayToUpload = new File(Id);
    FileUtils.writeByteArrayToFile(imageGrayToUpload, GrayImage);

    can_upload_file_then_download_file(imageToUpload, Id);
    can_upload_file_then_download_file(imageGrayToUpload, file2BucketKey);
  }

  @GetMapping("/photo/{id}")
  public Map<String, String> getPhotoById(@PathVariable String id) {

    Map<String, String> response = new HashMap<>();
    response.put("original_url", String.valueOf(Optional.of(can_presign(id).toString())));
    response.put(
        "transfromed_url", String.valueOf(Optional.of(can_presign(id + "-gray").toString())));

    return response;
  }

  private void writeRandomContent(File file) throws IOException {
    FileWriter writer = new FileWriter(file);
    var content = randomUUID().toString();
    writer.write(content);
    writer.close();
  }

  private File can_upload_file_then_download_file(File toUpload, String bucketKey)
      throws IOException {
    bucketComponent.upload(toUpload, bucketKey);

    var downloaded = bucketComponent.download(bucketKey);
    var downloadedContent = Files.readString(downloaded.toPath());
    var uploadedContent = Files.readString(toUpload.toPath());
    if (!uploadedContent.equals(downloadedContent)) {
      throw new RuntimeException("Uploaded and downloaded contents mismatch");
    }

    return downloaded;
  }

  private File download_file(String bucketKey) {
    return bucketComponent.download(bucketKey);
  }

  private FileHash can_upload_directory(File toUpload, String bucketKey) {
    var hash = bucketComponent.upload(toUpload, bucketKey);
    if (!NONE.equals(hash.algorithm())) {
      throw new RuntimeException("FileHashAlgorithm.NONE expected but got: " + hash.algorithm());
    }
    return hash;
  }

  private URL can_presign(String fileBucketKey) {
    return bucketComponent.presign(fileBucketKey, Duration.ofMinutes(2));
  }
}
