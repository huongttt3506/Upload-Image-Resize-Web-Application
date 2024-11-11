package com.example.ImageProcessing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final Path inputDirectory = Paths.get("media/input");
    private final Path outputDirectory = Paths.get("media/output");


    // Upload image
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "width", defaultValue = "640") int width,
            @RequestParam(value = "height", defaultValue = "480") int height
    ) {
        String fileName = file.getOriginalFilename();
        Map<String, Object> response = new HashMap<>();

        try {
            // Create folder if not exists
            Files.createDirectories(inputDirectory);
            Files.createDirectories(outputDirectory);

            // save uploaded file to media/input
            assert fileName != null;
            Path inputPath = inputDirectory.resolve(fileName);

            // change to bytes
            Files.write(inputPath, file.getBytes());

            // send to Flask via process Image method
            byte[] processedImageBytes = processImage(file, width, height);

            // Save the processed image to media/output
            String processedImagePath = saveProcessedImage(processedImageBytes, fileName);
            log.info("Processed image URL: {}", processedImagePath);
            // Prepare response JSON
            response.put("success", true);
            response.put("imageUrl", processedImagePath); // Return the URL for the processed image

            return ResponseEntity
                .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload and resize image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    // Method to save processed image to media/output
    private String saveProcessedImage(byte[] processedImageBytes, String originalFileName) throws IOException {
        String processedFileName = originalFileName.replace(".jpg", "_resized.jpg")
                .replace(".jpeg", "_resized.jpeg")
                .replace(".png", "_resized.png");
        Path outputPath = outputDirectory.resolve(processedFileName);

        // Save the processed image to file system
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            fos.write(processedImageBytes);
        }
        // Convert the file path to a URL with forward slashes
        String processedImageUrl = outputDirectory.toString() + "/" + processedFileName;
        // Convert backslashes to forward slashes for URL compatibility
        return processedImageUrl.replace("\\", "/");
    }

    // Process image by calling Flask
    private byte[] processImage(MultipartFile file, int width, int height) throws IOException {
        String flaskUrl = "http://127.0.0.1:5000/resize"; // Flask API URL

        // Create RestTemplate to send HTTP request to Flask
        RestTemplate restTemplate = new RestTemplate();

        // Create header with Content-Type is multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //Create multiValueMap
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Using the file resource for multipart
//        body.add("data", file.getBytes());
        body.add("width", width);
        body.add("height", height);

        //Create HttpEntity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send request to Flask and get the processed image in byte[] form
        ResponseEntity<byte[]> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, requestEntity, byte[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to process image");
        }
    }
}
