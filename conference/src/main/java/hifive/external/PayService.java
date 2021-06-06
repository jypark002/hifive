
package hifive.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Map;
import hifive.Applied;
@FeignClient(name="pay", url="http://localhost:8082")
public interface PayService {

    @RequestMapping(method= RequestMethod.POST, path="/pays/room_applied")
    public Map<String,String> paid(Applied applied);
}