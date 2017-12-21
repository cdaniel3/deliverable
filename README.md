# deliverable issue tracker - restful web services
Backend services for issue tracking built using the Spring framework, RESTful web services, and JWT authentication

***

`git clone https://github.com/cdaniel3/deliverable.git`

`cd deliverable`

`mvn spring-boot:run` to start the application

Authenticate via a POST request (username: *user1*, password: *password*):

`curl -v -H "Content-Type: application/json" -X POST -d '{ "username":"user1","password":"password"}' http://localhost:8080/auth/login`

An access token and refresh token will be generated and returned in the response. Use the access token in the 'Authorization' header of subsequent requests:

`curl -v -X GET -H "Authorization: Bearer eyJhbGyJzdWIiOiJ1c2VyMSIsInNjb3BlcyI6W10sImlzcyI6Imlzc3VlcmNkIiwiZXhwIjoxNTEzODI4NDg5fQ.w44K-MY5o39nkgqMf0zj8JnuVoLT02T9aNHhdaGf1mypocm8nju4Owyx7at4y-g-cT0h9sZpROeWK3mPC97s1g" http://localhost:8080/tickets`

- [ ] Use in-memory db rather than requiring MySql
