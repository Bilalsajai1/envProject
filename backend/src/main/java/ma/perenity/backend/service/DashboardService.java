package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.DashboardSummaryDTO;
import ma.perenity.backend.repository.ApplicationRepository;
import ma.perenity.backend.repository.EnvApplicationRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.repository.ProjetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjetRepository projetRepository;
    private final EnvironnementRepository environnementRepository;
    private final EnvApplicationRepository envApplicationRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {

        long totalProjects        = projetRepository.count();
        long totalEnvironments    = environnementRepository.count();
        long totalEnvApplications = envApplicationRepository.count();
        long totalApplications    = applicationRepository.count();

        long activeEnvironments    = environnementRepository.countByActifTrue();
        long activeEnvApplications = envApplicationRepository.countByActifTrue();

        return DashboardSummaryDTO.builder()
                .totalProjects(totalProjects)
                .totalEnvironments(totalEnvironments)
                .totalEnvApplications(totalEnvApplications)
                .totalApplications(totalApplications)
                .activeEnvironments(activeEnvironments)
                .activeEnvApplications(activeEnvApplications)
                .build();
    }
}
