package hifive;

import java.util.*;
import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Room_table")
public class Room {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long roomNumber;
    private String roomStatus;
    private Integer usedCount;
    private Long conferenceId;
    private Long payId;

    @PostUpdate
    public void onPostUpdate(){
      
        System.out.println("\n\n##### RoomAssign PostPersist: " + this.getRoomStatus());

        //예약 회의실 상태(roomStatus) == FULL
        if(this.getRoomStatus().equals("FULL")){
                
            Assigned assignedRoom = new Assigned();
            assignedRoom.setRoomNumber(this.getRoomNumber());
            assignedRoom.setRoomStatus("ASSIGNED");
            assignedRoom.setConferenceId(this.getConferenceId());
            assignedRoom.publishAfterCommit();
        }
    
        //취소 회의실 상태(roomStatus) == EMPTY
        else if(this.getRoomStatus().equals("EMPTY")){
        
            CancelAssigned cancelAssigned = new CancelAssigned();
            cancelAssigned.setId(this.getId());
            cancelAssigned.setRoomNumber(this.getRoomNumber());
            cancelAssigned.setRoomStatus("CANCELED");
            cancelAssigned.setConferenceId(this.getConferenceId());
            cancelAssigned.publishAfterCommit();
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Long roomNumber) {
        this.roomNumber = roomNumber;
    }
    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }
    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }
    public Long getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(Long conferenceId) {
        this.conferenceId = conferenceId;
    }
    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }




}
