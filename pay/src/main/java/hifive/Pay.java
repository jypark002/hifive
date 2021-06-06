package hifive;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long payId;
    private String status;
    private Long conferenceId;
    private Long roomNumber;

    @PostPersist
    public void onPostPersist(){

        if (this.getStatus() != "PAID") return;

        System.out.println("********************* Pay PostPersist Start. PayStatus=" + this.getStatus());

        Paid paid = new Paid();
        paid.setPayId(this.payId);
        paid.setPayStatus(this.status);
        paid.setConferenceId(this.conferenceId);
        paid.setRoomNumber(this.roomNumber);
        //BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();

        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("********************* Pay PostPersist End.");
    }

    @PreRemove
    public void onPreRemove() {

        if (this.getPayId() == null || this.getStatus() != "CANCELED") return;

        System.out.println("********************* Pay PreRemove Start. PayStatus=" + this.getStatus());

        PayCanceled payCanceled = new PayCanceled();
        payCanceled.setPayId(this.getPayId());
        payCanceled.setPayStatus(this.getStatus());
        payCanceled.setConferenceId(this.getConferenceId());
//        BeanUtils.copyProperties(this, payCanceled);
        payCanceled.publishAfterCommit();

        System.out.println("********************* Pay PayCanceled End.");
    }

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
