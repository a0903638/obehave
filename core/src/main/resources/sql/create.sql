-- sql statements to initialize the data structure of the in-memory test database

CREATE TABLE PUBLIC.Subject
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  name VARCHAR2(255) NOT NULL,
  alias VARCHAR2(255),
  color VARCHAR2(8)
);

CREATE TABLE PUBLIC.Observation
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  name VARCHAR2(255) NOT NULL,
  video VARCHAR(255),
  date DATETIME
);

CREATE TABLE PUBLIC.ModifierFactory
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  type VARCHAR2(100) NOT NULL,
  name VARCHAR2(255),
  alias VARCHAR2(255),
  rangeFrom INT,
  rangeTo INT
);

CREATE TABLE PUBLIC.Action
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  name VARCHAR2(255) NOT NULL,
  alias VARCHAR2(255),
  recurring INT,
  modifierFactory INT,
  type VARCHAR2(255),
  FOREIGN KEY (modifierFactory) REFERENCES Public.ModifierFactory(id)
);

CREATE TABLE PUBLIC.ValidSubject
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  subject INT NOT NULL,
  modifierFactory INT NOT NULL,
  FOREIGN KEY (subject) REFERENCES Public.Subject(id),
  FOREIGN KEY (modifierFactory) REFERENCES Public.ModifierFactory(id)
);

CREATE TABLE PUBLIC.EnumerationItem
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  value VARCHAR2(255) NOT NULL,
  modifierFactory INT NOT NULL,
  FOREIGN KEY (modifierFactory) REFERENCES Public.ModifierFactory(id)
);

CREATE TABLE PUBLIC.Modifier
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  modifierFactory INT NOT NULL,
  type VARCHAR2(255) NOT NULL,
  subject INT,
  enumerationValue VARCHAR2(255),
  number INT,
  FOREIGN KEY (subject) REFERENCES Public.Subject(id),
  FOREIGN KEY (modifierFactory) REFERENCES Public.ModifierFactory(id)
);

CREATE TABLE PUBLIC.Coding
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  subject INT,
  action INT,
  modifier INT,
  observation INT,
  start INT NOT NULL,
  end INT,
  FOREIGN KEY (subject) REFERENCES Public.Subject(id),
  FOREIGN KEY (action) REFERENCES Public.Action(id),
  FOREIGN KEY (modifier) REFERENCES Public.Modifier(id),
  FOREIGN KEY (observation) REFERENCES Public.Observation(id)
);

CREATE TABLE PUBLIC.Node
(
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  modified TIMESTAMP NOT NULL,
  parent INT,
  title VARCHAR2(255),
  type VARCHAR2(100) NOT NULL,
  subject INT,
  action INT,
  actionType VARCHAR2(100),
  modifierFactory INT,
  observation INT,
  FOREIGN KEY (parent) REFERENCES (id),
  FOREIGN KEY (subject) REFERENCES Public.Subject(id),
  FOREIGN KEY (action) REFERENCES Public.Action(id),
  FOREIGN KEY (modifierFactory) REFERENCES Public.ModifierFactory(id),
  FOREIGN KEY (observation) REFERENCES Public.Observation(id)
);