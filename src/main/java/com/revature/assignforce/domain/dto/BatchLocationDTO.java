package com.revature.assignforce.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by August Duet on 4/10/2017.
 */
@ApiModel("BatchLocationDTO")
public class BatchLocationDTO {

    @ApiModelProperty(notes = "The Location ID")
    private Integer locationId;
    @ApiModelProperty(notes = "The name of the location")
    private String locationName;
    @ApiModelProperty(notes = "The building ID")
    private Integer buildingId;
    @ApiModelProperty(notes = "The name of the Building")
    private String buildingName;
    @ApiModelProperty(notes = "The room ID")
    private Integer roomId;
    @ApiModelProperty(notes = "The Name of the room")
    private String roomName;

    public BatchLocationDTO(){}

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return "BatchLocationDTO{" +
                "locationId=" + locationId +
                ", locationName='" + locationName + '\'' +
                ", buildingId=" + buildingId +
                ", buildingName='" + buildingName + '\'' +
                ", roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                '}';
    }
}
