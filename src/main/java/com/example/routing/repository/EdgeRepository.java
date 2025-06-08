package com.example.routing.repository;

import com.example.routing.model.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EdgeRepository extends JpaRepository<Edge, Long> {
    @Query(value =
            "SELECT e.* FROM edge e " +
                    "JOIN node n1 ON e.from_id = n1.id " +
                    "JOIN node n2 ON e.to_id   = n2.id " +
                    "WHERE e.road_type IN (:types) " +
                    "  AND ( (ST_Y(n1.geom) BETWEEN :latMin AND :latMax " +
                    "         AND ST_X(n1.geom) BETWEEN :lonMin AND :lonMax) " +
                    "     OR (ST_Y(n2.geom) BETWEEN :latMin AND :latMax " +
                    "         AND ST_X(n2.geom) BETWEEN :lonMin AND :lonMax) )",
            nativeQuery = true)
    List<Edge> findEdgesInBBox(@Param("latMin") Double latMin,
                               @Param("latMax") Double latMax,
                               @Param("lonMin") Double lonMin,
                               @Param("lonMax") Double lonMax,
                               @Param("types") Collection<String> types);
}
