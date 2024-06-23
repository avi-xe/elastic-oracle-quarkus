alter session
set
    "_ORACLE_SCRIPT" = true;

create user admin identified by admin;

grant all privileges to admin;

create table
    test (
        id number generated always as identity (
            start
            with
                1 increment by 1
        ),
        name varchar(32 char),
        description varchar(128 char)
    );

alter table test add (constraint test_pk primary key (id));

begin
  for i in 1..1000000 loop
    insert into test(name, description)
    values (dbms_random.string('x', 8), dbms_random.string('x', 32));
  end loop;
end;