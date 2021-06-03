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
    @Autowired RoomRepository roomRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_RoomAssign(@Payload Paid paid){

        if(!paid.validate()) {
            System.out.println("##### listener RoomAssign Fail");
            return;
        }

        else{
            System.out.println("\n\n##### listener RoomAssign : " + paid.toJson() + "\n\n");
            Room room = roomRepository.findByRoomNumber(paid.getRoomNumber());

            room.setRoomStatus("FULL");
            room.setUsedCount(room.getUsedCount() + 1);
            room.setConferenceId(paid.getConferenceId());
            room.setPayId(paid.getPayId());

            System.out.println("방배정 확인");
            System.out.println("[ RoomStatus : "+ room.getRoomStatus()+", RoomNumber : " + room.getRoomNumber() + ", UsedCount : "+ room.getUsedCount()+ ", ConferenceId : "+ room.getConferenceId()+"]");
            roomRepository.save(room);
        }
        
        
            
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_CancelRoomAssign(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;

        // Sample Logic //
        Room room = roomRepository.findByPayId(payCanceled.getPayId());
        
        //변경
        room.setRoomStatus("EMPTY");
        room.setUsedCount(room.getUsedCount() - 1);
        room.setConferenceId((long)0);
        room.setPayId((long)0);
        roomRepository.save(room);

    }




}
