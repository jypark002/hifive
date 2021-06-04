package hifive;
import java.util.Optional;
import hifive.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ConferenceRepository conferenceRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverAssigned_UpdateStatus(@Payload Assigned assigned){

        // 룸이 할당이 되었으면 
        // 신청->결제->룸번호 할당 or 상태변경 
        // 현재는 이 2가지만 이벤트만 가능함
        // 회의의 상태를 ASSIGNED로 변경하는 로직을 수행하겠음
        if(!assigned.validate()) {
            System.out.println("##### listener UpdateStatus Fail Assigned");
            return;
        }
        else { //assigned.validate()
            System.out.println("\n\n##### listener UpdateStatus : " + assigned.toJson() + "\n\n");
            Optional<Conference> confOptional = conferenceRepository.findById(assigned.getConferenceId());
            //assigned의 conferenceId로 찾고
            Conference conference = confOptional.get();
            System.out.println("--------------------------------------");
            System.out.println("Assigend된 conference 데이터");
            System.out.println(conference.getConferenceId());
            System.out.println(conference.getPayId());
            System.out.println(conference.getRoomNumber());
            System.out.println(conference.getStatus());
            System.out.println("--------------------------------------");
            conference.setPayId(assigned.getPayId())
            conference.setStatus(assigned.getRoomStatus());
            conferenceRepository.save(conference);
        } 
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelAssigned_UpdateStatus(@Payload CancelAssigned cancelAssigned){

        if(!cancelAssigned.validate()) {
            System.out.println("##### listener UpdateStatus Fail cancelAssigned");
            return;
        }
        else{
            // 동일한 로직으로 할당 취소가 되면, 상태가 Canceled로 변경됨
            System.out.println("\n\n##### listener UpdateStatus : " + cancelAssigned.toJson() + "\n\n");
            Optional<Conference> confOptional = conferenceRepository.findById(cancelAssigned.getConferenceId());
            //assigned의 conferenceId로 찾고
            Conference conference = confOptional.get();
            conference.setStatus("CANCELED");
            conferenceRepository.save(conference);
        }
            
    }

}
