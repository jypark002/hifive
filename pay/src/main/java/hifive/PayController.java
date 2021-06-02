package hifive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 @RestController
 public class PayController {

  @Autowired PayRepository payRepository;

  @PostMapping("/room_applied")
  public String room_reg(Applied applied)
  {
    Long con_id = applied.getConferenceId();
    Long room_no = applied.getRoomNumber();
    String con_status = applied.getConferenceStatus();
    Pay pay = new Pay();
    pay.setConferenceId(con_id);
    pay.setStatus(con_status);
    payRepository.save(pay);
    return "Pay_complete";
  }

  @GetMapping("/test")
  public void test()
  {
      ApplyCanceled ac = new ApplyCanceled();
      ac.setPayId(1l);
      ac.setConferenceId(1l);
      ac.setConferenceStatus("OK");
      ac.publish();
  }
 }
