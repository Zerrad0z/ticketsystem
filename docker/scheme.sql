-- Create sequences
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE ticket_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE comment_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE audit_log_seq START WITH 1 INCREMENT BY 1;

-- Create users table
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    role VARCHAR2(20) NOT NULL
);

-- Create tickets table
CREATE TABLE ticket (
    id NUMBER PRIMARY KEY,
    title VARCHAR2(200) NOT NULL,
    description VARCHAR2(1000),
    priority VARCHAR2(20) NOT NULL,
    category VARCHAR2(20) NOT NULL,
    status VARCHAR2(20) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    created_by NUMBER REFERENCES users(id)
);

-- Create comments table
CREATE TABLE comment (
    id NUMBER PRIMARY KEY,
    content VARCHAR2(1000) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    created_by NUMBER REFERENCES users(id),
    ticket_id NUMBER REFERENCES ticket(id)
);

-- Create audit_log table
CREATE TABLE audit_log (
    id NUMBER PRIMARY KEY,
    action VARCHAR2(100) NOT NULL,
    old_value VARCHAR2(100),
    new_value VARCHAR2(100),
    timestamp TIMESTAMP NOT NULL,
    performed_by NUMBER REFERENCES users(id),
    ticket_id NUMBER REFERENCES ticket(id)
);

-- Create indexes
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_created_by ON ticket(created_by);
CREATE INDEX idx_comment_ticket ON comment(ticket_id);
CREATE INDEX idx_audit_ticket ON audit_log(ticket_id);