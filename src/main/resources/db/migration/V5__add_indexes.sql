CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_booking_training_id
ON booking(training_slot_id);

CREATE INDEX idx_booking_user_id
ON booking(user_id);

CREATE INDEX idx_training_workout_id
ON training_slot(workout_id);

CREATE INDEX idx_training_coach_id
ON training_slot(coach_id);