INSERT INTO central_unit (unit_id, status, mode, default_temperature) VALUES (1, 'off', 'cooling', 22.0);
INSERT INTO room (current_temperature, target_temperature, fan_speed, temperature_threshold, status, mode, last_update, service_status, energy_consumed, cost_accumulated, unit_id)
VALUES
    (24.5, 25.0, 'medium', 1.0, 'off', 'cooling', CURRENT_TIMESTAMP, 'waiting', 0.0, 0.0, 1),
    (22.0, 22.5, 'low', 1.0, 'off', 'heating', CURRENT_TIMESTAMP, 'waiting', 0.0, 0.0, 1),
    (23.0, 23.5, 'high', 1.0, 'off', 'cooling', CURRENT_TIMESTAMP, 'waiting', 0.0, 0.0, 1),
    (20.0, 21.0, 'low', 1.0, 'off', 'heating', CURRENT_TIMESTAMP, 'waiting', 0.0, 0.0, 1),
    (25.0, 24.5, 'high', 1.0, 'off', 'cooling', CURRENT_TIMESTAMP, 'waiting', 0.0, 0.0, 1);
INSERT INTO user (username, password, room_id,role) VALUES ('admin', '$2a$10$lS3qyKc9SzAx.MUJqvDG5O5uGV3UCcmgSIqsly7a5mN6ep37mEyDO', '1' , 'admin');
