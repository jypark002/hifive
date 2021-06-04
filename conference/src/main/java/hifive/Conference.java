package hifive;
import java.util.Optional;
import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
@Entity
@Table(name="Conference_table")
public class Conference {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long conferenceId;
    private String status;
    private Long payId;
    private Long roomNumber;

    // @Autowired ConferenceRepository conferenceRepository;

    @PostPersist //해당 엔티티를 저장한 후
    public void onPostPersist(){
        //회의가 저장되면, pay에 request를 보낸다.
         // 회의 상태 : CREATED | PAID | ASSIGNED | CANCELED
        System.out.println("\n저장되고");
        System.out.println(this.getStatus()+" "+this.getConferenceId()+" " +this.getRoomNumber()+ " "+this.getPayId());
        setStatus("CREATED");
        System.out.println(this.getStatus());

        
        System.out.println("#####################################");
        System.out.println("회의를 신청 하면 created 되고, Applied 이벤트 내용이 카프카에 올라가고, (결제가 된 내용이 response로 온다.)-이건 테스트용 ");
        Applied applied = new Applied();
        //BeanUtils.copyProperties는 원본객체의 필드 값을 타겟 객체의 필드값으로 복사하는 유틸인데, 필드이름과 타입이 동일해야함.
        applied.setConferenceId(this.getConferenceId());
        applied.setConferenceStatus(this.getStatus());
        applied.setRoomNumber(this.getRoomNumber());
        applied.publishAfterCommit();
        //신청내역이 카프카에 올라감
        System.out.println("applied값 확인");
        System.out.println("[ conferenceId : "+applied.getConferenceId()+", status : " + applied.getConferenceStatus() + ", roomNumber : "+ applied.getRoomNumber()+" ]");
        Map<String,String> res = ConferenceApplication.applicationContext
                                                      .getBean(hifive.external.PayService.class)
                                                      .paid(this.getStatus(),this.getConferenceId(),this.getRoomNumber());
        System.out.println("res값 확인");
        System.out.println("[ msg : "+res.get("msg")+", status : " + res.get("status") + "\n payid : "+ res.get("payid")+" ]");
        System.out.println("#####################################");
        //결제 아이디가 있고, 결제 상태로 돌아온 경우 회의 상태로 결제로 바꾼다.
        if(this.getPayId() != null && res.get("status").equals("PAID")){
            this.setStatus("PAID");
        }
        // Optional<Conference> confOptional = conferenceRepository.findByConferenceId(this.getConferenceId());
        // Conference conference = confOptional.get();
        this.setPayId(Long.valueOf(res.get("payid")));
        // conferenceRepository.save(conference);

        System.out.println("저장된 후 confernece");
        System.out.println(this.getConferenceId());
        System.out.println(this.getPayId());
        System.out.println(this.getStatus());
        ConferenceApplication.applicationContext.getBean(hifive.ConferenceRepository.class).save(this);

        Optional<Conference> confOptional = ConferenceApplication.applicationContext.getBean(hifive.ConferenceRepository.class).findById(this.getConferenceId());
        Conference conference = confOptional.get();
        System.out.println("가져온 후 confernece");
        System.out.println(conference.getConferenceId());
        System.out.println(conference.getPayId());
        System.out.println(conference.getStatus());
        
        //컨슈머는 kafka-console-consumer.bat --bootstrap-server http://localhost:9092 --topic hifive --from-beginning 여기서 확인 가능
        //roomnumber 없어서 추가, Room Policy Handler 메소드 2개 겹침 제거. Pay Policy Handler도 불필요한 메소드 1개 추가된거 제거
        //req, res 온거 확인. 
        //kafka에 올라간것도 확인. 
        //consumer 확인
    }

    @PreRemove //해당 엔티티를 삭제하기 전 (회의를 삭제하면 취소신청 이벤트 생성)
    public void onPreRemove(){
        ApplyCanceled applyCanceled = new ApplyCanceled();
        applyCanceled.setConferenceId(this.getConferenceId());
        applyCanceled.setConferenceStatus("CANCELED");
        applyCanceled.setPayId(this.getPayId());
        applyCanceled.publishAfterCommit();
        //삭제하고 ApplyCanceled 이벤트 카프카에 전송
    }

    @PostUpdate
    public void onPostUpdate(){

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
