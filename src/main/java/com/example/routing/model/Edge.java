package com.example.routing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.locationtech.jts.geom.LineString;

@Entity
@Table(name = "edge")
public class Edge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "from_id")
    private Long fromId;

    @Column(name = "to_id")
    private Long toId;

    @Column(name = "geom", columnDefinition = "geometry(LineString)")
    private LineString geom;

    @Column(name = "road_type")
    private String roadType;          // NEW FIELD

    public Edge() {
    }

    public Edge(Long fromId, Long toId, LineString geom) {
        this.fromId = fromId;
        this.toId = toId;
        this.geom = geom;
    }

    public Long getId() {
        return id;
    }

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getToId() {
        return toId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    @JsonIgnore
    public LineString getGeom() {
        return geom;
    }

    public void setGeom(LineString geom) {
        this.geom = geom;
    }

    @JsonProperty("lat1")
    public Double getLat1() {
        if (geom != null && geom.getNumPoints() >= 2) {
            return geom.getCoordinateN(0).y;
        }
        return null;
    }

    @JsonProperty("lon1")
    public Double getLon1() {
        if (geom != null && geom.getNumPoints() >= 2) {
            return geom.getCoordinateN(0).x;
        }
        return null;
    }

    @JsonProperty("lat2")
    public Double getLat2() {
        if (geom != null && geom.getNumPoints() >= 2) {
            return geom.getCoordinateN(1).y;
        }
        return null;
    }

    @JsonProperty("lon2")
    public Double getLon2() {
        if (geom != null && geom.getNumPoints() >= 2) {
            return geom.getCoordinateN(1).x;
        }
        return null;
    }

    public String getRoadType() { return roadType; }
    public void   setRoadType(String rt){ this.roadType = rt; }

    public Double getCost() {
        // Giả sử chi phí là độ dài của đoạn đường
        return (geom != null) ? geom.getLength() : 0.0;
    }
}
