package hifive;

public class Applied {
    private Long conferenceId;
    private String conferenceStatus;
    private Long roomNumber;

    public Applied(){
        super();
    }

    public Long getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(Long confernceId) {
        this.conferenceId = confernceId;
    }
    public String getConferenceStatus() {
        return conferenceStatus;
    }

    public void setConferenceStatus(String conferenceStatus) {
        this.conferenceStatus = conferenceStatus;
    }
    public Long getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Long roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isMe() {
        return false;
    }

}
