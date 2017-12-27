-- create tickettype table
create table tickettype (
  tickettype_id int not null auto_increment,
  name varchar(30) not null,
  primary key (tickettype_id));

-- create priority table
create table priority (
  priority_id int not null auto_increment,
  name varchar(30) not null,
  weight int not null,
  primary key (priority_id));

-- create status table
create table status (
  status_id int not null auto_increment,
  name varchar(30) not null,
  primary key (status_id));

-- create users table
create table users (
  user_id int not null auto_increment,
  username varchar(50) not null,
  password varchar(60) null default null,
  enabled tinyint(1) not null,
  primary key (user_id));

-- create roles table
create table roles (
  role_id int not null auto_increment,
  user_id int not null,
  role_name varchar(50) not null,
  primary key (role_id),
  foreign key (user_id) references users(user_id) on delete cascade);

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
  foreign key (assignee_id) references users(user_id),
  foreign key (tickettype_id) references tickettype(tickettype_id),
  foreign key (priority_id) references priority(priority_id),
  foreign key (status_id) references status(status_id));

-- create comments table
create table comments (
  comment_id int not null auto_increment,
  ticket_id int not null,
  comment text not null,
  user_id int not null,
  comment_timestamp timestamp default current_timestamp,
  primary key (comment_id),
  foreign key (ticket_id) references tickets (ticket_id) on delete cascade,
  foreign key (user_id) references users (user_id) on delete cascade);

-- create transition table
create table transition (
  transition_id int not null auto_increment,
  name varchar(64),
  tickettype_id int not null,
  origin_status int not null,
  dest_status int not null,
  primary key (transition_id),
  foreign key (tickettype_id) references tickettype(tickettype_id),
  foreign key (origin_status) references status(status_id),
  foreign key (dest_status) references status(status_id));
