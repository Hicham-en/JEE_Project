CREATE INDEX idx_users_login ON users(login);
CREATE INDEX idx_task_dataset_id ON task(dataset_id);
CREATE INDEX idx_task_annotator_id ON task(annotator_id);
CREATE INDEX idx_annotation_task_id ON annotation(task_id);
CREATE INDEX idx_annotation_annotator_id ON annotation(annotator_id);
CREATE INDEX idx_textpair_dataset_id ON text_pair(dataset_id);
