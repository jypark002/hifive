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

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelAssigned_SendAlarm(@Payload CancelAssigned cancelAssigned){

        if(!cancelAssigned.validate()) return;

        System.out.println("\n\n##### listener SendAlarm : " + cancelAssigned.toJson() + "\n\n");

        // Sample Logic //
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverAssigned_SendAlarm(@Payload Assigned assigned){

        if(!assigned.validate()) return;

        System.out.println("\n\n##### listener SendAlarm : " + assigned.toJson() + "\n\n");

        // Sample Logic //
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
