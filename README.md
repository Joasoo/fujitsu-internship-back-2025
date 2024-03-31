## Introduction
This is a delivery fee calculation application. The delivery fee is calculated based on:
- Regional base fee (vehicle, city)
- Extra fees (vehicle, weather conditions)

### Author
Markus Joasoo

### Used technologies
- Java 17+
- H2 Database
- Spring Boot 3.2.3
- Lombok 1.18.30
- Mapstruct 1.5.5
- Liquibase 4.27.0
- Hibernate Validator 8.0.1
- Jackson-dataformat-XML 2.15.4
- OpenAPI 2.3.0

### How to start the application
Clone the repository:

`git clone [project url]`

Navigate to the project directory:

`cd [project folder]`

Build the project (this will also download the necessary dependencies):

`./gradlew build`

Run the application (liquibase will setup database):

`./gradlew bootRun`

### OpenApi
http://localhost:8080/swagger-ui/index.html#/
