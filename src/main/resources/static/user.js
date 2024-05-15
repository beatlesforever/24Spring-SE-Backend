// Get token from local storage
var globalToken = localStorage.getItem('token');

function getRoomInfo() {
    const roomId = document.getElementById('roomIdInput').value;

    const requestOptions = {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + globalToken,
            'Content-Type': 'application/json'
        }
    };

    fetch('/api/rooms/' + roomId, requestOptions)
        .then(response => response.json())
        .then(data => {
            if (data.status === "200 OK") {
                displayRoomInfo(data.data);
            } else {
                alert(data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("An error occurred while fetching room information.");
        });
}

function displayRoomInfo(roomData) {
    const roomInfoDiv = document.getElementById('roomInfo');
    roomInfoDiv.innerHTML = `
        <h2>Room ID: ${roomData.roomId}</h2>
        <p>Current Temperature: ${roomData.currentTemperature}</p>
        <p>Target Temperature: ${roomData.targetTemperature}</p>
        <p>Fan Speed: ${roomData.fanSpeed}</p>
        <p>Temperature Threshold: ${roomData.temperatureThreshold}</p>
        <p>Status: ${roomData.status}</p>
        <p>Mode: ${roomData.mode}</p>
        <p>Last Update: ${roomData.lastUpdate}</p>
        <p>Service Status: ${roomData.serviceStatus}</p>
        <p>Energy Consumed: ${roomData.energyConsumed}</p>
        <p>Cost Accumulated: ${roomData.costAccumulated}</p>
    `;
}
