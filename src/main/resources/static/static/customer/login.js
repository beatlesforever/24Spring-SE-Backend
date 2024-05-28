document.getElementById("customerLoginForm").addEventListener("submit", function(event) {
    event.preventDefault();
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var room = document.getElementById("room").value;

    var data = {
        username: username,
        password: password,
        roomId: room
    };

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
                window.location.href = 'user.html';
            } else if (responseData.message === "房间号不匹配") {
                $('#roomMismatchModal').modal('show');
            } else if (responseData.message === "用户名或身份证号错误") {
                $('#credentialsErrorModal').modal('show');
            } else {
                console.error('未知错误:', responseData.message);
                $('#loginErrorModal').modal('show');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            $('#loginErrorModal').modal('show');
        });
});
