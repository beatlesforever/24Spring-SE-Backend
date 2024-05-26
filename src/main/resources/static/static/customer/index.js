document.getElementById("loginButton").addEventListener("click", function() {
    window.location.href = 'login.html';
});

document.getElementById("registerButton").addEventListener("click", function() {
    window.location.href = 'registration.html';
});

document.getElementById("checkRoomsButton").addEventListener("click", function() {
    fetch('http://localhost:8080/api/rooms/available')
        .then(response => response.json())
        .then(responseData => {
            const data = responseData.data; // 提取包含房间数据的数组
            const roomsContainer = document.getElementById("roomsContainer");
            const roomsList = document.getElementById("roomsList");
            roomsList.innerHTML = ''; // 清空之前的结果

            data.forEach(room => {
                const roomDiv = document.createElement("div");
                roomDiv.classList.add("room");
                roomDiv.textContent = `房间号: ${room.roomId}`;
                roomsList.appendChild(roomDiv);
            });

            roomsContainer.style.display = 'block'; // 显示房间列表容器
        })
        .catch(error => {
            console.error('Error:', error);
            alert('查询空房间时发生错误，请稍后重试。');
        });
});
