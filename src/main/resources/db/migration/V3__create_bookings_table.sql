CREATE TABLE booking(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    workout_id BIGINT NOT NULL,

    booking_status VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_booking_workout
        FOREIGN KEY (workout_id)
        REFERENCES workouts(id)
);