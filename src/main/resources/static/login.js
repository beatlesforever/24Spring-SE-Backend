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
    fetch('/api/users/login', {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Login response:', data);
        // 根据后端返回的信息处理登录结果
        if (data.message === "登录成功") {
            // 存储用户名和令牌为全局变量
            globalrole = data.role;
            globalToken = data.token;
            localStorage.setItem('token', globalToken);
            if (data.role === "admin") {
                window.location.href = 'admin.html';
            } else {
                window.location.href = 'user.html';
            }
        } else if (data.message === "房间号不匹配") {
            alert('Room number does not match. Please check your room number.');
        } else if (data.message === "用户名或密码错误") {
            alert('Invalid username or password. Please try again.');
        } else {
            console.error('Unknown message from server:', data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Login failed. Please try again.');
    });
});
