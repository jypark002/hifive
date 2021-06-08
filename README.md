![booking-meeting-room](https://user-images.githubusercontent.com/80938080/118491280-c9c47580-b759-11eb-9f7c-3ca3a17b83a2.png)

# HiFive - 회의실 신청 시스템

본 과제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성하였습니다.  
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 Project HiFive의 팀과제 수행 결과입니다.


# Table of contents

- [HiFive - 회의실 신청 시스템](#HiFive---회의실-신청-시스템)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#DDD-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일아웃 (HPA)](#오토스케일아웃_(HPA))
    - [ConfigMap](#ConfigMap)
    - [Zero-downtime deploy (Readiness Probe)](#Zero-downtime_deploy_(Readiness_Probe))
    - [Self-healing (Liveness Probe)](#Self-healing_(Liveness_Probe))


# 서비스 시나리오


기능적 요구사항
1. 회의실 관리자는 회의실을 등록한다.
2. 고객이 회의실을 신청한다.
3. 고객이 회의실 사용 비용을 결제한다.
4. 신청이 되면 회의실이 할당되고 현황이 업데이트 된다. (FULL)
6. 고객이 신청을 취소할 수 있다.
7. 신청이 취소되면 회의실 할당이 취소되고 현황이 업데이트 된다. (CANCELED, EMPTY)
8. 회의실 현황은 언제나 확인할 수 있다.
9. 회의실 할당/할당취소가 되면 알림을 보낸다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않으면 회의실은 신청되지 않는다. `Sync 호출` 
1. 장애격리
    1. 회의실 관리 기능이 수행되지 않더라도 신청은 365일 24시간 가능해야 한다. `Async (event-driven)`, `Eventual Consistency`
    1. 결제시스템이 과중되면 신청을 잠시동안 받지 않고 잠시후에 신청하도록 유도한다. `Circuit breaker`, `fallback`
1. 성능
    1. 고객은 회의실 현황을 언제든지 확인할 수 있어야 한다. `CQRS`
    1. 신청 상태가 생성/취소되면 알림을 줄 수 있어야 한다. `Event driven`


# 체크포인트
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW
1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)

# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
![image](https://user-images.githubusercontent.com/81279673/120967847-fee54600-c7a2-11eb-92dc-198f3c1ef19a.png)

## TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/81279673/120967821-f55bde00-c7a2-11eb-97e5-a3895e1632ce.png)

## Event Storming 결과
* MSAEZ 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/pYauKq27pAMMO4ZZcMLRDtjzgIv1/share/40d9c225e0f9826deff3b8035d97b38f


### 이벤트 도출
![image](https://user-images.githubusercontent.com/81279673/120964712-b3309d80-c79e-11eb-9e12-03e968f6f7fd.png)

### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/81279673/120964787-cfccd580-c79e-11eb-9746-08b844f44181.png)

    - 이벤트스토밍 과정 중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
    - 회의실 선택, 취소를 위한 신청건 선택, 결제버튼 선택, 결제버튼 선택은 UI이벤트이므로 대상에서 제외함

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/81279673/120964860-effc9480-c79e-11eb-9858-89bf32d3ba2f.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/81279673/120964886-f985fc80-c79e-11eb-837d-1302e29b4e9b.png)

    - 신청, 결제, 회의실 관리 어그리게잇을 생성하고 그와 연결된 command와 event들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌

### 바운디드 컨텍스트로 묶기
![image](https://user-images.githubusercontent.com/81279673/120964904-07d41880-c79f-11eb-9049-88d11fa059a3.png)

    - 도메인 서열 분리 
        - Core Domain:  conference, room : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 conference 의 경우 1주일 1회 미만, room 의 경우 1개월 1회 미만
        - Supporting Domain:   customer center : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain:   pay : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)

### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)
![image](https://user-images.githubusercontent.com/81279673/120964924-11f61700-c79f-11eb-9b3a-10cdf6ed50e4.png)

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)
![image](https://user-images.githubusercontent.com/81279673/120964957-1c181580-c79f-11eb-8f31-00dd15712190.png)

### 완성된 1차 모형
![image](https://user-images.githubusercontent.com/81279673/120964986-276b4100-c79f-11eb-9a9a-ed94470edffd.png)

    - View Model 추가
    - 팀원 중 외국인이 투입되어 유비쿼터스 랭귀지인 영어로 변경함	

### 기능적 요구사항 검증
![image](https://user-images.githubusercontent.com/81279673/120969885-ab282c00-c7a5-11eb-9dcc-aafb74c4d962.png)

    - 회의실 관리자가 회의실을 등록한다 (ok)
    - 고객이 회의실을 신청한다. (ok)
    - 고객이 결제한다 (ok)    
    - 회의실이 할당되면 회의실 현황이 업데이트 된다 (ok)    

![image](https://user-images.githubusercontent.com/81279673/120969797-9186e480-c7a5-11eb-8582-35a2517616e5.png)

    - 고객이 신청을 취소할 수 있다. (ok)
    - 신청이 취소되면 회의실 할당이 취소된다. (ok)

### 비기능 요구사항 검증
![image](https://user-images.githubusercontent.com/81279673/120970763-cfd0d380-c7a6-11eb-9a6b-4760792b3815.png)

    - 트랜잭션
        1. 결제가 되지 않으면 회의실은 신청되지 않는다. `Sync 호출` 
    - 장애격리
        2. 회의실 관리 기능이 수행되지 않더라도 신청은 365일 24시간 가능해야 한다. `Async (event-driven)`, `Eventual Consistency`
        3. 결제시스템이 과중되면 신청을 잠시동안 받지 않고 잠시후에 신청하도록 유도한다. `Circuit breaker`, `fallback`
    - 성능
        4. 고객은 회의실 현황을 언제든지 확인할 수 있어야 한다. `CQRS`
        5. 신청 상태가 생성/취소되면 알림을 줄 수 있어야 한다. `Event driven`


### 완성된 모델
![image](https://user-images.githubusercontent.com/81279673/120965022-3520c680-c79f-11eb-9f13-dc80b8872f3f.png)

    - 수정된 모델은 모든 요구사항을 커버함.

## 헥사고날 아키텍처 다이어그램 도출
- 외부에서 들어오는 요청을 인바운드 포트를 호출해서 처리하는 인바운드 어댑터와 비즈니스 로직에서 들어온 요청을 회부 서비스를 호출해서 처리하는 아웃바운드 어댑터로 분리
- 호출관계에서 Pub/Sub 과 Req/Resp 를 구분함
- 서브 도메인과 바운디드 컨텍스트의 분리: 각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐
- 회의(Conference)의 경우 Polyglot 적용을 위해 Hsql로 설계

<img width="1200" alt="헥사고날 최종" src="https://user-images.githubusercontent.com/80210609/120962597-00ab0b80-c79b-11eb-9917-7c271b2a2434.PNG">


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 Bounded Context별로 마이크로서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다. (각 서비스의 포트넘버는 8081 ~ 8084, 8088 이다)

```
cd conference
mvn spring-boot:run

cd pay
mvn spring-boot:run 

cd room
mvn spring-boot:run  

cd customerCenter
mvn spring-boot:run 

cd gateway
mvn spring-boot:run
```

## DDD 의 적용

- msaez.io에서 이벤트스토밍을 통해 DDD를 작성하고 Aggregate 단위로 Entity를 선언하여 구현을 진행하였다.

> Conference 서비스의 Conference.java
```java
package hifive;

import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.persistence.*;

import java.util.Map;

@Entity
@Table(name="Conference_table")
public class Conference{

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long conferenceId;
  private String status;
  private Long payId;
  private Long roomNumber;

  @PrePersist //해당 엔티티를 저장한 후
  public void onPrePersist(){
      
    setStatus("CREATED");
    Applied applied = new Applied();
    //BeanUtils.copyProperties는 원본객체의 필드 값을 타겟 객체의 필드값으로 복사하는 유틸인데, 필드이름과 타입이 동일해야함.
    applied.setConferenceId(this.getConferenceId());
    applied.setConferenceStatus(this.getStatus());
    applied.setRoomNumber(this.getRoomNumber());
    applied.publishAfterCommit();
    //신청내역이 카프카에 올라감
    
    Map<String, String> res = ConferenceApplication.applicationContext
            .getBean(hifive.external.PayService.class)
            .paid(applied);
    //결제 아이디가 있고, 결제 상태로 돌아온 경우 회의 상태로 결제로 바꾼다.
    if (res.get("status").equals("Req_complete")) {
      this.setStatus("Req complete");
    }
    this.setPayId(Long.valueOf(res.get("payid")));

    return;
  }

  @PreRemove //해당 엔티티를 삭제하기 전 (회의를 삭제하면 취소신청 이벤트 생성)
  public void onPreRemove(){
    System.out.println("#################################### PreRemove : ConferenceId=" + this.getConferenceId());
    ApplyCanceled applyCanceled = new ApplyCanceled();
    applyCanceled.setConferenceId(this.getConferenceId());
    applyCanceled.setConferenceStatus("CANCELED");
    applyCanceled.setPayId(this.getPayId());
    applyCanceled.publishAfterCommit();
    //삭제하고 ApplyCanceled 이벤트 카프카에 전송
  }

  public Long getConferenceId() {
    return conferenceId;
  }
  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  public Long getPayId() {
    return payId;
  }
  public void setPayId(Long payId) {
    this.payId = payId;
  }

  public Long getRoomNumber() {
    return roomNumber;
  }
  public void setRoomNumber(Long roomNumber) {
    this.roomNumber = roomNumber;
  }
}

```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 기반의 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리 없이 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다.

> Conference 서비스의 ConferenceRepository.java
```java
package hifive;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="conferences", path="conferences")
public interface ConferenceRepository extends PagingAndSortingRepository<Conference, Long>{

}
```
> Conference 서비스의 PolicyHandler.java
```java
package hifive;

import java.util.Optional;
import hifive.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ConferenceRepository conferenceRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverAssigned_UpdateStatus(@Payload Assigned assigned){
    
        if(!assigned.validate()) return;
        
        Optional<Conference> confOptional = conferenceRepository.findById(assigned.getConferenceId());
        Conference conference = confOptional.get();
        conference.setPayId(assigned.getPayId());
        conference.setStatus(assigned.getRoomStatus());
        conferenceRepository.save(conference);
    }
}
```

- 적용 후 REST API 의 테스트
```
# conference 서비스의 회의실 신청
http post http://localhost:8081/conferences status="" payId=0 roomNumber=1

# conference 서비스의 회의실 신청 취소
http delete http://localhost:8081/conferences/1

# 회의실 상태 확인
http GET http://localhost:8084/roomStates
```
> 회의실 신청 후 Conference 동작 결과
![Cap 2021-06-07 21-39-53-966](https://user-images.githubusercontent.com/80938080/121018071-f60f6700-c7d8-11eb-889d-fb674d1e8189.png)

## CQRS

- Materialized View 구현을 통해 다른 마이크로서비스의 데이터 원본에 접근없이 내 서비스의 화면 구성과 잦은 조회가 가능하게 하였습니다. 본 과제에서 View 서비스는 CustomerCenter 서비스가 수행하며 회의실 상태를 보여준다.

> 회의실 신청 후 customerCenter 결과
![Cap 2021-06-07 22-08-17-580](https://user-images.githubusercontent.com/80938080/121022024-edb92b00-c7dc-11eb-872b-23b51f1b1d57.png)

## 폴리글랏 퍼시스턴스

- 회의(conference)의 경우 H2 DB인 결제(pay)/회의실(room) 서비스와 달리 Hsql로 구현하여 MSA의 서비스간 서로 다른 종류의 DB에도 문제없이 동작하여 다형성을 만족하는지 확인하였다.

> pay, room 서비스의 pom.xml 설정
```xml
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
```
> conference 서비스의 pom.xml 설정
```xml
    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <scope>runtime</scope>
    </dependency>
```
## Gateway 적용
- API Gateway를 통하여 마이크로서비스들의 진입점을 단일화하였습니다.
> gateway > application.xml 설정
```yaml
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: conference
          uri: http://conference:8080
          predicates:
            - Path=/conferences/** 
        - id: pay
          uri: http://pay:8080
          predicates:
            - Path=/pays/** 
        - id: room
          uri: http://room:8080
          predicates:
            - Path=/rooms/** 
        - id: customerCenter
          uri: http://customerCenter:8080
          predicates:
            - Path= /roomStates/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 Conference -> Pay 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

> Pay 서비스의 external\PayService.java

```java
package hifive.external;

@FeignClient(name="pay", url="http://pay:8080")
public interface PayService {

    @RequestMapping(method= RequestMethod.GET, path="/pays/paid")
    public Map<String,String> paid(@RequestParam("status") String status, @RequestParam("conferenceId") Long conferenceId, @RequestParam("roomNumber") Long roomNumber);
}
```

- 예약을 받은 직후(@PostPersist) 결제를 요청하도록 처리

> Conference 서비스의 Conference.java (Entity)

```java
    @PostPersist //해당 엔티티를 저장한 후
    public void onPostPersist(){
    
        setStatus("CREATED");
        Applied applied = new Applied();
        //BeanUtils.copyProperties는 원본객체의 필드 값을 타겟 객체의 필드값으로 복사하는 유틸인데, 필드이름과 타입이 동일해야함.
        applied.setConferenceId(this.getConferenceId());
        applied.setConferenceStatus(this.getStatus());
        applied.setRoomNumber(this.getRoomNumber());
        applied.publishAfterCommit();
        //신청내역이 카프카에 올라감
        try {
            // 결제 서비스 Request
            Map<String, String> res = ConferenceApplication.applicationContext
                    .getBean(hifive.external.PayService.class)
                    .paid(applied);
            //결제 아이디가 있고, 결제 상태로 돌아온 경우 회의 상태로 결제로 바꾼다.
            if (res.get("status").equals("Req_complete")) {
                this.setStatus("Req complete");
            }
            this.setPayId(Long.valueOf(res.get("payid")));
            ConferenceApplication.applicationContext.getBean(javax.persistence.EntityManager.class).flush();
            return;
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 예약도 못받는다는 것을 확인:


```
# 결제 (pay) 서비스를 잠시 내려놓음 (ctrl+c)

# 결제 처리
http post http://localhost:8081/conferences status="" payId=0 roomNumber=1   #Fail
http post http://localhost:8081/conferences status="" payId=0 roomNumber=2   #Fail
```
> 결제 요청 오류 발생
![Cap 2021-06-07 22-24-26-184](https://user-images.githubusercontent.com/80938080/121024411-28bc5e00-c7df-11eb-9a84-d3095683d49c.png)
```
#결제서비스 재기동
cd pay
mvn spring-boot:run

#주문처리
http post http://localhost:8081/conferences status="" payId=0 roomNumber=1   #Success
http post http://localhost:8081/conferences status="" payId=0 roomNumber=2   #Success
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 회의실 관리(Room)로 이를 알려주는 행위는 동기식이 아니라 비동기식으로 처리하여 회의실 관리 서비스의 처리를 위하여 결제가 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```java
package hifive;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long payId;
    private String status;
    private Long conferenceId;
    private Long roomNumber;

    @PostPersist
    public void onPostPersist(){

        if (this.getStatus() != "PAID") return;

        System.out.println("********************* Pay PostPersist Start. PayStatus=" + this.getStatus());

        Paid paid = new Paid();
        paid.setPayId(this.payId);
        paid.setPayStatus(this.status);
        paid.setConferenceId(this.conferenceId);
        paid.setRoomNumber(this.roomNumber);
        //BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();

        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(toString());
        System.out.println("********************* Pay PostPersist End.");
    }

}
```
- 상점 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```java
package hifive;

@Service
public class PolicyHandler {
    @Autowired
    RoomRepository roomRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_RoomAssign(@Payload Paid paid) {

        if (!paid.validate()) {
            System.out.println("##### listener RoomAssign Fail");
            return;
        } else {
            System.out.println("\n\n##### listener RoomAssign : " + paid.toJson() + "\n\n");

            //예약 신청한 방 번호 조회, 퇴실 개념이 없기 때문에 상태 검사 하지 않음
            Optional<Room> optionalRoom = roomRepository.findById(paid.getRoomNumber());

            Room room = optionalRoom.get();
            room.setRoomStatus("FULL");
            room.setUsedCount(room.getUsedCount() + 1);
            room.setConferenceId(paid.getConferenceId());
            room.setPayId(paid.getPayId());

            System.out.println("##### 방배정 확인");
            System.out.println("[ RoomStatus : " + room.getRoomStatus() + ", RoomNumber : " + room.getRoomNumber() + ", UsedCount : " + room.getUsedCount() + ", ConferenceId : " + room.getConferenceId() + "," + room.getPayId() + "]");
            roomRepository.save(room);
        }
    }
}

```


회의실 관리 시스템은 주문/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 회의실 관리 시스템이 유지보수로 인해 잠시 내려간 상태라도 신청을 받는데 문제가 없다:
```
# 회의실 관리 시스템 (Room) 를 잠시 내려놓음 (ctrl+c)

#신청 처리
http post http://localhost:8081/conferences status="" payId=0 roomNumber=1   #Success
http post http://localhost:8081/conferences status="" payId=0 roomNumber=2   #Success

#신청 상태 확인
http localhost:8080/conferences     # 신청 상태 안바뀜 확인

#회의실 관리 서비스 기동
cd room
mvn spring-boot:run

#신청 상태 확인
http localhost:8080/conferences     # 모든 신청의 상태가 "할당됨"으로 확인
```


# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 도커라이징, deploy 및
서비스 생성을 진행하였다.

- git에서 소스 가져오기
```
git clone https://github.com/jypark002/hifive.git
```
- Build 하기
```
cd hifive
cd conference
mvn package
```
- 도커라이징 : Azure 레지스트리에 도커 이미지 푸시하기
```
az acr build --registry skccuser05 --image skccuser05.azurecr.io/conference:latest .
```
- 컨테이너라이징 : 디플로이 생성 확인
```
kubectl create deploy conference --image=skccuser05.azurecr.io/conference:latest
```
- 컨테이너라이징 : 서비스 생성
```
kubectl expose deploy conference --port=8080
```
> customerCenter, pay, room, gateway 서비스도 동일한 배포 작업 반복

## 동기식 호출 / 서킷 브레이킹 / 장애격리

- Spring FeignClient + Hystrix을 사용하여 서킷 브레이킹 구현
- Hystrix 설정 : 결제 요청 쓰레드의 처리 시간이 410ms가 넘어서기 시작한 후 어느정도 지속되면 서킷 브레이커가 닫히도록 설정
- 결제를 요청하는 Conference 서비스에서 Hystrix 설정

> Conference 서비스의 application.yml 파일
```yaml
feign:
  hystrix:
    enabled: true
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```

- 결제 서비스(pay)에서 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
> Pay 서비스의 Pay.java 파일
```java
    @PostPersist
    public void onPostPersist(){
        if (this.getStatus() != "PAID") return;

        Paid paid = new Paid();
        paid.setPayId(this.payId);
        paid.setPayStatus(this.status);
        paid.setConferenceId(this.conferenceId);
        paid.setRoomNumber(this.roomNumber);
        paid.publishAfterCommit();

        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

- 부하테스터 siege 툴을 통한 서킷브레이커 동작 확인:
    - 동시사용자 100명
    - 60초 동안 실시

```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://52.231.34.176:8080/conferences POST {"status":"", "payId":0, "roomNumber":1}'
```
- 부하가 발생하고 서킷브레이커가 발동하여 요청 실패하였고, 밀린 부하가 다시 처리되면서 회의실 신청(Apply)를 받기 시작
![Cap 2021-06-08 10-37-57-954](https://user-images.githubusercontent.com/80938080/121108974-a450f600-c845-11eb-94ed-621b894f0da1.png)


- 운영 중인 시스템은 죽지 않고 지속적으로 서킷브레이커에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 47.10% 가 성공하였고, 53%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.
![Cap 2021-06-08 10-39-01-129](https://user-images.githubusercontent.com/80938080/121109032-bdf23d80-c845-11eb-906b-9416924c6c1c.png)


## 오토스케일아웃 (HPA)
앞서 서킷브레이커는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- conference의 deployment.yaml 파일 설정

<img width="400" alt="야믈" src="https://user-images.githubusercontent.com/80210609/121058380-3b449080-c7fb-11eb-92ab-20852519d9d9.PNG">

- 신청서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:

```
kubectl autoscale deploy confenrence --min=1 --max=10 --cpu-percent=15
```

- hpa 설정 확인

<img width="600" alt="스케일-hpa" src="https://user-images.githubusercontent.com/80210609/121057419-37fcd500-c7fa-11eb-81ff-8d5062a219b4.PNG">


- CB 에서 했던 방식대로 워크로드를 1분 동안 걸어준다.
```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://conference:8080/conferences POST {"roomNumber": "123"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy conference -w
```

- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다:
<img width="700" alt="스케일최종" src="https://user-images.githubusercontent.com/80210609/121056827-937a9300-c7f9-11eb-9ebc-ca86c271d3c3.PNG">

- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
<img width="600" alt="상태" src="https://user-images.githubusercontent.com/80210609/121057028-cde43000-c7f9-11eb-88d2-c022dddca49f.PNG">
  

## ConfigMap
- 환경정보로 변경 시 ConfigMap으로 설정함

- 리터럴 값으로부터 ConfigMap 생성
![image](https://user-images.githubusercontent.com/81279673/121073309-4ef8f280-c80d-11eb-998e-d13b361d53e4.png)

- 설정된 ConfigMap 정보 가져오기
![image](https://user-images.githubusercontent.com/81279673/121074021-42c16500-c80e-11eb-8db8-2497dcc099e1.png)
![image](https://user-images.githubusercontent.com/81279673/121073595-a9924e80-c80d-11eb-80e5-88b40effb31b.png)

- 관련된 프로그램(application.yaml, PayService.java) 적용
![image](https://user-images.githubusercontent.com/81279673/121073814-fe35c980-c80d-11eb-980b-5dcc1c6d7019.png)
![image](https://user-images.githubusercontent.com/81279673/121073824-ffff8d00-c80d-11eb-8bda-cc188492d138.png)

## Zero-downtime deploy (Readiness Probe)
- Room 서비스에 kubectl apply -f deployment_non_readiness.yml 을 통해 readiness Probe 옵션을 제거하고 컨테이너 상태 실시간 확인
![non_de](https://user-images.githubusercontent.com/47212652/121105020-32c17980-c83e-11eb-8e10-c27ee89a369d.PNG)

- Room 서비스에 kubectl apply -f deployment.yml 을 통해 readiness Probe 옵션 적용
- readinessProbe 옵션 추가  
    > initialDelaySeconds: 10  
    > timeoutSeconds: 2  
    > periodSeconds: 5  
    > failureThreshold: 10  

- 컨테이너 상태 실시간 확인
![dep](https://user-images.githubusercontent.com/47212652/121105025-33f2a680-c83e-11eb-9db0-ee2206a966fe.PNG)

## Self-healing (Liveness Probe)
- Pay 서비스에 kubectl apply -f deployment.yml 을 통해 liveness Probe 옵션 적용

- liveness probe 옵션을 추가
- initialDelaySeconds: 10
- timeoutSeconds: 2
- periodSeconds: 5
- failureThreshold: 5
                 
  ![스크린샷 2021-06-08 오후 2 16 45](https://user-images.githubusercontent.com/40500484/121127061-2cde8f00-c864-11eb-8b4f-7d3abcba60b3.png)


- Pay 서비스에 liveness가 적용된 것을 확인

- Http Get Pay/live를 통해서 컨테이너 상태 실시간 확인 및 재시동 

  
  ![스크린샷 2021-06-07 오후 9 45 31](https://user-images.githubusercontent.com/40500484/121018788-c9a81a80-c7d9-11eb-9013-1a68ccf1a9b1.png)


- Liveness test를 위해 port : 8090으로 변경
- Delay time 등 옵션도 작게 변경
  
  ![스크린샷 2021-06-08 오후 1 59 29](https://user-images.githubusercontent.com/40500484/121125804-1cc5b000-c862-11eb-8d5d-34b5a0ba1df2.png)

- Liveness 적용된 Pay 서비스 , 응답불가로 인한 restart 동작 확인

  ![스크린샷 2021-06-08 오후 1 59 15](https://user-images.githubusercontent.com/40500484/121125928-50083f00-c862-11eb-91dd-c47a74eade37.png)
