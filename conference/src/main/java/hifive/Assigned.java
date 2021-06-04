
package hifive;

public class Assigned extends AbstractEvent {

    private Long roomNumber;
    private String roomStatus;
    private Long conferenceId;
    private Long payId;

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

