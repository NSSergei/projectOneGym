CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_booking_workout_id
ON booking(workout_id);

CREATE INDEX idx_booking_user_id
ON booking(user_id);