CREATE DATABASE example;

\c example

create table departments (
    id integer primary key,
    department_name text not null,
    parent_department_id integer references departments(id) null
);

create table users (
    id integer primary key,
    email text not null,
    age integer not null,
    department_id integer references departments(id) not null
);

create table target_users (
    id integer primary key,
    user_id integer references users(id) not null
);
