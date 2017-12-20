-- BCrypt calculated value of 'password': $2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6
insert into users (user_id, username, password, enabled) values (1, 'user1', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into users (user_id, username, password, enabled) values (2, 'admin', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into roles (role_id, user_id, role_name) values (1, 2, 'ROLE_ADMIN');
insert into roles (role_id, user_id, role_name) values (2, 7, 'ROLE_CHARLIES_ANGLES');