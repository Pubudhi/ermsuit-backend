package com.example.ermsuit.service;

import com.example.ermsuit.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${ermsuit.reports.output-dir:./reports}")
    private String reportsDir;

    @Value("${ermsuit.reports.templates-dir:./templates}")
    private String templatesDir;

    private final Path reportsLocation;
    private final Path templatesLocation;
    private final Path dataSourcesLocation;

    public FileStorageService() {
        this.reportsLocation = Paths.get("./reports").toAbsolutePath().normalize();
        this.templatesLocation = Paths.get("./templates").toAbsolutePath().normalize();
        this.dataSourcesLocation = Paths.get("./data-sources").toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.reportsLocation);
            Files.createDirectories(this.templatesLocation);
            Files.createDirectories(this.dataSourcesLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directories where the uploaded files will be stored.", ex);
        }
    }

    public String storeDataSourceFile(MultipartFile file) {
        return storeFile(file, dataSourcesLocation);
    }

    public String storeTemplateFile(MultipartFile file) {
        return storeFile(file, templatesLocation);
    }

    public String storeReportFile(MultipartFile file) {
        return storeFile(file, reportsLocation);
    }

    private String storeFile(MultipartFile file, Path location) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Generate a unique file name to prevent overwriting
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = location.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Path getReportsLocation() {
        return reportsLocation;
    }

    public Path getTemplatesLocation() {
        return templatesLocation;
    }

    public Path getDataSourcesLocation() {
        return dataSourcesLocation;
    }
}
