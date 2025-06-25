package com.example.ermsuit.controller;

import com.example.ermsuit.dto.DataSourceUploadRequest;
import com.example.ermsuit.entity.DataSource;
import com.example.ermsuit.service.DataSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataSource> uploadDataSource(
            @RequestPart("request") @Valid DataSourceUploadRequest request,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(dataSourceService.uploadDataSource(request, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataSource> getDataSourceById(@PathVariable Long id) {
        return ResponseEntity.ok(dataSourceService.getDataSourceById(id));
    }

    @GetMapping
    public ResponseEntity<List<DataSource>> getAllDataSources() {
        return ResponseEntity.ok(dataSourceService.getAllDataSources());
    }

    @GetMapping("/my-data-sources")
    public ResponseEntity<List<DataSource>> getMyDataSources() {
        return ResponseEntity.ok(dataSourceService.getDataSourcesByCurrentUser());
    }
}
