[![Build Status](https://travis-ci.org/andreyDelay/spring-s3-rest-api.svg?branch=master)](https://travis-ci.org/andreyDelay/spring-s3-rest-api)

# SPRING REST API  
This project represents the result of learning the basics thing related to the Spring Framework.  
The main topics that was learned and used are listed below:
 - Spring Boot as the main driver of the application
 - Spring security with session policy stateless and Jwt Token as authentication of requests.
 - Spring JPA to communicate with MySql database.
 - basic AOP things, such as execution after method invocation.
 - Simple Storage Service through usage of AWS SDK.
 - Maven.
 - Travis as CI CD.
<br/>
<br/>
   

### Practice task
___

>  Необходимо реализовать REST API, которое взаимодействует с файловым хранилищем AWS S3
   и предоставляет возможность получать доступ к файлам и истории загрузок.

Сущности:

> - User
> - Account
> - AccountStatus
> - Event
> - File
> - FileStatus (enum ACTIVE, BANNED, DELETED)
> - User -> List<File> files + List<Events> + Account account

Взаимодействие с S3 должно быть реализовано с помощью AWS SDK.
    
Уровни доступа:

> - ADMIN - полный доступ к приложению
> - MODERATOR - добавление и удаление файлов
> - USER - только чтение всех данных кроме User + Account

Технологии: 
> - Java, 
> - MySQL, 
> - Spring (IoC, Data, Sercurity), 
> - AWS SDK, 
> - MySQL, 
> - Travis, 
> - Docker, 
> - JUnit, 
> - Mockito, 
> - Gradle.

---

####To start the application properly you have to follow next steps:


>  - clone the project
>  - create mysql database with name s3_rest or simply change the values in the [application.properties](/src/main/resources/application.properties)
>  - you also should have your own AWS s3 credentials and then change 2 keys in the file [AwsCredentials.properties](/src/main/resources/AwsCredentials.properties)
>  - Database will be initialized automatically by liquibase during the first start of the application,  
      however you should fill tables with data of admin, moder and user manually to be able to use it in Postman environment.

___

###There are some points you should deal with to get a predictable result of application execution.


<br/>

**Postman environment**.  <br/>

*(You may follow the links below and use this stuff with your postman. This is the way to take usage of api easier.
Also, the collection can give you impression of how many requests api can handle).*

 - [s3 environment](https://drive.google.com/uc?export=download&id=1gowSbcTaG5JBpRbVVdrV6jXFdspzNlEg)
 - [collection of requests](https://drive.google.com/uc?export=download&id=12s3fj4HXkZRoy_maYI-dS9NGEqnOQoAR)
___


####End points of the API  

- Authentication

request type | url | description | permission
--- | --- | --- | ---
POST     |    /api/v1/auth/singup    |    takes DTO object, validates, fill DB    |    permit all    |    
POST     |    /api/v1/auth/singin    |    takes DTO object to return JWT in case of success    |    permit all    |    
POST     |    /api/v1/auth/restore    |    takes DTO object to restore deleted User    |    permit all    |    

- Users 
  
request type | url | description | permission
  :--- | :--- | :--- | :---
GET     |    /api/v1/users/me    |    returns logged in User    |    any ROLE    |    
GET     |    /api/v1/admin/users    |    returns list of all Users    |    ROLE_ADMIN    |    
GET     |    /api/v1/admin/users/{id}    |    returns User by ID    |    ROLE_ADMIN    |    
GET     |    /api/v1/admin/users/status/{status}    |    returns User by status    |    ROLE_ADMIN    |    
PUT     |    /api/v1/users/me    |    updates logged in User(UpdateDto required)    |    any ROLE    |    
PUT     |    /api/v1/admin/users/{id}    |    updates User by ID    |    ROLE_ADMIN    |    
POST     |    /api/v1/admin/users/add    |    adds new User    |    ROLE_ADMIN    |    
DELETE     |    /api/v1/admin/users/{id}    |    deletes User by id    |    ROLE_ADMIN    |    
DELETE     |    /api/v1/users/me    |    deletes logged in User    |    any ROLE    |    


- Accounts

request type | url | description | permission
  :--- | :--- | :--- | :---
GET     |    /api/v1/accounts/me    |    return logged in Account    |    any ROLE    |    
GET     |    /api/v1/admin/accounts    |    returns list of Accounts    |    ROLE_ADMIN    |    
GET     |    /api/v1/admin/accounts/{login}    |    returns Account by ID    |    ROLE_ADMIN    |    
GET     |    /api/v1/admin/accounts/{status}    |    returns Account by status    |    ROLE_ADMIN    |    
DELETE     |    /api/v1/accounts/me    |    deletes logged in Account    |    any ROLE    |    


- Files

request type | url | description | permission
  :--- | :--- | :--- | :---
GET     |    /api/v1/users/me/files    |    returns list of Files for logged in User    |    any ROLE    |    
GET     |    /api/v1/users/me/files/{status}    |    returns list of Files by status for logged in User    |    any ROLE    |    
GET     |    /api/v1/users/me/files/{id}    |    returns File by ID for logged in User    |    any ROLE    |    
GET     |    /api/v1/users/me/files/download/{filename}    |    returns original file by filename for logged in User    |    any ROLE    |    
GET     |    /api/v1/moderator/users/{id}/files/download/{filename}    |    returns original file by filename for concrete User    |    ROLE_ADMIN, ROLE_MODERATOR    |    
PUT     |    /api/v1/moderator/users/{id}/files/update    |    updates file for concrete User    |    ROLE_ADMIN, ROLE_MODERATOR    |    
POST     |    /api/v1/moderator/users/{id}/files/upload    |    uploads file for concrete User    |    ROLE_ADMIN, ROLE_MODERATOR    |    
DELETE     |    /api/v1/moderator/users/{id}/files/{filename}    |    deletes file by filename for concrete User    |    ROLE_ADMIN, ROLE_MODERATOR    |    
DELETE     |    /api/v1/moderator/users/me/files/{filename}    |    deletes file by id for concrete User    |    ROLE_ADMIN, ROLE_MODERATOR    |    


- Events

request type | url | description | permission
  :--- | :--- | :--- | :---
GET     |    /api/v1/admin/users/{id}/events    |    returns events for User by id    |    ROLE_ADMIN    |    
GET     |    /api/v1/admin/users/{id}/events/{id}    |    returns event by id for User by id    |    ROLE_ADMIN    |    
GET     |    /api/v1/users/me/events/{id}    |    returns events for logged in User    |    any ROLE    |    
GET     |    /api/v1/users/me/events    |    returns event by id for logged in User    |    any ROLE    |    
 
___
___


###JSON examples

___


 1. ####Valid JSON object for sing up 


```JSON
{  
    "username":"Your username",  
    "firstName":"Your first name",  
    "lastName":"Your last name",  
    "email":"yourEmail@mail.ru",  
    "password":"yourPassword",  
    "login":"yourLogin"  
}
```


 2. ####Sing in JSON object example


```JSON
    {
    "username":"{{admin}}",
    "password":"{{admin_password}}"
    }
```


 3. ####Restore account body request example


(*not work as expected, better not to user for now*)
```JSON
{
    "username":"{{user}}",
    "password":"{{user_password}}"
}
```


4. ####Пример ответа для роли ADMIN


```JSON
{
    "id": 2,  
    "created": "2021-05-31T09:20:58.000+00:00",  
    "updated": "2021-05-31T09:40:17.000+00:00",  
    "status": "ACTIVE",  
    "username": "administrator",  
    "email": "admin@mail.ru",  
    "firstName": "Oleg",  
    "lastName": "Ivanov",  
    "password": "$2y$04$6tDgZOGnH1oF0dZnwjS2uegx.p4bnbn5miiunbBJHrxJo0ZGne3WO",  
    "events": [],  
    "files": [],  
    "roles": [  
          {  
            "id": 3,  
            "name": "ROLE_ADMIN"  
          }]  
}
```