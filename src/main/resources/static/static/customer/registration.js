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
                window.location.href = 'login.html';
                alert("注册成功");
            } else {
                alert("注册失败，用户名可能已存在");
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
});
