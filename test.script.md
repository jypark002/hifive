0. Room 상태 조회 - 항상
http GET http://localhost:8084/roomStates

1. 회의 생성 : apply
http POST http://localhost:8081/conferences conferenceId=1 status="CREATED" roomNumber=1

2. 회의 취소 : cancelApply
http POST http://localhost:8081/conferences conferenceId=1 status="CANCELD" roomNumber=1