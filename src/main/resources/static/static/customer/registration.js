document.getElementById("registerForm").addEventListener("submit", function(event) {
    event.preventDefault();
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var roomId = document.getElementById("roomId").value;

    var data = {
        username: username,
        password: password,
        roomId: roomId
    };

    fetch('/api/users/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.message === "用户注册成功") {
                console.log('Registration response:', data);
                $('#successModal').modal('show');
            } else {
                $('#errorModal').modal('show');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            $('#errorModal').modal('show');
        });
});

document.getElementById("checkRoomsButton").addEventListener("click", function() {
    fetch('/api/rooms/available')
        .then(response => response.json())
        .then(responseData => {
            const data = responseData.data;
            const roomsList = document.getElementById("roomsList");
            roomsList.innerHTML = '';

            data.forEach(room => {
                const roomDiv = document.createElement("div");
                roomDiv.classList.add("room");
                roomDiv.textContent = `房间号: ${room.roomId}`;
                roomsList.appendChild(roomDiv);
            });

            // 显示 Modal
            $('#roomsModal').modal('show');
        })
        .catch(error => {
            console.error('Error:', error);
            $('#roomErrorModal').modal('show');
        });
});

// 在成功注册模态框中添加确定按钮的事件监听器，以便在点击确定按钮时重定向到登录页面
document.getElementById("successCloseButton").addEventListener("click", function() {
    window.location.href = 'login.html';
});
