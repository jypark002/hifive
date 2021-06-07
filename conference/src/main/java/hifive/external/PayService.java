package hifive.external;

import hifive.Applied;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Date;
import java.util.Map;
@FeignClient(name="pay", url="http://localhost:8082")
public interface PayService {

    @RequestMapping(method= RequestMethod.POST, path="/pays/room_applied")
    public Map<String,String> paid(Applied applied);
}
