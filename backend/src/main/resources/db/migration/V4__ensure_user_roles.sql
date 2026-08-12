-- Existing installations may have been created before role support was added.
alter table users add column if not exists role varchar(20) not null default 'USER';
update users set role = 'USER' where role is null;
