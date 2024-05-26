let initialRoomData;
let debounceTimer;

window.onload = function() {
    const roomId = localStorage.getItem('roomId');
    const username = localStorage.getItem('username');
    const token = localStorage.getItem('token');

    if (roomId && username && token) {
        document.getElementById('roomId').innerText = roomId;
        document.getElementById('username').innerText = username;
        initialRoomData = JSON.parse(localStorage.getItem('roomInfo'));
        displayRoomInfo(initialRoomData);
        setInterval(() => fetchRoomInfo(roomId, token), 10000);  // 10秒刷新一次
    }

    const temperatureInput = document.getElementById('temperatureInput');
    temperatureInput.addEventListener('input', function() {
        document.getElementById('targetTemperatureDisplay').innerText = this.value;
    });

    const fanSpeedButtons = document.querySelectorAll('.fan-speed-button');
    fanSpeedButtons.forEach(button => {
        button.addEventListener('click', function() {
            fanSpeedButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
        });
    });

    document.getElementById('submitButton').addEventListener('click', debounceSubmitSettings);
    document.getElementById('resetButton').addEventListener('click', resetSettings);
    document.getElementById('powerButton').addEventListener('click', togglePower);
};

function debounceSubmitSettings() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(submitSettings, 1000);
}

function fetchRoomInfo(roomId, token) {
    fetch(`http://localhost:8080/api/rooms/${roomId}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json().then(data => ({ status: response.status, data })))
        .then(({ status, data }) => {
            if (status === 200) {
                displayRoomInfo(data.data);
            } else {
                alert(data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("An error occurred while fetching room information.");
        });
}

function displayRoomInfo(roomData) {
    const fanSpeedMap = {
        'low': '低',
        'medium': '中',
        'high': '高'
    };
    const modeMap = {
        'cooling': '制冷',
        'heating': '制热'
    };
    const statusMap = {
        'on': '开',
        'off': '关',
        'standby': '待机'
    };

    document.getElementById('currentInfo').innerHTML = `
        <p>当前温度: ${roomData.currentTemperature} °C</p>
        <p>当前目标温度: ${roomData.targetTemperature} °C</p>
        <p>风速: ${fanSpeedMap[roomData.fanSpeed]}</p>
        <p>模式: ${modeMap[roomData.mode]}</p>
        <p>当前状态: ${statusMap[roomData.status]}</p>
        <p>服务状态: ${roomData.serviceStatus}</p>
    `;
    document.getElementById('energyConsumed').innerText = roomData.energyConsumed;
    document.getElementById('costAccumulated').innerText = roomData.costAccumulated;
    document.getElementById('temperatureInput').value = roomData.targetTemperature;
    document.getElementById('targetTemperatureDisplay').innerText = roomData.targetTemperature;
    document.querySelectorAll('.fan-speed-button').forEach(button => {
        button.classList.remove('active');
        if (button.getAttribute('data-speed') === roomData.fanSpeed) {
            button.classList.add('active');
        }
    });

    const powerButton = document.getElementById('powerButton');
    if (roomData.status === 'off') {
        powerButton.innerText = '开机';
    } else if (roomData.status === 'on' || roomData.status === 'standby') {
        powerButton.innerText = '关机';
    } else {
        powerButton.style.display = 'none';
    }
}

function submitSettings() {
    const targetTemperature = document.getElementById('temperatureInput').value;
    let fanSpeedChinese = initialRoomData.fanSpeed; // 默认使用初始房间信息中的风速
    const activeFanSpeedButton = document.querySelector('.fan-speed-button.active');

    if (activeFanSpeedButton) {
        fanSpeedChinese = activeFanSpeedButton.getAttribute('data-speed'); // 如果有点击按钮设置风速，使用点击的风速
    }

    const fanSpeedMap = {
        '低': 'low',
        '中': 'medium',
        '高': 'high'
    };
    let fanSpeed = fanSpeedMap[fanSpeedChinese];

    // 检测风速是否为合法值，如果不是，则使用初始房间信息中的风速
    if (!['low', 'medium', 'high'].includes(fanSpeed)) {
        fanSpeed = initialRoomData.fanSpeed;
    }

    const roomId = localStorage.getItem('roomId');
    const token = localStorage.getItem('token');

    if (targetTemperature == initialRoomData.targetTemperature && fanSpeed === initialRoomData.fanSpeed) {
        alert("没有检测到设置的改变！");
        return;
    }

    const url = `http://localhost:8080/api/unit/requests?targetTemperature=${targetTemperature}&fanSpeed=${fanSpeed}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json().then(data => ({ status: response.status, data })))
        .then(({ status, data }) => {
            if (status === 200) {
                alert('设置已提交');
                fetchRoomInfo(roomId, token);
            } else {
                alert(data.message || '提交设置失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("An error occurred while submitting settings.");
        });
}

function resetSettings() {
    if (initialRoomData) {
        displayRoomInfo(initialRoomData);
    }
}

function togglePower() {
    const roomId = localStorage.getItem('roomId');
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const password = localStorage.getItem('password');
    const powerButton = document.getElementById('powerButton');

    // 弹出窗口输入账号和密码
    const enteredUsername = prompt("请输入您的用户名：");
    const enteredPassword = prompt("请输入您的密码：");

    if (enteredUsername && enteredPassword) {
        const url = `http://localhost:8080/api/users/login`;

        const requestBody = {
            username: enteredUsername,
            password: enteredPassword,
            roomId: roomId
        };

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        })
            .then(response => response.json())
            .then(responseData => {
                console.log('Login response:', responseData);
                if (responseData.message === "登录成功") {
                    // 认证成功，继续进行开机操作
                    powerToggle(roomId, token, responseData.data.token);
                } else {
                    alert('认证失败，请检查用户名和密码.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('认证失败，请重试.');
            });
    } else {
        alert("请输入用户名和密码！");
    }
}

function powerToggle(roomId, token, authToken) {
    const powerButton = document.getElementById('powerButton');
    if (powerButton.innerText === '开机') {
        const url = `http://localhost:8080/api/unit/${roomId}/authen`;

        const requestBody = {
            username: localStorage.getItem('username'),
            password: localStorage.getItem('password')
        };

        fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        })
            .then(response => response.json().then(data => ({ status: response.status, data })))
            .then(({ status, data }) => {
                if (status === 200) {
                    alert(data.message);  // 从控机开启成功
                    fetchRoomInfo(roomId, token);
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message || "An error occurred while toggling power.");
            });
    } else {
        const url = `http://localhost:8080/api/rooms/${roomId}/stop`;

        fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        })
            .then(response => response.json().then(data => ({ status: response.status, data })))
            .then(({ status, data }) => {
                if (status === 200) {
                    alert('从控机已关闭');
                    displayRoomInfo(data.data);
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message || "An error occurred while toggling power.");
            });
    }
}

