package hifive.external;

public class Pay {

    private Long payId;
    private String status;
    private Long conferenceId;
    private Long roomNumber;

    public Long getPayId() {
        return payId;
    }
    public void setPayId(Long payId) {
        this.payId = payId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getConferenceId() {
        return conferenceId;
    }
    public void setConferenceId(Long conferenceId) {
        this.conferenceId = conferenceId;
    }
    public Long getRoomNumber() {
        return roomNumber;
    }
    public void setRoomNumber(Long roomNumber) {
        this.roomNumber = roomNumber;
    }
}
