<div align="center">
  <a href="https://github.com/Instagram-Clone-Coding">
    <img src="https://user-images.githubusercontent.com/68049320/148059706-59c1967d-d035-49e1-9557-2149640a8d2a.png" alt="Logo" width="130" height="130">
  </a>
  <h3 align="center">WeGrus-BE-Spring</h3>

  <p align="center">
    인하대학교 SW 프로그래밍 동아리 <b>IGRUS</b> 웹사이트 제작 프로젝트
    <br />
    <a href="https://github.com/WeGrus"><strong>Explore the Organization</strong></a>
    <br /><br />
    <a href="https://github.com/WeGrus/WeGrus-BE-Spring/issues/new?assignees=imgzon3%2C+seonpilKim&labels=bug&template=bug_report.md&title=">Report Bug</a>
    ·
    <a href="https://github.com/WeGrus/WeGrus-BE-Spring/issues/new?assignees=&labels=enhancement&template=feature_request.md&title=">Request Feature</a>
    <br /><br />
    <a href="https://www.facebook.com/IGRUS-445343065594761/">
      <img src="https://img.shields.io/badge/Facebook-1877F2?style=flat-square&logo=Facebook&logoColor=white"/>
    </a>
    <a href="https://www.instagram.com/igrus_inha/">
      <img src="https://img.shields.io/badge/Instagram-E4405F?style=flat-square&logo=Instagram&logoColor=white"/>
    </a>
    <a href="http://pf.kakao.com/_BfRNs/chat">
      <img src="https://img.shields.io/badge/KakaoTalk-FFCD00?style=flat-square&logo=KakaoTalk&logoColor=white"/>
    </a>
  </p>
</div>

## About
### Directory Structure
```txt
/src.main.java.wegrus.clubwebsite
├── /advice
│ ├── GlobalExceptionHandler.java 본문
├── /config
├── /controller
├── /dto
│ ├── /error
│ │ ├── ErrorCode.java
│ │ └── ErrorResponse.java
│ ├── /result
│ │ ├── ResultCode.java
│ │ └── ResultResponse.java
├── /entity
├── /exception
├── /repository
├── /service
├── /util
├── /vo
│ ClubWebsiteApplication.java
```
### Commit Convention
```txt
Type: Subject
ex) Feat: 회원가입 API 추가

Description

Footer 
ex) Resolves: #1, #2
```
- <b>Type</b>
  - Feat: 기능 추가, 삭제, 변경
  - Fix: 버그 수정
  - Refactor: 코드 리팩토링
  - Style: 코드 형식, 정렬 등의 변경. 동작에 영향 x
  - Test: 테스트 코드 추가, 삭제 변경
  - Docs: 문서 추가 삭제 변경. 코드 수정 x
  - Etc: 위에 해당하지 않는 모든 변경
- <b>Description</b>
  - 한 줄당 72자 이내로 작성
  - 최대한 상세히 작성(why - what)
- <b>Footer</b>
  - Resolve(s): Issue 해결 시 사용
  - See Also: 참고할 Issue 있을 시 사용
- <b>Rules</b>
  - 관련된 코드끼리 나누어 Commit
  - 불필요한 Commit 지양
  - 제목은 명령조로 작성
### Database Convention
- <b>Common</b>
  - 소문자 사용
  - 단어를 임의로 축약 x
  - 동사는 능동태 사용
- <b>Table</b>
  - 복수형 사용
  - 교차 테이블(Many to Many): 각 테이블 이름을 `_`(underscore)로 연결 -> Snake case
    > ex) vip_members
- <b>Column</b>
  - PK, FK는 해당 테이블의 `단수명_id`으로 사용
  - boolean 유형은 `_flag` 접미어 사용
  - datetime 유형은 `_date` 접미어 사용
### Collaborative Flow
1. New Issue
2. Create Branch
3. Commit Code
4. Pull Request
    - 가능한 기능별로 나누어서 PR하기
5. Code Review
6. Merge
7. Delete Branch
## Contributors
<table>
  <tr>
    <td align="center">
      <a href="https://github.com/seonpilKim">
        <img src="https://avatars.githubusercontent.com/u/68049320?v=4" width="130px;" alt=""/><br />
        <sub><b>김선필</b></sub></a><br />
        <a href="https://github.com/seonpilKim" title="Code">💻</a>
    </td>
    <td align="center">
      <a href="https://github.com/imgzon3">
        <img src="https://avatars.githubusercontent.com/u/59475880?v=4" width="130px;" alt=""/><br />
        <sub><b>이도경</b></sub></a><br />
        <a href="https://github.com/imgzon3m" title="Code">💻</a>
    </td>
  </tr>
</table>  
