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
	image_path VARCHAR(255) NOT NULL,
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

-- Создание таблицы "video_views"
CREATE TABLE video_views (
    view_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    video_id BIGINT REFERENCES video(video_id) NOT NULL,
    user_id BIGINT REFERENCES "user"(user_id) NOT NULL,
    viewed_at TIMESTAMPTZ DEFAULT now(),
    ip_address INET NOT NULL
);

-- Индексы для ускорения выборок
CREATE INDEX idx_video_views_video ON video_views(video_id);
CREATE INDEX idx_video_views_user ON video_views(user_id);
CREATE INDEX idx_video_views_combined ON video_views(video_id, user_id);
CREATE INDEX idx_video_views_viewed_at ON video_views(viewed_at);

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


INSERT INTO mark_type (name) VALUES 
('LIKE'),
('DISLIKE');

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

INSERT INTO "user" (login, password, surname, name, patronymic, email, telephone, image_path) VALUES
('test','2a$10$Q6f8rbUq3iDX1FevUmE5Mef5K6z7.rTJTXkQUzysZG7nqGgH1p5Oa','Тестов','Тест','Тестович','test@yandex.ru','+7-(000)-000-00-00','.\uploads\photo\4ce3851a-a569-4fec-8835-652f2c8d09ed_Снимок экрана_20250424_115816.png');
select * from "user";
