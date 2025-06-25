package com.example.ermsuit.service;

import com.aspose.cells.Chart;
import com.aspose.cells.ChartType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.pdf.Document;
import com.aspose.pdf.Page;
import com.aspose.pdf.TextFragment;
import com.example.ermsuit.dto.ReportRequest;
import com.example.ermsuit.entity.DataSource;
import com.example.ermsuit.entity.Report;
import com.example.ermsuit.entity.ReportTemplate;
import com.example.ermsuit.entity.User;
import com.example.ermsuit.exception.ResourceNotFoundException;
import com.example.ermsuit.repository.ReportRepository;
import com.example.ermsuit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DataSourceService dataSourceService;
    private final ReportTemplateService reportTemplateService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    public Report generateReport(ReportRequest request) throws Exception {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get data source
        DataSource dataSource = dataSourceService.getDataSourceById(request.getDataSourceId());
        
        // Get template
        ReportTemplate template = reportTemplateService.getTemplateById(request.getTemplateId());
        
        // Generate PDF report
        String reportFilePath = generatePdfReport(request.getName(), dataSource, template);
        
        // Create report entity
        Report report = Report.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .filePath(reportFilePath)
                .user(currentUser)
                .dataSource(dataSource.getName())
                .templateName(template.getName())
                .build();
        
        // Save to database
        Report savedReport = reportRepository.save(report);
        
        // Log the event
        auditService.logEvent("REPORT_GENERATED", 
                "Report generated: " + request.getName(), 
                "Report", 
                savedReport.getId(), 
                currentUser.getUsername());
        
        return savedReport;
    }

    private String generatePdfReport(String reportName, DataSource dataSource, ReportTemplate template) throws Exception {
        // Create a new PDF document
        Document pdfDocument = new Document();
        
        // Add a page to the document
        Page page = pdfDocument.getPages().add();
        
        // Add title
        TextFragment title = new TextFragment(reportName);
        title.getTextState().setFontSize(20);
        title.getTextState().setFontStyle(com.aspose.pdf.FontStyles.Bold);
        page.getParagraphs().add(title);
        
        // Add timestamp
        TextFragment timestamp = new TextFragment("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timestamp.getTextState().setFontSize(10);
        page.getParagraphs().add(timestamp);
        
        // Add data source info
        TextFragment dataSourceInfo = new TextFragment("Data Source: " + dataSource.getName());
        dataSourceInfo.getTextState().setFontSize(12);
        page.getParagraphs().add(dataSourceInfo);
        
        // Generate charts based on data source
        if (dataSource.getFormat() == DataSource.DataFormat.CSV) {
            // Parse CSV and generate charts
            List<Map<String, Object>> chartData = parseCSVData(dataSource.getFilePath());
            
            // Generate charts using Aspose.Cells
            byte[] chartImage = generateChartImage(chartData);
            
            // Add chart image to PDF
            com.aspose.pdf.Image image = new com.aspose.pdf.Image();
            image.setImageStream(new java.io.ByteArrayInputStream(chartImage));
            page.getParagraphs().add(image);
        }
        
        // Generate unique file name for the report
        String timestamp2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportFileName = "report_" + timestamp2 + ".pdf";
        Path reportPath = Paths.get(fileStorageService.getReportsLocation().toString(), reportFileName);
        
        // Save the PDF
        pdfDocument.save(reportPath.toString());
        
        return reportPath.toString();
    }

    private List<Map<String, Object>> parseCSVData(String csvFilePath) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (FileReader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                Map<String, Object> row = new HashMap<>();
                csvParser.getHeaderMap().forEach((header, index) -> 
                    row.put(header, record.get(index))
                );
                data.add(row);
            }
        }
        
        return data;
    }

    private byte[] generateChartImage(List<Map<String, Object>> data) throws Exception {
        // Create a new workbook
        Workbook workbook = new Workbook();
        Worksheet worksheet = workbook.getWorksheets().get(0);
        
        // Assuming the first column is labels and second column is values
        if (!data.isEmpty()) {
            // Get headers
            Map<String, Object> firstRow = data.get(0);
            List<String> headers = new ArrayList<>(firstRow.keySet());
            
            // Write headers
            for (int i = 0; i < headers.size(); i++) {
                worksheet.getCells().get(0, i).putValue(headers.get(i));
            }
            
            // Write data
            for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
                Map<String, Object> row = data.get(rowIndex);
                for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                    String header = headers.get(colIndex);
                    worksheet.getCells().get(rowIndex + 1, colIndex).putValue(row.get(header));
                }
            }
            
            // Add a chart
            int chartIndex = worksheet.getCharts().add(ChartType.COLUMN, 5, 0, 15, 10);
            Chart chart = worksheet.getCharts().get(chartIndex);
            
            // Set chart data range using the non-deprecated API for Aspose.Cells
            chart.getNSeries().add("A2:B5", true);
            chart.getNSeries().add("=" + worksheet.getName() + "!B2:B" + (data.size() + 1), true);
            chart.getNSeries().setCategoryData("=" + worksheet.getName() + "!A2:A" + (data.size() + 1));
            
            // Set chart title
            chart.getTitle().setText("Data Visualization");
        }
        
        // Save to memory stream
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.save(outputStream, com.aspose.cells.SaveFormat.PNG);
        
        return outputStream.toByteArray();
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getReportsByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return reportRepository.findByUser(currentUser);
    }
}
