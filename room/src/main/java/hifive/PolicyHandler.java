package hifive;

import hifive.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

            //예약 신청한 방 번호 조회, 퇴실 개념이 없기 때문에 상태 검사 하지 않음
            Optional<Room> optionalRoom = roomRepository.findById(paid.getRoomNumber());
//            Room room;
//            if (optionalRoom.equals(Optional.empty())) {
//                room = new Room();
//                room.setUsedCount(1);
//            }
//            else {
//                room = optionalRoom.get();
//                room.setUsedCount(room.getUsedCount() + 1);
//            }
//            Room room = roomRepository.findById(paid.getRoomNumber());

//            Room room = roomRepository.findById(paid.getRoomNumber());
            Room room = optionalRoom.get();
            room.setRoomStatus("FULL");
            room.setUsedCount(room.getUsedCount() + 1);
            room.setConferenceId(paid.getConferenceId());
            room.setPayId(paid.getPayId());

            System.out.println("##### 방배정 확인");
            System.out.println("[ RoomStatus : "+ room.getRoomStatus()+", RoomNumber : " + room.getRoomNumber() + ", UsedCount : "+ room.getUsedCount()+ ", ConferenceId : "+ room.getConferenceId()+ ","+room.getPayId()+"]");
            roomRepository.save(room);
        }
            
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_CancelRoomAssign(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) {
            System.out.println("##### listener CancelRoomAssign Fail");
            return;
        }

        else{

            if (payCanceled.getPayId() == null) return;

            //취소 시 조회 - findByPayId
            Room room = roomRepository.findByPayId(payCanceled.getPayId());

            //취소 시 방에 등록된 conferenceId, PayId 0으로 초기화 
            room.setRoomStatus("EMPTY");
            room.setUsedCount(room.getUsedCount() - 1);
            room.setConferenceId((long)0);
            room.setPayId((long)0);
            roomRepository.save(room);
        }
 
    }


    // @StreamListener(KafkaProcessor.INPUT)
    // public void whatever(@Payload String eventString){}


}
