package ma.perenity.backend.specification;


import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.util.Map;

public class EntitySpecification<T> {

    public Specification<T> getSpecification(Map<String, Object> filters) {

        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            Predicate predicate = cb.conjunction();

            if (filters == null) return predicate;

            for (String key : filters.keySet()) {
                Object value = filters.get(key);
                if (value == null) continue;

                // gestion des champs imbriqués : profil.libelle
                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    Join<Object, Object> join = root.join(parts[0], JoinType.LEFT);
                    predicate = cb.and(predicate,
                            cb.like(cb.lower(join.get(parts[1]).as(String.class)),
                                    "%" + value.toString().toLowerCase() + "%"));
                    continue;
                }

                Path<?> path = root.get(key);

                if (path.getJavaType() == String.class) {
                    predicate = cb.and(predicate,
                            cb.like(cb.lower(path.as(String.class)),
                                    "%" + value.toString().toLowerCase() + "%"));

                } else if (path.getJavaType() == Boolean.class) {
                    predicate = cb.and(predicate,
                            cb.equal(path.as(Boolean.class), Boolean.valueOf(value.toString())));

                } else if (Number.class.isAssignableFrom(path.getJavaType())) {
                    predicate = cb.and(predicate,
                            cb.equal(path.as(Number.class), value));

                } else {
                    // fallback
                    predicate = cb.and(predicate,
                            cb.equal(path, value));
                }
            }

            return predicate;
        };
    }
}