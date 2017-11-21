drop database deliverable_db;

create database deliverable_db;
use deliverable_db;

-- read only access
create user 'deliv_read'@'localhost' identified by 'password';
grant select on deliverable_db.* to 'deliv_read'@'%' identified by 'password';
flush privileges;

-- write access
create user 'deliv_write'@'localhost' identified by 'password';
grant select, delete, insert, update on deliverable_db.* to 'deliv_write'@'%' identified by 'password';
flush privileges;

-- create tickettype table
create table tickettype (tickettype_id int not null auto_increment, name varchar(30) not null, primary key (tickettype_id));

-- create priority table
create table priority (priority_id int not null auto_increment, name varchar(30) not null, weight int not null, primary key (priority_id));

-- create status table
create table status (status_id int not null auto_increment, name varchar(30) not null, primary key (status_id));

-- create users table
create table users (user_id int not null auto_increment, username varchar(50) not null, password varchar(60) null default null, enabled tinyint(1) not null, primary key (user_id));

-- create authorities table
create table authorities (user_id int not null, authority varchar(50) not null, foreign key (user_id) references users(user_id));

-- create tickets table
create table tickets (
  ticket_id int not null auto_increment,
  name varchar(64) not null,
  assignee_id int,
  description text,
  tickettype_id int not null,
  priority_id int,
  status_id int not null,
  date_created timestamp default current_timestamp,
  primary key (ticket_id),
  foreign key (assignee_id)
  references users(user_id),
  foreign key (tickettype_id) references tickettype(tickettype_id),
  foreign key (priority_id) references priority(priority_id),
  foreign key (status_id) references status(status_id));

-- create transition table
create table transition (transition_id int not null auto_increment, name varchar(64), tickettype_id int not null, origin_status int not null, dest_status int not null, primary key (transition_id), foreign key (tickettype_id) references tickettype(tickettype_id), foreign key (origin_status) references status(status_id), foreign key (dest_status) references status(status_id));

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
