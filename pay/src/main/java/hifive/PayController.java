package hifive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

 @RestController
 public class PayController {

  @Autowired PayRepository payRepository;
//
//  @PostMapping("/room_applied")
//  public String room_reg(Applied applied)
//  {
//    Long con_id = applied.getConferenceId();
//    Long room_no = applied.getRoomNumber();
//    String con_status = applied.getConferenceStatus();
//    Pay pay = new Pay();
//    pay.setConferenceId(con_id);
//    pay.setStatus(con_status);
//    payRepository.save(pay);
//    return "Pay_complete";
//  }
//
//  @GetMapping("/test")
//  public void test()
//  {
//      ApplyCanceled ac = new ApplyCanceled();
//      ac.setPayId(1l);
//      ac.setConferenceId(1l);
//      ac.setConferenceStatus("OK");
//      ac.publish();
//  }
//
  @RequestMapping(value = "/pays/paid", method = RequestMethod.GET,  produces = "application/json;charset=UTF-8")
    public Map<String,String> paid(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> res = new HashMap<String,String>();
        res.put("msg","");
        res.put("status","");
        //결제 입장에서는 회의팀에서 체크하고 상태값을 바꿔주는 정도 밖에 할게 없음 현재는
        //회의번호,회의실번호, 상태인데 회의실번호는 여기랑은 큰상관이 없음
        //하지만 추후 사용가능성을 염두에 두고 일단 다 받아들이겠음

        String status = request.getParameter("status");
        Long conferenceId = Long.valueOf(request.getParameter("conferenceId"));
        Long roomNumber = Long.valueOf(request.getParameter("roomNumber"));
        System.out.println("-------------------------");
        System.out.println("PAY CONTROLLER");
        System.out.println(status+" "+conferenceId+" "+ roomNumber+"!");
        System.out.println("-------------------------");
        //Conference 가 생성되어야지 Pay가 생성이 가능하기 때문에
        //Conference는 처음엔 payId가 없고, Pay에서 conferenceId가 존재
        //따라서 Conference에서 pay를 req하는 것은 pay를 생성하고 res를 받아 payId를 입력해줘야함
        try{
            Pay pay = new Pay();
            status = (status.equals("CREATED")? "PAID" : ""); //회의가 생성된 상태이면 결제상태로 변경하고 저장
            pay.setStatus(status);
            pay.setConferenceId(conferenceId);
            pay.setRoomNumber(roomNumber);
            Pay savedPay = payRepository.save(pay);
            res.put("msg","SUCCESS");
            res.put("status","200");
            res.put("payid",(savedPay.getPayId()+""));
        }catch(Exception e){

            e.printStackTrace();
        }
        return res;
    }
 }
