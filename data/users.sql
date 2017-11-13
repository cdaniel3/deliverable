-- BCrypt calculated value of 'password' using 12 rounds: $2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6
insert into users (username, password, enabled) values ('user1', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into authorities (user_id, authority) values (1, 'role_user');
