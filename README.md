# tradestore project  - Ganga Venkata

1) git clone https://github.com/vkoppara/tradestore.git
2) mvnw.cmd clean install
3) mvnw.cmd test 
4) mvnw.cmd surefire-report:report  #for junit and reports
5) mvnw spring-boot:run
6) Port 8443 (https) is opened for accessing the services
7) https://localhost:8443/h2-console (for h2-console) - use url: jdbc:h2:mem:trade



# Service EndPoints
1) HttpMethod: POST URL: https://localhost:8443/tradeRecords
   Headers: Content-Type=application/json
   ```javascript
   Example request Body: 
   {
    "tradeId": "T1",
    "version": 1,
    "counterPartyId": "CP-1",
    "bookId": "B1",
    "maturityDate": "21/03/2021"    
   }
   Success Response:
   {
    "tradeId": "T1",
    "version": 1,
    "statusMessage": "Inserted/Updated Successfully"
    }
    Failure Response:
    {
    "message": "Failed",
    "details": [
        "Either TradeId or Version is invalid"
       ]
    }
    ```

2) HttpMethod: GET URL: https://localhost:8443/tradeRecords
   Headers: None
   ```javascript
   Example Response:
   [
    {
        "tradeId": "T1",
        "version": 1,
        "counterPartyId": "CP-1",
        "bookId": "B1",
        "maturityDate": "10/03/2021",
        "createdDate": "19/03/2021",
        "expired": false
    },
    {
        "tradeId": "T2",
        "version": 3,
        "counterPartyId": "CP-1",
        "bookId": "B1",
        "maturityDate": "21/03/2021",
        "createdDate": "20/03/2021",
        "expired": false
    },
    {
        "tradeId": "T4",
        "version": 3,
        "counterPartyId": "CP-1",
        "bookId": "B1",
        "maturityDate": "21/03/2021",
        "createdDate": "20/03/2021",
        "expired": false
    }
   ]
   ```
# Response Messages and Scnearios

| Scneario      | Message |   Http Response Code |
| ------------- | ------------- | ------------- |
| Input record's Maturity date is in past | The Maturity Date Cannot be Older than Current Date  | 500 |
| Input record's tradeId is empty or null or the versios is <= 0   | Either TradeId or Version is invalid  | 500 |
| Input record's version is less than the one existing in the system | version is less than existing.. cannot be inserted/updated | 500 |
| Invalid format error e.g. Input date is not the format 'dd/MM/YYYY' | Bad Request | 400 |
| Success | Inserted/Updated Successfully | 200 |
   
# Table records snapshot after the cron job execution
![image](https://user-images.githubusercontent.com/49525515/111865606-16741600-898e-11eb-9f3a-1d554471c780.png)

# Junit Test Report
![image](https://user-images.githubusercontent.com/49525515/111860435-6f7f8200-896d-11eb-8e5a-62472a5327d8.png)


# Assumptions & Notes
1. Version is an integer and is not auto generated by the system. The application checks if the user provides a latest version or the version last inserted. 
2. The system will not update the older version of the record, except for the record with the latest version.
3. The system will not allow to delete any records. 
4. A job runs every day at 12pm to update the expried flag based on if the maturity date is less than current date.
5. Only Json format is allowed to fetch the record or insert/update the records.
6. All the dates are shown in IST and dd/MM/YYYY format.
7. A self-signed certificate is being used for secured http connection. The service endpoints use https and port 8443 port.
8. A h2 database has been used for storing the trade store records. It is in memory database, rebooting of the application will clean the previously inserted records.
9. Maven build tool has been used to build the application.
10. Junits covers all the insert and update scenarios. The developement was not done with TDD.
11. AOP has been used to intercept and to log the time taken by each method.
12. Loggers (logback) has been used in this program. Both Console and File loggings are enabled.

# Next Steps:
1. Create a Docker Image and plug into a CI/CD pipeline
