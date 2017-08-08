create DATABASE deliverable_db;

--read only access
create user 'deliv_read'@'localhost' identified by 'password';
grant select on deliverable_db.* to 'deliv_read'@'%' identified by 'password';
flush privileges;

--write access
create user 'deliv_write'@'localhost' identified by 'password';
grant select, delete, insert, update on deliverable_db.* to 'deliv_write'@'%' identified by 'password';
flush privileges;



--Create TICKETTYPE table
create table TICKETTYPE (TICKETTYPE_ID int not null auto_increment, NAME varchar(30) not null, PRIMARY KEY (TICKETTYPE_ID));

--Create PRIORITY table
create table PRIORITY (PRIORITY_ID int not null auto_increment, NAME varchar(30) not null, WEIGHT int not null, PRIMARY KEY (PRIORITY_ID));

--Create STATUS table
create table STATUS (STATUS_ID int not null auto_increment, NAME varchar(30) not null, PRIMARY KEY (STATUS_ID));

--Create TICKETS table
create table TICKETS (TICKET_ID int not null auto_increment, NAME varchar(64) not null, DESCRIPTION text, TICKETTYPE_ID int not null, PRIORITY_ID int, STATUS_ID int not null, DATE_CREATED timestamp default current_timestamp, PRIMARY KEY (TICKET_ID), FOREIGN KEY (TICKETTYPE_ID) references TICKETTYPE(TICKETTYPE_ID), FOREIGN KEY (PRIORITY_ID) references PRIORITY(PRIORITY_ID), FOREIGN KEY (STATUS_ID) references STATUS(STATUS_ID));

--Create TRANSITION table
create table TRANSITION (TRANSITION_ID int not null auto_increment, NAME varchar(64), TICKETTYPE_ID int not null, ORIGIN_STATUS int not null, DEST_STATUS int not null, PRIMARY KEY (TRANSITION_ID), FOREIGN KEY (TICKETTYPE_ID) references TICKETTYPE(TICKETTYPE_ID), FOREIGN KEY (ORIGIN_STATUS) references STATUS(STATUS_ID), FOREIGN KEY (DEST_STATUS) references STATUS(STATUS_ID));

--Insert TICKETTYPE records
insert into TICKETTYPE set NAME='Feature';
insert into TICKETTYPE set NAME='Bug';

--Insert PRIORITY records
insert into PRIORITY set NAME='High', WEIGHT=250;
insert into PRIORITY set NAME='Medium', WEIGHT=150;
insert into PRIORITY set NAME='Low', WEIGHT=50;
insert into PRIORITY set NAME='None', WEIGHT=0;

--Insert STATUS records
insert into STATUS set NAME='Open';
insert into STATUS set NAME='In Analysis';
insert into STATUS set NAME='In Development';
insert into STATUS set NAME='In QA';
insert into STATUS set NAME='Closed';
insert into STATUS SET NAME='In Code Review';

--Insert TRANSITION records
insert into TRANSITION set NAME='Move to Analysis', TICKETTYPE_ID=1, ORIGIN_STATUS=1, DEST_STATUS=2;
insert into TRANSITION set NAME='Move to Development', TICKETTYPE_ID=1, ORIGIN_STATUS=2, DEST_STATUS=3;
insert into TRANSITION set NAME='Ready for QA', TICKETTYPE_ID=1, ORIGIN_STATUS=3, DEST_STATUS=4;
insert into TRANSITION set NAME='Close', TICKETTYPE_ID=1, ORIGIN_STATUS=4, DEST_STATUS=5;
insert into TRANSITION set NAME='Reopen', TICKETTYPE_ID=1, ORIGIN_STATUS=5, DEST_STATUS=1;
insert into TRANSITION set NAME='Move to Code Review', TICKETTYPE_ID=1, ORIGIN_STATUS=3, DEST_STATUS=6;
insert into TRANSITION set NAME='Move to Development', TICKETTYPE_ID=1, ORIGIN_STATUS=6, DEST_STATUS=3;

