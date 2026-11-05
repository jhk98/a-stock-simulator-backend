# a-stock-simulator-backend

모의 주식 시뮬레이터

## 기술 스택

| 구분 | 버전 | 비고 |
|---|---|---|
| Java | **25 (LTS)** | |
| Spring Boot | **4.1.0** | |
| Gradle | **9.5.1** | |
| DB | **MySQL 8** | AWS Server에 구축 |
| ORM | **Spring Data JPA / Hibernate** | DB 접근 및 Entity 매핑 |
| MySQL Driver | **MySQL Connector/J 9.7.0** | Spring Boot와 MySQL 연결 |

## 배포 및 로그

Jenkins가 GitHub 변경 사항을 확인해 백엔드를 빌드하고 Docker 이미지를 Amazon ECR에 저장합니다. AWS Systems Manager를 통해 EC2에서 배포 명령을 실행하고 Docker Compose로 서비스를 구동합니다.

운영 환경에서는 Nginx가 HTTPS 연결을 처리하고 `/api/` 요청을 Spring Boot로 전달합니다. 백엔드는 Logback으로 `/app/logs/application.log`에 로그를 기록합니다. 이 디렉터리를 EC2 호스트의 `/var/log/a-stock-simulator`에 연결하고, CloudWatch Agent가 파일을 읽어 `/a-stock-simulator/backend` 로그 그룹으로 전송하도록 구성했습니다.

주소 : https://stock.prac.click/
