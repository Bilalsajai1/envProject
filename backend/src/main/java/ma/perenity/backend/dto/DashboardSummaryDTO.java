package ma.perenity.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    private long totalProjects;
    private long totalEnvironments;
    private long totalEnvApplications;
    private long totalApplications;

    private long activeEnvironments;
    private long activeEnvApplications;
}
