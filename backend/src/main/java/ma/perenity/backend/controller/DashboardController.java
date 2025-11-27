package ma.perenity.backend.controller;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.DashboardSummaryDTO;
import ma.perenity.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
