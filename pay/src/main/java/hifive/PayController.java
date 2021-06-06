package hifive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
public class PayController {

    @Autowired PayRepository payRepository;

    @PostMapping("/pays/room_applied")
    public Map<String,String> room_reg(@RequestBody Applied applied)
    {
        Long con_id = applied.getConferenceId();
        applied.getRoomNumber();
        Long room_no = applied.getRoomNumber();
        String status = "Not Paid";
        Pay pay = new Pay();
        pay.setConferenceId(con_id);
        pay.setStatus(status);
        payRepository.save(pay);
        Map<String,String> res = new HashMap<>();
        res.put("status","Req_complete");
        res.put("payid",pay.getPayId().toString());
        return res;
    }

    @PostMapping("/pays/room_pay")
    public String room_paid(@RequestParam Long id)
    {
        Optional<Pay> pay = payRepository.findById(id);
        String result;
        if(pay.isPresent())
        {
            pay.get().setStatus("Paid");
            result = "OK";
        }
        else
        {
            result = "not exist";
        }
        payRepository.save(pay.get());
        return result;
    }
}
