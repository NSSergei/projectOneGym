CREATE TABLE workouts(
   id BIGSERIAL PRIMARY KEY,
   workout_name VARCHAR(255),
   description VARCHAR(255),
   price INT NOT NULL DEFAULT 500
);