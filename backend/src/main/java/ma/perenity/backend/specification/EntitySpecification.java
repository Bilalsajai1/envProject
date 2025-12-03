package ma.perenity.backend.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class EntitySpecification<T> {

    public Specification<T> getSpecification(Map<String, Object> filters) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            Predicate predicate = cb.conjunction();

            if (filters == null || filters.isEmpty()) {
                return predicate;
            }

            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    continue;
                }
                if (value instanceof String str && str.isBlank()) {
                    continue;
                }
                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    if (parts.length != 2) {
                        continue;
                    }

                    Join<Object, Object> join = root.join(parts[0], JoinType.LEFT);
                    Path<String> nestedPath = join.get(parts[1]);

                    predicate = cb.and(
                            predicate,
                            cb.like(
                                    cb.lower(nestedPath),
                                    "%" + value.toString().toLowerCase() + "%"
                            )
                    );
                    continue;
                }

                Path<?> path;
                try {
                    path = root.get(key);
                } catch (IllegalArgumentException ex) {
                    continue;
                }

                Class<?> javaType = path.getJavaType();

                if (String.class.equals(javaType)) {

                    predicate = cb.and(
                            predicate,
                            cb.like(
                                    cb.lower(path.as(String.class)),
                                    "%" + value.toString().toLowerCase() + "%"
                            )
                    );

                } else if (Boolean.class.equals(javaType) || boolean.class.equals(javaType)) {

                    predicate = cb.and(
                            predicate,
                            cb.equal(path.as(Boolean.class), Boolean.valueOf(value.toString()))
                    );

                } else if (Number.class.isAssignableFrom(javaType)) {


                    predicate = cb.and(predicate, cb.equal(path, value));

                } else {

                    predicate = cb.and(predicate, cb.equal(path, value));
                }
            }

            return predicate;
        };
    }
}
