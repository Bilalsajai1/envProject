package ma.perenity.backend.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PaginationRequest {

    private int page = 0;
    private int size = 10;

    private String sortField = "id";
    private String sortDirection = "asc";


    private Map<String, Object> filters;
}
