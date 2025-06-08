package com.example.routing.service;

import com.example.routing.dto.Graph;
import com.example.routing.model.Edge;
import com.example.routing.model.Node;
import com.example.routing.repository.EdgeRepository;
import com.example.routing.repository.NodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GraphService {
    /* tập road type cấu hình một chỗ */
    public static final List<String> DRIVING_TYPES = List.of(
            "motorway", "primary", "secondary", "tertiary",
            "trunk", "unclassified", "residential",
            "living_street", "service");

    public static final List<String> WALKING_TYPES = List.of(
            "footway", "pedestrian", "path", "steps",
            "living_street", "residential", "service", "unclassified");

    private final NodeRepository nodeRepo;
    private final EdgeRepository edgeRepo;

    private static Graph fullGraph;

    @PostConstruct
    public void loadFullGraph() {
        List<Node> nodes = nodeRepo.findAll();
        List<Edge> edges = edgeRepo.findAll();
        fullGraph = new Graph(nodes, edges);
        System.out.println("Loaded full graph: " + nodes.size() + " nodes, " + edges.size() + " edges");
    }

    public Graph getFullGraph() {
        return fullGraph;
    }

    public GraphService(NodeRepository nodeRepo, EdgeRepository edgeRepo) {
        this.nodeRepo = nodeRepo;
        this.edgeRepo = edgeRepo;
    }

    public Graph getGraph(double latMin, double latMax,
                          double lonMin, double lonMax,
                          String mode) {

        Collection<String> types = mode.equalsIgnoreCase("walking")
                ? WALKING_TYPES
                : DRIVING_TYPES;

        /* 1. Lấy edge với danh sách road_type đã chọn */
        List<Edge> edges = edgeRepo.findEdgesInBBox(
                latMin, latMax, lonMin, lonMax, types);

        /* 2. Lọc node theo edge */
        Set<Long> nodeIds = edges.stream()
                .flatMap(e -> Stream.of(e.getFromId(), e.getToId()))
                .collect(Collectors.toSet());

        List<Node> nodes = nodeRepo.findAllById(nodeIds);

        return new Graph(nodes, edges);
    }
}
