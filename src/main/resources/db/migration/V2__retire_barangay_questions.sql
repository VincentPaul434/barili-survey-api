alter table questions add column active boolean not null default true;

update questions
set active = false
where code in ('A13', 'C13', 'D13');
