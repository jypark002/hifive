package hifive;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomStateRepository extends CrudRepository<RoomState, Long> {

    List<RoomState> findByConferenceId(Long conferenceId);

}