
package hifive.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Date;
import java.util.Map;
@FeignClient(name="pay", url="http://localhost:8082")
public interface PayService {

    @RequestMapping(method= RequestMethod.GET, path="/pays/paid")
    public Map<String,String> paid(@RequestParam("status") String status, @RequestParam("conferenceId") Long conferenceId, @RequestParam("roomNumber") Long roomNumber);
    //public void pay(@RequestBody Pay pay);
    /*
        Req/Res 관계에서는 CRUD가 아닌 기능의 경우 타겟 클래스(external) Controller의 메소드를 호출한다
        그리고 external 폴더에 있는 타겟 클래스의 멤버변수를 필요한만큼 파라미터로 보내야한다. 물론 RequestBody로도 가능하겠다.
    */
}