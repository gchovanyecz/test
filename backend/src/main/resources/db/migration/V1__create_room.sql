create table room (
    id       bigint generated always as identity primary key,
    name     varchar(100) not null,
    capacity integer      not null,
    location varchar(100) not null,
    constraint room_name_unique unique (name),
    constraint room_capacity_positive check (capacity > 0)
);
