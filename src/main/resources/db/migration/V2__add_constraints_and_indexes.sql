ALTER TABLE time_slots
ADD CONSTRAINT uk_room_time_slot
UNIQUE (room_id, start_time, end_time);

CREATE INDEX idx_reservations_student
ON reservations(student_id);

CREATE INDEX idx_reservations_time_slot
ON reservations(time_slot_id);

CREATE INDEX idx_waitlist_student
ON waitlist_entries(student_id);

CREATE INDEX idx_waitlist_time_slot
ON waitlist_entries(time_slot_id);

CREATE INDEX idx_time_slots_status
ON time_slots(status);

CREATE INDEX idx_reservations_status
ON reservations(status);