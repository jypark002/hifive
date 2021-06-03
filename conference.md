# 2021.06.03 수정사항
msa-ez 오류였는지 멤버변수 빠져있는 것이 많네요. 오타도..
## git branch develop-conference
```
git [branch | checkout] develop-conference
git checkout develop-conference
git pull origin develop-conference

git checkout main
git merge develop-confernece
```

## Conferenece

 - Conference.java
   > onPostPersist()
   > onPreRemove()

 - Applied.java
   > conference오타 수정   
   > roomNumber 멤버변수, getter, setter 추가 (msa ez 오류였던 듯)

 - Assigned.java
   > payId 멤버변수, getter, setter 추가 

 - external/Pay.java
   > roomNumber 멤버변수, getter, setter 추가 

 - external/PayService.java
   > request api 요청 내용 수정.

 - PolicyHandler.java
   > wheneverAssigned_UpdateStatus() 
   > wheneverCancelAssigned_UpdateStatus() 수정
   > pub/sub 선 중복으로 인한 불필요한 메소드 whatever() 제거

## Pay
 - Paid.java
   > roomNumber 멤버변수, getter, setter 추가

 - Pay.java
   > roomNumber 멤버변수, getter, setter 추가

 - PayController.java
   > conference의 payservice에서 요청할 api 생성
  
## Room
 - PolicyHandler.java
   > 오타 수정 payCanceled, roomRepository

# 테스트 
## 조회
```
http get http://localhost:8081/conferences
http get http://localhost:8082/pays
```

## 회의 신청
```
http post http://localhost:8081/conferences status="" payId=0 roomNumber=1
이런식으로 kafka에 올라옴(confernece생성 -> applied 이벤트 발생 -> req로 pay 생성 -> paid 이벤트 발생) 
// room에서 이벤트 인지하면 room 생성하고 assigned 이벤트를 발생시켜서 conference updateStatus()가 되도록
>{"eventType":"Paid","timestamp":"20210603143206","payId":1,"payStatus":null,"conferenceId":1,"roomNumber":1}
>{"eventType":"Applied","timestamp":"20210603143206","conferenceId":1,"conferenceStatus":"CREATED","roomNumber":1}
>{"eventType":"Paid","timestamp":"20210603143213","payId":2,"payStatus":null,"conferenceId":2,"roomNumber":2}
>{"eventType":"Applied","timestamp":"20210603143213","conferenceId":2,"conferenceStatus":"CREATED","roomNumber":2}
``` 

## 회의 취소신청
```
http delete http://localhost:8081/conferences/2
> {"eventType":"ApplyCanceled","timestamp":"20210603143241","conferenceId":2,"conferenceStatus":"","payId":0}
> {"eventType":"PayCanceled","timestamp":"20210603143244","payId":2,"payStatus":"","conferenceId":0}
> {"eventType":"PayCanceled","timestamp":"20210603143246","payId":2,"payStatus":"","conferenceId":0}
> {"eventType":"PayCanceled","timestamp":"20210603143248","payId":2,"payStatus":"","conferenceId":0}

3번 올라오는 이유는 어딘가에서 중복으로 올리고 있다는 것인데 아직 못찾았습니다.
```