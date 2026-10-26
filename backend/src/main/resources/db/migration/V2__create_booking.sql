-- btree_gist lets a GiST exclusion constraint combine equality on room_id with range overlap.
create extension if not exists btree_gist;

create table booking (
    id         bigint generated always as identity primary key,
    room_id    bigint       not null references room (id),
    title      varchar(100),
    start_time timestamptz  not null,
    end_time   timestamptz  not null,
    created_at timestamptz  not null,
    constraint booking_time_range_valid check (start_time < end_time),
    -- Final guarantee against overlapping bookings of the same room, even for concurrent requests.
    -- '[)' makes the range half-open, so a booking ending at 10:00 does not overlap one starting at 10:00.
    constraint booking_no_overlap exclude using gist (
        room_id with =,
        tstzrange(start_time, end_time, '[)') with &&
    )
);

create index booking_room_time_idx on booking (room_id, start_time, end_time);
