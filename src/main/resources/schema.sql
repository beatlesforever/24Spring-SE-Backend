-- 创建 central_unit 表
CREATE TABLE IF NOT EXISTS central_unit (
                                            unit_id INT AUTO_INCREMENT PRIMARY KEY,
                                            mode VARCHAR(255),
    default_temperature FLOAT,
    current_temperature FLOAT,
    min_temperature FLOAT,
    max_temperature FLOAT,
    status VARCHAR(255),
    capacity INT,
    active_units INT,
    frequency INT
    );

-- 创建 room 表
CREATE TABLE IF NOT EXISTS room (
                                    room_id INT AUTO_INCREMENT PRIMARY KEY,
                                    current_temperature FLOAT,
                                    target_temperature FLOAT,
                                    fan_speed VARCHAR(255),
    temperature_threshold FLOAT,
    status VARCHAR(255),
    mode VARCHAR(255),
    last_update TIMESTAMP,
    service_status VARCHAR(255),
    energy_consumed FLOAT,
    cost_accumulated FLOAT,
    unit_id INT,
    FOREIGN KEY (unit_id) REFERENCES central_unit(unit_id)
    );

-- 创建 control_log 表
CREATE TABLE IF NOT EXISTS control_log (
                                           log_id INT AUTO_INCREMENT PRIMARY KEY,
                                           room_id INT,
                                           requested_temp FLOAT,
                                           actual_temp FLOAT,
                                           end_temp FLOAT,
                                           requested_fan_speed VARCHAR(255),
    mode VARCHAR(255),
    request_time TIMESTAMP,
    end_time TIMESTAMP,
    duration INT,
    is_completed BOOLEAN,
    FOREIGN KEY (room_id) REFERENCES room(room_id)
    );

-- 创建 report 表
CREATE TABLE IF NOT EXISTS report (
                                      report_id INT AUTO_INCREMENT PRIMARY KEY,
                                      room_id INT,
                                      type VARCHAR(255),
    generation_date TIMESTAMP,
    total_energy_consumed FLOAT,
    total_cost FLOAT,
    usage_time INT,
    creator VARCHAR(255),
    FOREIGN KEY (room_id) REFERENCES room(room_id)
    );

-- 创建 usage_record 表
CREATE TABLE IF NOT EXISTS usage_record (
                                            record_id INT AUTO_INCREMENT PRIMARY KEY,
                                            room_id INT,
                                            start_time TIMESTAMP,
                                            end_time TIMESTAMP,
                                            total_energy_consumed FLOAT,
                                            cost FLOAT,
                                            FOREIGN KEY (room_id) REFERENCES room(room_id)
    );

-- 创建 user 表
CREATE TABLE IF NOT EXISTS user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    room_id INT,
    role VARCHAR(255),
    FOREIGN KEY (room_id) REFERENCES room(room_id)
    );

-- 创建 environment_temperature 表
CREATE TABLE IF NOT EXISTS environment_temperature (
                                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                                       temperature FLOAT,
                                                       timestamp TIMESTAMP
);
