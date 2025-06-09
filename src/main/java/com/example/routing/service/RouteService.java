package com.example.routing.service;

import com.example.routing.dto.Graph;
import com.example.routing.model.Node;
import com.example.routing.model.Edge;
import com.example.routing.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteService {
    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private GraphService graphService;

    private static final double DELTA = 0.01;

    @Cacheable(value = "routeCache", key = "#startId + '-' + #endId + '-' + #routeType")
    public List<Node> findShortestRoute(Long startId, Long endId, String routeType) {
        System.out.println("THỰC SỰ chạy hàm này: " + startId + " - " + endId);
        Node start = nodeRepository.findById(startId).orElseThrow();
        Node end = nodeRepository.findById(endId).orElseThrow();

        // Use the full graph loaded at startup
        Graph graph = graphService.getFullGraph();

        Map<Long, Node> nodeMap = graph.getNodes().stream()
                .collect(Collectors.toMap(Node::getId, n -> n));
        List<Edge> edgeList = graph.getEdges();

        Map<Long, List<Edge>> adjList = new HashMap<>();
        for (Edge edge : edgeList) {
            adjList.computeIfAbsent(edge.getFromId(), k -> new ArrayList<>()).add(edge);
        }

        return aStar(startId, endId, nodeMap, adjList);
    }

    /**
     * Thuật toán A* - now returns List<Node> instead of List<Long>
     */
    private List<Node> aStar(Long startId, Long endId,
                             Map<Long, Node> nodeMap,
                             Map<Long, List<Edge>> adjList) {

        PriorityQueue<NodeRecord> heap = new PriorityQueue<>(Comparator.comparingDouble(nr -> nr.fScore));
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> cameFrom = new HashMap<>();

        gScore.put(startId, 0.0);
        heap.add(new NodeRecord(startId, heuristic(nodeMap.get(startId), nodeMap.get(endId))));

        while (!heap.isEmpty()) {
            NodeRecord current = heap.poll();
            if (current.nodeId.equals(endId)) {
                // Đã tới đích -> reconstruct path with Node objects
                return reconstructPath(cameFrom, current.nodeId, startId, nodeMap);
            }

            List<Edge> neighbors = adjList.getOrDefault(current.nodeId, List.of());
            for (Edge edge : neighbors) {
                double tentativeG = gScore.get(current.nodeId) + edge.getCost();
                if (tentativeG < gScore.getOrDefault(edge.getToId(), Double.MAX_VALUE)) {
                    cameFrom.put(edge.getToId(), current.nodeId);
                    gScore.put(edge.getToId(), tentativeG);
                    double fScore = tentativeG + heuristic(nodeMap.get(edge.getToId()), nodeMap.get(endId));
                    heap.add(new NodeRecord(edge.getToId(), fScore));
                }
            }
        }
        // Không tìm thấy đường đi
        return List.of();
    }

    private double heuristic(Node a, Node b) {
        double dx = a.getLat() - b.getLat();
        double dy = a.getLon() - b.getLon();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Modified to return Node objects instead of just IDs
    private List<Node> reconstructPath(Map<Long, Long> cameFrom, Long current, Long startId, Map<Long, Node> nodeMap) {
        List<Node> path = new ArrayList<>();
        path.add(nodeMap.get(current));

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(nodeMap.get(current));
        }

        Collections.reverse(path);

        // Validate path starts with startId
        if (path.get(0).getId().equals(startId)) {
            return path;
        } else {
            return List.of();
        }
    }

    private static class NodeRecord {
        Long nodeId;
        double fScore;
        NodeRecord(Long nodeId, double fScore) {
            this.nodeId = nodeId;
            this.fScore = fScore;
        }
    }

    public Node findNearestNode(double lat, double lon) {
        // Tìm node gần nhất với tọa độ (lat, lon)
        Node node = nodeRepository.findNearestNode(lat, lon);
        if (node == null) {
            throw new NoSuchElementException("No nearest node found");
        }
        return node;
    }
}