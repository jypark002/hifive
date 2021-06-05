package hifive;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="RoomState_table")
public class RoomState {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long roomNumber;
        private String roomStatus;
        private Integer roomUsedCount;
        private Long conferenceId;
        private String conferenceStatus;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public Long getRoomNumber() {
            return roomNumber;
        }

        public void setRoomNumber(Long roomNumber) {
            this.roomNumber = roomNumber;
        }
        public String getRoomStatus() {
            return roomStatus;
        }

        public void setRoomStatus(String roomStatus) {
            this.roomStatus = roomStatus;
        }
        public Integer getRoomUsedCount() {
            return roomUsedCount;
        }

        public void setRoomUsedCount(Integer roomUsedCount) {
            this.roomUsedCount = roomUsedCount;
        }
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

}
