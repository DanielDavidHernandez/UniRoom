CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institutional_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE study_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    location VARCHAR(120) NOT NULL,
    capacity INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    CONSTRAINT fk_time_slot_room
        FOREIGN KEY (room_id)
        REFERENCES study_rooms(id)
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,

    CONSTRAINT fk_reservation_student
        FOREIGN KEY (student_id)
        REFERENCES students(id),

    CONSTRAINT fk_reservation_time_slot
        FOREIGN KEY (time_slot_id)
        REFERENCES time_slots(id)
);

CREATE TABLE waitlist_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    position INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_waitlist_student
        FOREIGN KEY (student_id)
        REFERENCES students(id),

    CONSTRAINT fk_waitlist_time_slot
        FOREIGN KEY (time_slot_id)
        REFERENCES time_slots(id)
);