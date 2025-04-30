-- Создание таблицы для оценок
CREATE TABLE "mark_type" (
    mark_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Создание таблицы "user"
CREATE TABLE "user" (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    telephone VARCHAR(255) NOT NULL CHECK (telephone ~ '^\+\d{1,2}-\(\d{3}\)-\d{3}-\d{2}-\d{2}$'),
    image_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Создание таблицы "video"
CREATE TABLE video (
    video_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author_id BIGINT REFERENCES "user"(user_id),
    video_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_video_author_id ON video(author_id);

-- Создание таблицы "class"
CREATE TABLE "class" (
    class_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_name VARCHAR(50) NOT NULL
);

-- Создание таблицы "role"
CREATE TABLE "role" (
    role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

-- Создание таблицы "playlist"
CREATE TABLE playlist (
    playlist_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    user_id BIGINT REFERENCES "user"(user_id),
    video_id BIGINT REFERENCES "video"(video_id),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_playlist_user_id ON playlist(user_id);

-- Создание таблицы "comment"
CREATE TABLE comment (
    comment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES "user"(user_id),
    video_id BIGINT REFERENCES "video"(video_id),
    text TEXT NOT NULL,
    parent_id BIGINT,
    is_modified BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_comment_user_id ON comment(user_id);
CREATE INDEX idx_comment_video_id ON comment(video_id);

-- Создание таблицы "user_comment_mark"
CREATE TABLE user_comment_mark (
    user_id BIGINT,
    comment_id BIGINT,
    mark BIGINT REFERENCES "mark_type" NOT NULL,
    PRIMARY KEY (user_id, comment_id)
);
CREATE INDEX idx_user_comment_mark_user_id ON User_comment_mark(user_id);

-- Создание таблицы "user_video_mark"
CREATE TABLE user_video_mark (
    user_id BIGINT,
    video_id BIGINT,
    mark BIGINT REFERENCES "mark_type" NOT NULL,
    PRIMARY KEY (user_id, video_id)
);
CREATE INDEX idx_user_video_mark_user_id ON user_video_mark(user_id);

-- Создание таблицы "role_assignment"
CREATE TABLE role_assignment (
    receiver_id BIGINT,
    role_id BIGINT,
    assigned_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (receiver_id, role_id),
    FOREIGN KEY (receiver_id) REFERENCES "user"(user_id),
    FOREIGN KEY (role_id) REFERENCES "role"(role_id)
);
CREATE INDEX idx_role_assignment_receiver_id ON role_assignment(receiver_id);

-- Создание таблицы "video_class"
CREATE TABLE video_class (
    video_id BIGINT,
    class_id BIGINT,
    PRIMARY KEY (video_id, class_id),
    FOREIGN KEY (video_id) REFERENCES video(video_id),
    FOREIGN KEY (class_id) REFERENCES "class"(class_id)
);
CREATE INDEX idx_video_class_video_id ON video_class(video_id);

-- Создание таблицы "history"
CREATE TABLE history (
    video_id BIGINT,
    user_id BIGINT,
	viewed_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (video_id, user_id),
    FOREIGN KEY (video_id) REFERENCES "video"(video_id),
    FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);
CREATE INDEX idx_history_user_id ON history(user_id);

-- Создание таблицы "subscriptions"
CREATE TABLE subscriptions (
    subscriber_id BIGINT,
    author_id BIGINT,
	subscribed_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (subscriber_id, author_id),
    FOREIGN KEY (subscriber_id) REFERENCES "user"(user_id),
    FOREIGN KEY (author_id) REFERENCES "user"(user_id)
);
CREATE INDEX idx_subscriptions_subscriber_id ON Subscriptions(subscriber_id);


INSERT INTO role (role_name) VALUES 
('USER'),
('EXPERT'),
('AUTHOR'),
('ADMIN');

INSERT INTO "class"	(class_name) VALUES 
('программирование'),
('образование'),
('развлечение'),
('рыбалка'),
('строительство'),
('хобби');

select * from "user";
