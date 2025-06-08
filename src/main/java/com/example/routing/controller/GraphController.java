package com.example.routing.controller;

import com.example.routing.dto.Graph;        // <-- DTO độc lập
import com.example.routing.service.GraphService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Trả về tập đỉnh + cạnh trong bounding-box,
     * kèm lọc theo road_type (driving / walking).
     *
     * @param latMin  vĩ độ nhỏ nhất
     * @param latMax  vĩ độ lớn nhất
     * @param lonMin  kinh độ nhỏ nhất
     * @param lonMax  kinh độ lớn nhất
     * @param mode    driving | walking  (mặc định driving)
     */
    @GetMapping
    public Graph getGraph(
            @RequestParam Double latMin,
            @RequestParam Double latMax,
            @RequestParam Double lonMin,
            @RequestParam Double lonMax,
            @RequestParam(defaultValue = "driving") String mode) {

        /* gọi service đã gộp logic lọc node-edge */
        return graphService.getGraph(latMin, latMax, lonMin, lonMax, mode);
    }
}
