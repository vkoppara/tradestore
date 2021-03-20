# tradestore project  - Ganga Venkata

1) git clone https://github.com/vkoppara/tradestore.git
2) mvnw.cmd clean install
3) mvnw.cmd test
4) Port 44 (https) is opened for accessing the services


# Service EndPoints
1) HttpMethod: POST URL: https://localhost:8443/tradeRecords
   Headers: Content-Type=application/json
   Sample request Body: 
   {
    "tradeId": "T1",
    "version": 1,
    "counterPartyId": "CP-1",
    "bookId": "B1",
    "maturityDate": "21/03/2021"    
   }
   Response:
   {
    "tradeId": "T1",
    "version": 1,
    "statusMessage": "Inserted/Updated Successfully"
    }

2) 
