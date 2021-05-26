package hifive;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Conference_table")
public class Conference {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long conferenceId;
    private String status;
    private Long payId;
    private Long roomNumber;

    @PostPersist
    public void onPostPersist(){
        Applied applied = new Applied();
        BeanUtils.copyProperties(this, applied);
        applied.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        hifive.external.Pay pay = new hifive.external.Pay();
        // mappings goes here
        Application.applicationContext.getBean(hifive.external.PayService.class)
            .pay(pay);


        ApplyCanceled applyCanceled = new ApplyCanceled();
        BeanUtils.copyProperties(this, applyCanceled);
        applyCanceled.publishAfterCommit();


    }


    public Long getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(Long conferenceId) {
        this.conferenceId = conferenceId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }
    public Long getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Long roomNumber) {
        this.roomNumber = roomNumber;
    }




}
