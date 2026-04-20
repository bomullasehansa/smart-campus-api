# Populate Smart Campus API with demo data for the video demonstration

$baseUrl = "http://localhost:8080/api/v1"

Write-Host ">>> Initializing Smart Campus Demo Data..." -ForegroundColor Cyan

# 1. Create a Room
Write-Host "Creating Room LIB-301..."
Invoke-RestMethod -Uri "$baseUrl/rooms" -Method Post -ContentType "application/json" -Body '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'

# 2. Create another Room
Write-Host "Creating Room SCR-202..."
Invoke-RestMethod -Uri "$baseUrl/rooms" -Method Post -ContentType "application/json" -Body '{"id": "SCR-202", "name": "Computer Lab 2", "capacity": 30}'

# 3. Register Sensors
Write-Host "Registering Sensors..."
Invoke-RestMethod -Uri "$baseUrl/sensors" -Method Post -ContentType "application/json" -Body '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "roomId": "LIB-301"}'
Invoke-RestMethod -Uri "$baseUrl/sensors" -Method Post -ContentType "application/json" -Body '{"id": "CO2-005", "type": "CO2", "status": "ACTIVE", "roomId": "SCR-202"}'
Invoke-RestMethod -Uri "$baseUrl/sensors" -Method Post -ContentType "application/json" -Body '{"id": "HUM-001", "type": "Humidity", "status": "MAINTENANCE", "roomId": "LIB-301"}'

# 4. Add Initial Readings
Write-Host "Adding Initial Readings..."
Invoke-RestMethod -Uri "$baseUrl/sensors/TEMP-001/readings" -Method Post -ContentType "application/json" -Body '{"value": 21.5}'
Invoke-RestMethod -Uri "$baseUrl/sensors/CO2-005/readings" -Method Post -ContentType "application/json" -Body '{"value": 450.0}'

Write-Host ">>> Demo registration complete!" -ForegroundColor Green
