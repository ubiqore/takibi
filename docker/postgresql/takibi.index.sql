

ALTER TABLE valueset ADD CONSTRAINT fpk_valueset_project FOREIGN KEY (valueset_project_id)  REFERENCES project (project_id);



ALTER TABLE profile ADD CONSTRAINT fpk_profile_project FOREIGN KEY (profile_project_id)  REFERENCES project (project_id);
