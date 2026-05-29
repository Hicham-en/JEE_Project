CREATE TABLE task_text_pairs (
    task_id BIGINT NOT NULL,
    text_pair_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, text_pair_id),
    CONSTRAINT fk_task_text_pairs_task FOREIGN KEY (task_id) REFERENCES task(id),
    CONSTRAINT fk_task_text_pairs_text_pair FOREIGN KEY (text_pair_id) REFERENCES text_pair(id)
);
