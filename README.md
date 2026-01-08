# IronGate-Framework

### 🛡️ Real-Time API-to-DB Validation
Unlike standard automation, IronGate doesn't just check for a `200 OK` status.
1. **API Trigger:** Sends a POST request to the transaction endpoint.
2. **Data Extraction:** Captures the JSON response body.
3. **Database Assertion:** Uses JDBC to query the H2 Database in real-time.
4. **Verification:** Validates that the persisted data matches the API response exactly.
