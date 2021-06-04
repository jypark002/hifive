# Room 마이크로 서비스 구현

## Room

 - Room.java
   > onPostUpdate() 

 - PolicyHandler.java
   > wheneverPaid_RoomAssign(), wheneverPayCanceled_CancelRoomAssign() 수정

 - RoomRepository.java
   > findbyRoomNumber, findbyPayId 추가


# 테스트 
## 회의실 등록
```
http POST localhost:8083/rooms roomStatus=EMPTY usedCount=0 roomNumber=10
```

## 회의실 조회
```
http GET localhost:8083/rooms 
```

## 회의 신청
```
http post http://localhost:8081/conferences status="" payId=0 roomNumber=60
> {"eventType":"Paid","timestamp":"20210604151900","payId":4,"payStatus":null,"conferenceId":4,"roomNumber":60}
> {"eventType":"Applied","timestamp":"20210604151900","conferenceId":4,"conferenceStatus":"CREATED","roomNumber":60}
> {"eventType":"Assigned","timestamp":"20210604151901","roomNumber":60,"roomStatus":"ASSIGNED","conferenceId":4}
```


## 회의 취소신청
```
http delete http://localhost:8081/conferences/4
> {"eventType":"ApplyCanceled","timestamp":"20210604152856","conferenceId":4,"conferenceStatus":"ASSIGNED","payId":0}
> {"eventType":"PayCanceled","timestamp":"20210604152856","payId":4,"payStatus":"ASSIGNED","conferenceId":0}
> {"eventType":"CancelAssigned","timestamp":"20210604152856","id":1,"roomNumber":60,"roomStatus":"CANCELED","conferenceId"
