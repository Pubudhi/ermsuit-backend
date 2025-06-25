package com.example.ermsuit.service;

import com.example.ermsuit.dto.DataSourceUploadRequest;
import com.example.ermsuit.entity.DataSource;
import com.example.ermsuit.entity.User;
import com.example.ermsuit.exception.ResourceNotFoundException;
import com.example.ermsuit.repository.DataSourceRepository;
import com.example.ermsuit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceService {

    private final DataSourceRepository dataSourceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    public DataSource uploadDataSource(DataSourceUploadRequest request, MultipartFile file) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Store the file
        String filePath = fileStorageService.storeDataSourceFile(file);
        
        // Count rows if it's a CSV file
        Integer rowCount = null;
        if (request.getFormat() == DataSource.DataFormat.CSV) {
            rowCount = countCsvRows(file);
        }

        // Create data source entity
        DataSource dataSource = DataSource.builder()
                .name(request.getName())
                .description(request.getDescription())
                .filePath(filePath)
                .format(request.getFormat())
                .uploadedBy(currentUser)
                .rowCount(rowCount)
                .build();

        // Save to database
        DataSource savedDataSource = dataSourceRepository.save(dataSource);
        
        // Log the event
        auditService.logEvent("DATA_SOURCE_UPLOAD", 
                "Data source uploaded: " + request.getName(), 
                "DataSource", 
                savedDataSource.getId(), 
                currentUser.getUsername());
        
        return savedDataSource;
    }

    public DataSource getDataSourceById(Long id) {
        return dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found with id: " + id));
    }

    public List<DataSource> getAllDataSources() {
        return dataSourceRepository.findAll();
    }

    public List<DataSource> getDataSourcesByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return dataSourceRepository.findByUploadedBy(currentUser);
    }

    private Integer countCsvRows(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            return (int) csvParser.getRecords().size();
        } catch (IOException e) {
            // Just return null if we can't count
            return null;
        }
    }
}
