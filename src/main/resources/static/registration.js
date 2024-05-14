document.getElementById("registerForm").addEventListener("submit", function(event) {
   event.preventDefault();
   // 获取表单数据
   var username = document.getElementById("username").value;
   var password = document.getElementById("password").value;
   var roomId = document.getElementById("roomId").value;
   var role = document.getElementById("role").value;
   
   // 构建请求体数据
   var data = {
       username: username,
       password: password,
       roomId: roomId,
       role: role
   };
   
   // 发送 POST 请求到后端
   fetch('/api/users/register', {
       method: 'POST',
       headers: {
           'Content-Type': 'application/json'
       },
       body: JSON.stringify(data)
   })
   .then(response => response.json())
   .then(data => {
       console.log('Registration response:', data);
       window.location.href = 'registration.html';
       // 在这里处理注册响应，例如显示成功消息或重定向到登录页面
   })
   .catch(error => {
       console.error('Error:', error);
   });
});