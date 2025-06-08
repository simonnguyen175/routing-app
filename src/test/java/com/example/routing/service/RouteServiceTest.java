package com.example.routing.service;

import com.example.routing.dto.Graph;
import com.example.routing.model.Node;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RouteServiceTest {

    @Autowired
    private RouteService routeService;

    @Test
    void testFindShortestRoute() {
        Long startNode = 7886541920L;
        Long endNode = 7886541942L;

        List<Node> result = routeService.findShortestRoute(startNode, endNode, "driving");
        assertNotNull(result);
        System.out.println("Shortest route from " + startNode + " to " + endNode + ":");
        System.out.println(result.size() + " nodes found.");
        for (Node node : result) {
            System.out.print(node.getId() + " ");
        }
    }

    @Test
    void testFindNearestNode() {
        // Given
        double lat = 18.676328;
        double lon = 105.688123;

        // When
        Node nearestNode = routeService.findNearestNode(lat, lon);

        // Optional: Log the found node for debugging
        System.out.println("Found nearest node: " + nearestNode.getId() +
                " at [" + nearestNode.getLat() + ", " + nearestNode.getLon() + "]");
    }
}
