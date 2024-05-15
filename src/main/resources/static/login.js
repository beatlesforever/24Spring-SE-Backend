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
    .then(responseData => {
        // 这里使用 responseData.data 来访问返回的数据
        console.log('Login response:', responseData);
        if (responseData.message === "登录成功") {
            globalrole = responseData.data.role;
            globalToken = responseData.data.token;
            console.log('Token:', globalToken);
            localStorage.setItem('token', globalToken);
            localStorage.setItem('roomId', room);
            if (responseData.data.role === "admin") {
                window.location.href = 'admin.html';
            } else {
                window.location.href = 'user.html';
            }
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
