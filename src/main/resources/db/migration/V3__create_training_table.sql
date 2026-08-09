CREATE TABLE training_slot(
    id BIGSERIAL PRIMARY KEY,

    coach_id BIGINT NOT NULL,
    workout_id BIGINT NOT NULL,

    start_time TIME,
    end_time TIME,
    capacity INT NOT NULL,

    CONSTRAINT fk_training_coach
        FOREIGN KEY (coach_id)
        REFERENCES users(id),

    CONSTRAINT fk_training_workout
        FOREIGN KEY (workout_id)
        REFERENCES workouts(id)
)