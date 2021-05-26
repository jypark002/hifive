package hifive;

public class Applied extends AbstractEvent {

    private Long confernceId;
    private String conferenceStatus;

    public Applied(){
        super();
    }

    public Long getConfernceId() {
        return confernceId;
    }

    public void setConfernceId(Long confernceId) {
        this.confernceId = confernceId;
    }
    public String getConferenceStatus() {
        return conferenceStatus;
    }

    public void setConferenceStatus(String conferenceStatus) {
        this.conferenceStatus = conferenceStatus;
    }
}
