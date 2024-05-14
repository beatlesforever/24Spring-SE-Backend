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
        room: room // 将房间号包含在请求体数据中
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
        // 存储用户名和令牌为全局变量
        globalrole = data.role;
        globalToken = data.token;
        localStorage.setItem('token', globalToken);
        data.status
        // 根据身份信息重定向到相应页面
        if (data.role === "admin") {
            window.location.href = 'admin.html';
        } else {
            window.location.href = 'user.html';
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
});
