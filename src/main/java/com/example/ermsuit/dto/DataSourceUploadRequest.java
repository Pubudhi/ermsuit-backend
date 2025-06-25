package com.example.ermsuit.dto;

import com.example.ermsuit.entity.DataSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceUploadRequest {
    
    @NotBlank(message = "Data source name is required")
    private String name;
    
    @NotBlank(message = "Data source description is required")
    private String description;
    
    @NotNull(message = "Data format is required")
    private DataSource.DataFormat format;
    
    // The actual file will be handled separately in the controller
}
