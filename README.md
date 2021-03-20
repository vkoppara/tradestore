# tradestore project  - Ganga Venkata

1) git clone https://github.com/vkoppara/tradestore.git
2) mvnw.cmd clean install
3) mvnw.cmd test 
4) mvnw.cmd surefire-report:report  #for junit and reports
5) Port 8443 (https) is opened for accessing the services



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
# Junit Test Report
![image](https://user-images.githubusercontent.com/49525515/111860435-6f7f8200-896d-11eb-8e5a-62472a5327d8.png)
