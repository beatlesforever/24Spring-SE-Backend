-- 创建房间表
CREATE TABLE room (
                      room_id INT PRIMARY KEY,  -- 房间的唯一标识符
                      current_temperature FLOAT,  -- 房间当前的温度
                      target_temperature FLOAT,  -- 房间设定的目标温度
                      min_temperature FLOAT,  -- 温度下限（自动重启温控机制）
                      max_temperature FLOAT,  -- 温度上限（自动重启温控机制）
                      temperature_threshold FLOAT DEFAULT 1.0,  -- 温度变化阈值，用于自动重启
                      status VARCHAR(10) CHECK (status IN ('on', 'off', 'standby')),  -- 房间的当前状态，使用枚举类型限定值
                      mode VARCHAR(10) CHECK (mode IN ('heating', 'cooling')),  -- 当前工作模式，使用枚举类型限定值
                      last_update DATETIME,  -- 最后一次状态更新时间
                      connected BOOLEAN DEFAULT FALSE  -- 表示房间是否与中央空调系统连接
);

-- 创建中央空调单元表
CREATE TABLE central_unit (
                              unit_id INT PRIMARY KEY,  -- 中央空调单元的唯一标识符
                              mode VARCHAR(10) CHECK (mode IN ('heating', 'cooling')),  -- 工作模式，使用枚举类型限定值
                              default_temperature FLOAT,  -- 缺省的工作温度设置
                              status VARCHAR(10) CHECK (status IN ('on', 'off', 'standby')),  -- 中央空调的状态，使用枚举类型限定值
                              capacity INT  -- 同时处理的最大从控机数量
);

-- 创建用户表
CREATE TABLE user(
                     user_id INT PRIMARY KEY,  -- 用户的唯一标识符
                     username VARCHAR(50),  -- 用户名
                     password VARCHAR(255),  -- 加密存储的用户密码
                     room_id INT  -- 关联的房间编号
                         FOREIGN KEY (room_id) REFERENCES Room(room_id)  -- 外键约束，关联到房间表
);

-- 创建温度控制日志表
CREATE TABLE temperature_control_log (
                                         log_id INT PRIMARY KEY,  -- 温度控制日志的唯一标识符
                                         room_id INT,  -- 关联的房间编号
                                         requested_temp FLOAT,  -- 请求的温度
                                         actual_temp FLOAT,  -- 实际温度
                                         request_time DATETIME,  -- 请求时间
                                         response_time DATETIME,  -- 响应时间
                                         action_taken VARCHAR(50),  -- 采取的行动，如 'cooling on', 'heating on', 'standby'
                                         FOREIGN KEY (room_id) REFERENCES Room(room_id)  -- 外键约束，关联到房间表
);

-- 创建使用记录表
CREATE TABLE usage_record (
                              record_id INT PRIMARY KEY,  -- 使用记录的唯一标识符
                              room_id INT,  -- 关联的房间编号
                              start_time DATETIME,  -- 使用开始的时间
                              end_time DATETIME,  -- 使用结束的时间
                              cost DECIMAL(10, 2),  -- 该次使用的计费金额
                              fan_speed VARCHAR(10) CHECK (fan_speed IN ('high', 'medium', 'low')),  -- 风速设置，使用枚举类型限定值
                              temperature_change FLOAT,  -- 使用期间的温度变化
                              FOREIGN KEY (room_id) REFERENCES Room(room_id)  -- 外键约束，关联到房间表
);

-- 创建系统设置表
CREATE TABLE system_setting (
                                setting_id INT PRIMARY KEY,  -- 设置的唯一标识符
                                name VARCHAR(50),  -- 设置项的名称
                                value VARCHAR(255),  -- 设置项的值
                                description TEXT  -- 设置项的详细描述
);


-- 创建能耗记录表
CREATE TABLE energy_consumption (
                                    consumption_id INT PRIMARY KEY,  -- 能耗记录的唯一标识符
                                    room_id INT,  -- 关联的房间编号
                                    datetime DATETIME,  -- 记录时间
                                    energy_used DECIMAL(10, 2),  -- 使用的能量量
                                    cost DECIMAL(10, 2),  -- 对应的费用
                                    FOREIGN KEY (room_id) REFERENCES Room(room_id)  -- 外键约束，关联到房间表
);

-- 创建报告表
CREATE TABLE report (
                        report_id INT PRIMARY KEY,  -- 报告的唯一标识符
                        type VARCHAR(10) CHECK (type IN ('daily', 'weekly', 'monthly')),  -- 报告类型，使用枚举类型限定值
                        generation_date DATETIME,  -- 报告生成日期
                        details TEXT  -- 报告的详细内容，可能包含JSON格式的数据
);

-- 创建温度变化记录表
CREATE TABLE temperature_change_log (
                                        change_id INT PRIMARY KEY,  -- 温度变化记录的唯一标识符
                                        room_id INT,  -- 关联的房间编号
                                        temperature_change FLOAT,  -- 记录的温度变化
                                        change_time DATETIME,  -- 记录的时间点
                                        FOREIGN KEY (room_id) REFERENCES Room(room_id)  -- 外键约束，关联到房间表
);