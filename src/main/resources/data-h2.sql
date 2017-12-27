
-- insert tickettype records
insert into tickettype set name='feature';
insert into tickettype set name='bug';

-- insert priority records
insert into priority set name='high', weight=250;
insert into priority set name='medium', weight=150;
insert into priority set name='low', weight=50;
insert into priority set name='none', weight=0;

-- insert status records
insert into status set name='open';
insert into status set name='in analysis';
insert into status set name='in development';
insert into status set name='in qa';
insert into status set name='closed';
insert into status set name='in code review';

-- insert transition records
insert into transition set name='move to analysis', tickettype_id=1, origin_status=1, dest_status=2;
insert into transition set name='move to development', tickettype_id=1, origin_status=2, dest_status=3;
insert into transition set name='ready for qa', tickettype_id=1, origin_status=3, dest_status=4;
insert into transition set name='close', tickettype_id=1, origin_status=4, dest_status=5;
insert into transition set name='reopen', tickettype_id=1, origin_status=5, dest_status=1;
insert into transition set name='move to code review', tickettype_id=1, origin_status=3, dest_status=6;
insert into transition set name='move to development', tickettype_id=1, origin_status=6, dest_status=3;
insert into transition set name='move to development', tickettype_id=1, origin_status=4, dest_status=3;
insert into transition set name='move to analysis', tickettype_id=2, origin_status=1, dest_status=2;
insert into transition set name='move to development', tickettype_id=2, origin_status=2, dest_status=3;
insert into transition set name='ready for qa', tickettype_id=2, origin_status=3, dest_status=4;
insert into transition set name='close', tickettype_id=2, origin_status=4, dest_status=5;
insert into transition set name='reopen', tickettype_id=2, origin_status=5, dest_status=1;
insert into transition set name='move to code review', tickettype_id=2, origin_status=3, dest_status=6;
insert into transition set name='move to development', tickettype_id=2, origin_status=6, dest_status=3;
insert into transition set name='move to development', tickettype_id=2, origin_status=4, dest_status=3;

insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('blah', 'fix something', 2, 1, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('return better json error messages in ticketrestcontroller', 'this is a short description.', 2, 1, 4);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test4', 'normal descr', 2, 1, 5);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test6', '', 2, 3, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('t5', 'normal descr', 2, 2, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test10', 'normal descr', 2, 3, 3);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test12', '', 2, 4, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test14', 'normal descr', 2, 3, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test16', 'normal descr', 2, 1, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test18', '', 1, 1, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test20', 'this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. ', 1, 3, 2);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test22', 'this is a short description.', 1, 1, 5);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test24', 'this is a short description.', 1, 2, 5);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test26', 'this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. this is a long description. testing long descr. ', 1, 4, 5);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test28', 'normal descr', 1, 3, 2);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test30', 'normal descr', 1, 3, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('ticket17', 'normal descr', 1, 3, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('test11', 'description here..', 2, 4, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('fix broken stuff', ' descr', 2, 2, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('improve login feature', 'the login feature should be improved', 1, 1, 1);
insert into tickets (name, description, tickettype_id, priority_id, status_id) values ('fix dropdown issues', 'descr', 2, 3, 1);

-- BCrypt calculated value of 'password': $2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6
insert into users (user_id, username, password, enabled) values (1, 'user1', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into users (user_id, username, password, enabled) values (2, 'admin', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into users (user_id, username, password, enabled) values (3, 'alice', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into users (user_id, username, password, enabled) values (4, 'bob', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);
insert into users (user_id, username, password, enabled) values (5, 'charlie', '$2a$12$qsVTPS5pW20FGgiQzl0VQui2CLLXFn0sCO01KHolNOT.qBnyxyTk6', 1);

insert into roles (role_id, user_id, role_name) values (1, 2, 'ROLE_ADMIN');

insert into comments (ticket_id, comment, user_id, comment_timestamp) values (1, 'what\s wrong with the error msgs?', 3, '2017-12-19 10:59:10');
insert into comments (ticket_id, comment, user_id, comment_timestamp) values (1, 'they are not descriptive enough', 4, '2017-12-19 10:59:30');
insert into comments (ticket_id, comment, user_id, comment_timestamp) values (1, 'what\s descriptive enough', 3, '2017-12-19 10:59:40');
insert into comments (ticket_id, comment, user_id) values (19, 'please list all that is broken', 4);
