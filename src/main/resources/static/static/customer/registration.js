document.getElementById("registerForm").addEventListener("submit", function(event) {
    event.preventDefault();
    // 获取表单数据
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var roomId = document.getElementById("roomId").value;

    // 构建请求体数据
    var data = {
        username: username,
        password: password,
        roomId: roomId
    };

    // 发送 POST 请求到后端
    fetch('http://localhost:8080/api/users/register', {
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
                window.location.href = 'registration.html';
                // 注册成功消息
                alert("注册成功");
            } else {
                // 注册失败消息
                alert("注册失败，用户名可能已存在");
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
});
