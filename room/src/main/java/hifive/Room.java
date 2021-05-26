package hifive;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Room_table")
public class Room {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long roomNumber;
    private String roomStatus;
    private Integer usedCount;
    private Long conferenceId;
    private Long payId;

    @PostPersist
    public void onPostPersist(){
        Assigned assigned = new Assigned();
        BeanUtils.copyProperties(this, assigned);
        assigned.publishAfterCommit();


        CancelAssigned cancelAssigned = new CancelAssigned();
        BeanUtils.copyProperties(this, cancelAssigned);
        cancelAssigned.publishAfterCommit();


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
