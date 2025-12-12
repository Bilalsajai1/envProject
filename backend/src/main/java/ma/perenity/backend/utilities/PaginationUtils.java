package ma.perenity.backend.utilities;

import ma.perenity.backend.dto.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static Pageable buildPageable(PaginationRequest req) {
        String sortField = req.getSortField() != null && !req.getSortField().isBlank()
                ? req.getSortField()
                : "id";

        Sort.Direction direction = "desc".equalsIgnoreCase(req.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(req.getPage(), req.getSize(), Sort.by(direction, sortField));
    }

    public static Map<String, Object> extractFilters(PaginationRequest req) {
        return req.getFilters() == null ? new HashMap<>() : new HashMap<>(req.getFilters());
    }

    public static String extractSearch(Map<String, Object> filters) {
        if (filters == null) {
            return null;
        }
        Object searchObj = filters.remove("search");
        if (searchObj == null) {
            return null;
        }
        String value = searchObj.toString().trim();
        return value.isEmpty() ? null : value;
    }
}
