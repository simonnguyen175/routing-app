package com.example.routing.repository;

import com.example.routing.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Chọn những node sao cho tọa độ lat giữa latMin và latMax,
 * và lon giữa lonMin và lonMax.
 */
public interface NodeRepository extends JpaRepository<Node, Long> {
    @Query(value =
            "SELECT * FROM node " +
                    "WHERE ST_Y(geom) BETWEEN :latMin AND :latMax " +
                    "  AND ST_X(geom) BETWEEN :lonMin AND :lonMax",
            nativeQuery = true)
    List<Node> findNodesInBBox(Double latMin, Double latMax,
                               Double lonMin, Double lonMax);

    @Query(value =
            "SELECT * FROM node " +
                    "ORDER BY ST_Distance_Sphere(geom, POINT(:lon, :lat)) " +
                    "LIMIT 1",
            nativeQuery = true)
    Node findNearestNode(@Param("lat") double lat, @Param("lon") double lon);
}
