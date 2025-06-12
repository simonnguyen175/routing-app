package com.example.routing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "node")
public class Node {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "geom", columnDefinition = "geometry(Point)")
    private Point geom;

    public Node() {
    }

    public Node(Long id, Point geom) {
        this.id = id;
        this.geom = geom;
    }

    public Node(Long id, Double lat, Double lon) {
        this.id = id;
        this.geom = new org.locationtech.jts.geom.GeometryFactory().createPoint(new org.locationtech.jts.geom.Coordinate(lon, lat));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }

    @JsonProperty("lat")
    public Double getLat() {
        return (geom != null) ? geom.getY() : null;
    }

    @JsonProperty("lon")
    public Double getLon() {
        return (geom != null) ? geom.getX() : null;
    }

}
