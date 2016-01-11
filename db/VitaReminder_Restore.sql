DROP TABLE IF EXISTS regimens CASCADE; 
DROP TABLE IF EXISTS supplements CASCADE;  

CREATE TABLE regimens 
(
  regimen_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  regimen_name VARCHAR(60) NOT NULL,
  regimen_notes VARCHAR(1024) DEFAULT NULL,
  CONSTRAINT regimens_pk PRIMARY KEY (regimen_id)
);



CREATE TABLE supplements 
(
  supp_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  regimen_id INTEGER NOT NULL,
  supp_name VARCHAR(60) NOT NULL,
  supp_amount DOUBLE DEFAULT NULL,
  supp_units VARCHAR(30) DEFAULT NULL,
  supp_time TIME DEFAULT NULL,
  supp_email_enabled BOOLEAN NOT NULL,
  supp_text_enabled BOOLEAN NOT NULL,
  supp_voice_enabled BOOLEAN NOT NULL,
  supp_notes VARCHAR(1024) DEFAULT NULL,
  CONSTRAINT supp_id_pk PRIMARY KEY (supp_id),
  CONSTRAINT regimen_id_fk FOREIGN KEY (regimen_id) REFERENCES regimens(regimen_id)
      ON UPDATE RESTRICT
      ON DELETE CASCADE
);



INSERT INTO regimens (regimen_name, regimen_notes) VALUES
('My Current Regimen', 'Vitamins and minerals for optimum health.'),
('My Exercise Regimen', 'Basic vitamins and minerals along with amino acids for more energy.'),
('Grandpa''s Regimen', 'Grandpa''s daily vitamins and medications.');



INSERT INTO supplements (regimen_id, supp_name, supp_amount, supp_units, supp_time, supp_email_enabled, supp_text_enabled, supp_voice_enabled, supp_notes) VALUES
(1, 'Multivitamin Mineral', 1, 'capsule', '07:30:00', 0, 0, 0, 'Can be taken with or without food.'),
(1, 'Vitamin C', 1000.00, 'mg', '07:45:00', 0, 0, 0, 'Take with a meal.'),
(2, 'Multivitamin Mineral', 1, 'capsule', '07:30:00', 0, 0, 0, 'Can be taken with or without food.'),
(2, 'Vitamin C', 1000.00, 'mg', '07:45:00', 0, 0, 0, 'Take with a meal.'),
(2, 'Amino acids', 2.00, 'tsp', '07:45:00', 0, 0, 0, 'Take on an empty stomach.'),
(3, 'Prescription Multivitamin', 2, 'capsules', '09:00:00', 0, 0, 0, 'Doctor prescribed multivitamins.'),
(3, 'Heart medication', 1, 'tablet', '09:00:00', 0, 0, 0, 'Doctor prescribed heart medication.');
