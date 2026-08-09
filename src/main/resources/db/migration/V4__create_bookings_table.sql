CREATE TABLE booking(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    training_slot_id BIGINT NOT NULL,

    booking_status VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_booking_training
        FOREIGN KEY (training_slot_id)
        REFERENCES training_slot(id)
);