
package hifive;

public class ApplyCanceled extends AbstractEvent {

    private Long conferenceId;
    private String conferenceStatus;
    private Long payId;

    public Long getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(Long conferenceId) {
        this.conferenceId = conferenceId;
    }
    public String getConferenceStatus() {
        return conferenceStatus;
    }

    public void setConferenceStatus(String conferenceStatus) {
        this.conferenceStatus = conferenceStatus;
    }
    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }
}

