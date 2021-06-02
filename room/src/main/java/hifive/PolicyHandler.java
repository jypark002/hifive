package hifive;

import hifive.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired RoomRepository roomRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_RoomAssign(@Payload Paid paid){

        if(!paid.validate()) return;

        // Sample Logic //
            
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_CancelRoomAssign(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;

        // Sample Logic //
        Room room = RoomRepository.findByPayId(payCancelled.getPayId());
        
        //변경
        
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
