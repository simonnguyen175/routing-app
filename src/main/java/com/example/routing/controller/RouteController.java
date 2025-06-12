package com.example.routing.controller;
import com.example.routing.dto.Graph;
import com.example.routing.model.Node;
import java.util.List;

import com.example.routing.service.GraphService;
import com.example.routing.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
public class RouteController {
    @Autowired
    private RouteService routeService;
    @Autowired
    private GraphService graphService;

    @GetMapping
    public ResponseEntity<List<Node>> getRoute(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon,
            @RequestParam String routeType
    ) {
        Node startNode = graphService.findNearestNode(fromLat, fromLon);
        Node endNode = graphService.findNearestNode(toLat, toLon);
        System.out.println("startNode: " + startNode.getId() + " endNode: " + endNode.getId());
        List<Node> path = routeService.findShortestRoute(startNode, endNode, routeType);
        if (path.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(path);
    }
}
