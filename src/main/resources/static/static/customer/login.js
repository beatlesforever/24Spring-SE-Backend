// 声明全局变量存储用户名和令牌
var globalrole = "";
var globalToken = "";

document.getElementById("customerLoginForm").addEventListener("submit", function(event) {
    event.preventDefault();
    // 获取表单数据
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var room = document.getElementById("room").value; // 获取房间号

    // 构建请求体数据
    var data = {
        username: username,
        password: password,
        roomId: room // 将房间号包含在请求体数据中
    };

    // 发送 POST 请求到后端
    fetch('http://localhost:8080/api/users/login', {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(responseData => {
            console.log('Login response:', responseData);
            if (responseData.message === "登录成功") {
                globalrole = responseData.data.role;
                globalToken = responseData.data.token;
                console.log('Token:', globalToken);
                localStorage.setItem('token', globalToken);
                localStorage.setItem('roomId', room);
                localStorage.setItem('username', username);
                localStorage.setItem('password', password);
                // 获取房间信息
                fetchRoomInfo(room, globalToken).then(() => {
                    if (responseData.data.role === "admin") {
                        window.location.href = 'admin.html';
                    } else {
                        window.location.href = 'user.html';
                    }
                });
            } else if (responseData.message === "房间号不匹配") {
                alert('房间号不匹配，请检查你的房间号.');
            } else if (responseData.message === "用户名或密码错误") {
                alert('用户名或密码错误，请重试.');
            } else {
                console.error('未知错误:', responseData.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('登陆失败，请重试.');
        });
});

function fetchRoomInfo(roomId, token) {
    return fetch(`http://localhost:8080/api/rooms/${roomId}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === "200 OK") {
                localStorage.setItem('roomInfo', JSON.stringify(data.data));
            } else {
                alert(data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("An error occurred while fetching room information.");
        });
}
