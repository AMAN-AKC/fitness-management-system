package com.fitness.controller;

import com.fitness.dto.ManagerDashboardDto;
import com.fitness.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fitness.entity.Invoice;
import com.fitness.repository.InvoiceRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    private final InvoiceRepository invoiceRepository;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ManagerDashboardDto> getDashboardStats(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // Delegate to service (could parse dates if needed, for now just pass branchId override)
        return ResponseEntity.ok(managerService.getDashboardStats(branchId));
    }

    @GetMapping(value = "/dashboard/export-csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<byte[]> exportAnalyticsCsv() {
        String csv = managerService.exportAnalyticsCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=analytics_report.csv")
                .body(csv.getBytes());
    }

    @GetMapping("/dashboard/transactions")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<Invoice>> getTransactionsDrillDown(Pageable pageable) {
        return ResponseEntity.ok(invoiceRepository.findAll(pageable));
    }
}
