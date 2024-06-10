# 스터디 올래 (스터디 모임 관리 서비스) 

## 💻프로젝트 소개
- 스터디를 생성하여 모임을 만들 수 있고, 스터디에 가입되어 있는 사용자 및  관심주제 및 지역정보를 등록한 사용자 들에게  알림 서비스를 제공하는 서비스로 로그인, 관심주제 지역정보, 스터디, 모임, 알림(web, 메일) 등 의 기능이 있습니다. 

### [블로그 링크](https://velog.io/@cse05091/series/%EC%8A%A4%ED%94%84%EB%A7%81%EA%B3%BC-JPA-%EA%B8%B0%EB%B0%98-%EC%9B%B9-%EC%95%A0%ED%94%8C%EB%A6%AC%EC%BC%80%EC%9D%B4%EC%85%98-%EA%B0%9C%EB%B0%9C)


## 📌주요기능 
- ##  1. 로그인 (ID + Password 입력방식, 이메일 전송)

  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/9247fd0d-2722-40cd-af68-6e2672f9ea18)
  - #### SpringSecurity 를 사용 - 인증이 필요, 필요없는 요청을 분리
  - #### 회원가입, 이메일 로그인 시 자동로그인 기능을 구현
  - #### 새션이 만료되더라고 로그인을 유지하기 위해 (RememberMe) 구현


-------------------------------------------------------------

- ##  2. 메일
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/2fe56657-436f-441b-91cb-2b9b8ca83456)
  - #### 회원가입 인증, 이메일 로그인, 스터디 및 모임 알림 기능 제공
  - #### Thymeleaf의 TemplateEngine을 활용하여 이메일 템플릿 구현 - Gmail SMTP 서버 사용

-------------------------------------------------------------
- ##  3. 관심주제, 지역정보
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/3c1114a7-b1ad-4992-9166-fe6d00dd3a89)

  - #### 독자적인 life cycle 을 사용하기 위해서 value 가 아닌  entity로 선언
  - #### 관심주제 및 지역정보는 사용자와, 스터디에서 사용 - 관심주제, 지역이 같은 경우 스터디가 생성시 알림을 주기 위해 사용

- ##  4. 스터디
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/3aaefcbb-dec3-4eb4-80f1-6bb26cb4eb0d)

  - #### 인증된 사용자에게 스터디 가입, 탈퇴, 모임만들기 버튼을 보여주기 위해 타임리프기 제공하는 security관련 테그를 사용
  - ####  study 조회 시 발생하는 쿼리 줄이기 위해 entityGraph사용 (fetch join 사용가능)
  - ####  스터디를 공개하고 팀원모집을 시작하면, 다른 사용자가 스터디를 가입 및 탈퇴 가능

  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/d172c119-f05c-4665-a798-14d4dc7b7651)

  - #### 스터디 설정에서는 소개정보 수정, 배너이미지 변경, 스터디주제, 활동지역 변경, 스터디 상태값 변경 가능
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/246fdc13-3c1b-4621-95bc-8e152d15685e)

  - #### 배너, 프로필 이미지 수정 : 이미지 파일 업로드 시  ->  이미지 파일인지, 이미지 크기 확인 1MB 넘지 않도록 확인

  - #### 주제, 활동지역  : 사용자 관심주제/ 횔동지역 동일


- ##  5. 모임 ,참여
  - #### 모임의 타입 : 선착순 모집, 관리자 승인모집
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/21a02727-b76c-48cb-8b1e-e0015eaf08bf)

   - #### 모인 조회 시 (StartDateTime 으로 내림차순 정렬) 현재 시간과 비교하여 지난 모임인지, 새로운 모임인지 구분
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/9552a85a-5362-43ee-8ecb-3604c4138f94)
   - #### 모임 수정 로직 :
     #### 1) 모집 방법은 수정할 수 없고 모집 인원 수정은 확정된 참가 신청 수 보다는 커야 한다.
     #### 2) 모집 인원을 늘린 선착순 모임의 경우에, 자동으로 추가 인원의 참가 신청을 확정 상태로 변경.
   
   - #### 모임 수정 로직 : 선착순 모임이라면, 대기 중인 모임 참가 신청 중에 가장 빨리 신청한 것을 확정 상태로 변경.


- ##  6. 알림
  ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/24521fbd-4faf-4382-b695-7965b1174337)

  - #### (관심주제, 지역에 대한 신규 스터디, 참여중인 스터디  업데이트 정보, 모임 참가 신청 결과) 에 대한 메일 및 web 알림
  - #### 스프링에서 제공하는 ApplicationEventPublisher와  @Async 기능을 사용한 비동기 이벤트 기반으로 알림 처리
  - #### 이벤트 사용이유 :



    #### 1) 주요 로직 응답 시간에 영향을 주지 않기위해  -> ex) 알림 처리시 에러 발생하여 rollback이 발생하여 기존 기능에 영향을 주는 경우
    #### 2) 코드를 최대한 주요 로직에 집중하기위해 알림 처리 로직은 분리.
    #### 3) 의존성 사이클을 해결하기 위해

  - #### 읽지 않은 알림이 있을경우 알림 아이콘 활성화 기능 :
    ![image](https://github.com/dlcksgh1/studyolle/assets/119422058/cf8df11a-e614-452f-8a9e-208629e99b11)

    #### interceptor의 posthandle 메서드 구현하여 모든 요청에 대해(리다이렉트 요청과 static 리소스 요청제외) 해당 사용자가 읽지 않는 알림이 있는지 전달.

- ## 기타 기능 구현 

   #### 1)  검색(n + 1)문제 -> fetch join과 distinct 로 해결, pagable 사용하여 페이징 구현
   #### 2)  에러 핸들러 구현 -> ControllerAdvice 를 사용하여  exceptionhandler 구현  RuntimeException 에 대하여
     #### 로그인한 사용자가 보낸 요청이라면, 사용자 nickname 과 해당요청에 대하여 로그를 남겨주고,
     #### 로그인한 사용자가 아니라면 해당 요청에 대해서만 로그를 남겨준다.


  

