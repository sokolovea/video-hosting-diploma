import { showLoginPrompt } from './authorize_window.js';

export function toggleReplyForm(button) {
    const replyForm = button.closest('.comment-item').querySelector('.reply-form');
    if (replyForm.style.display === 'none' || replyForm.style.display === '') {
        replyForm.style.display = 'block';
        replyForm.style.animation = 'fadeIn 0.3s ease';
    } else {
        replyForm.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Обработчик для лайков/дизлайков
    document.querySelector('.comment-list').addEventListener('submit', async function(event) {
        event.preventDefault();
        const videoId = parseInt(document.querySelector('meta[name="_video_id"]').content, 10);
        const form = event.target;
        if (!form.matches('form')) return;

        try {
            const commentId = form.querySelector('.commentId').value;
            const markId = form.querySelector('.markId').value;

            const formData = new FormData(form);
            const url = `http://localhost:8082/api/video/${videoId}/comment/${commentId}/like?markId=${markId}`;
            console.log(form);
            const method = form.method;
            const commentItem = form.closest('.comment-item');

            console.log(commentItem);
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

            const response = await fetch(url, {
                method: method,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                }
            });

            if (response.status === 401) {
                showLoginPrompt();
                return;
            }
            if (!response.ok) throw new Error('Ошибка сети');

            const data = await response.json();

            // Обновляем счетчики
            commentItem.querySelector('.comment-like-count').innerHTML = data.likes;
            commentItem.querySelector('.comment-dislike-count').innerHTML = data.dislikes;

            // Обновляем кнопки
            const buttons = commentItem.querySelectorAll('[data-mark-id]');
            buttons.forEach(btn => btn.classList.remove('active'));
            // if (data.userMark > 0) {
            //     commentItem.querySelector(`[data-mark-id="${data.userMark}"]`).classList.add('active');
            // }
        } catch (error) {
            console.error('Ошибка:', error);
        }
    });
});




document.addEventListener('DOMContentLoaded', function () {
    document.querySelector('.user-info').addEventListener('submit', async function (event) {
        event.preventDefault();

        const form = event.target;
        const authorId = parseInt(form.querySelector('input[name="author"]').value, 10);
        const actionSubscribeEnum = form.querySelector('input[name="actionSubscribeEnum"]').value;

        const url = '/api/user/subscribe';
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken,
                },
                body: JSON.stringify({
                    author: authorId,
                    actionSubscribeEnum: actionSubscribeEnum,
                }),
            });

            if (response.status === 401) {
                showLoginPrompt();
                return;
            }
            if (!response.ok) throw new Error('Ошибка сети');

            const data = await response.json();

            // Изменение состояния кнопки на клиенте
            const actionField = form.querySelector('input[name="actionSubscribeEnum"]');
            const button = form.querySelector('.subscribe-button');

            if (actionSubscribeEnum === 'SUBSCRIBE') {
                actionField.value = 'UNSUBSCRIBE';
                button.textContent = 'Отписаться';
            } else {
                actionField.value = 'SUBSCRIBE';
                button.textContent = 'Подписаться';
            }
        } catch (error) {
            console.error('Ошибка:', error);
        }
    });
});


function sendPostRequest() {
    const videoId = parseInt(document.querySelector('meta[name="_video_id"]').content, 10);
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:8082/api/video/view', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            console.log('Response:', xhr.responseText);
        } else if (xhr.readyState === 4) {
            console.error('Error:', xhr.statusText);
        }
    };
    const requestBody = JSON.stringify({ videoId: videoId });
    xhr.send(requestBody);
}

setTimeout(sendPostRequest, 10000);

function onVideoMark (markId) {

    const videoId = parseInt(document.querySelector('meta[name="_video_id"]').content, 10);
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

    // Формируем URL с параметрами
    const url = `/api/video/${videoId}/mark?markId=${markId}`;

    // Отправляем AJAX-запрос
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
    })
        .then(response => {
            if (response.status === 401) {
                showLoginPrompt();
            }
            if (response.ok) {
                return response.json(); // Предполагаем, что сервер возвращает JSON
            }
            throw new Error('Network response was not ok');
        })
        .then(data => {
            // console.log(data);
            document.getElementById('likeCount').textContent = data.likesCount;
            document.getElementById('dislikeCount').textContent = data.dislikesCount;
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

document.addEventListener('DOMContentLoaded', function() {
    const forms = document.querySelectorAll('.reply-form, .comment-form');
    forms.forEach(form => {
        form.addEventListener('submit', function (event) {
            event.preventDefault(); // Предотвращаем стандартную отправку формы

            const parentId = form.querySelector('.parentId').value; //form.dataset.parentId || null;
            const commentText = form.querySelector('.reply-input').value;
            const videoId = document.querySelector('meta[name="_video_id"]').content;
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

            let url = `/api/video/${videoId}/comment`;
            if (parentId !== null) {
                url += `?parentId=${parentId}`;
            }

            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken,
                },
                body: JSON.stringify({ commentText }),
            })
                .then(response => {
                    if (response.status === 401) {
                        showLoginPrompt();
                    }
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error(`Ошибка: ${response.status}`);
                    }
                })
                .then(data => {
                    console.log('Комментарий добавлен:', data);
                    // Логика для обновления интерфейса (например, добавление нового комментария в список)
                })
                .catch(error => console.error('Ошибка:', error));
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const commentList = document.querySelector('.comment-list');
    const videoId = parseInt(document.querySelector('meta[name="_video_id"]').content, 10);

    async function fetchCommentTree() {
        try {
            const response = await fetch(`/api/video/${videoId}/comments`);
            if (!response.ok) {
                throw new Error('Ошибка при загрузке комментариев');
            }
            const commentTree = await response.json();
            updateCommentTree(commentTree);
        } catch (error) {
            console.error(error);
        }
    }

    function updateCommentTree(commentTree) {
        const commentHtml = Object.entries(commentTree).map(([parent, replies]) => {
            // console.log(parent);
            const parentComment = JSON.parse(parent);
            return `
                        <div class="comment-item">
                            <div class="comment-header">
                                <img class="user-avatar" src="/${parentComment.user.imagePath}" alt="Avatar">
                                <span class="comment-author">${parentComment.user.username}</span>
                            </div>
                            <div class="comment-body">
                                <p class="comment-text">${parentComment.text}</p>
                                <div class="comment-actions">
                                    <button class="comment-like-button" data-comment-id="${parentComment.commentId}" data-mark="1">👍</button>
                                    <span class="comment-like-count">${parentComment.likesCount}</span>
                                    <button class="comment-like-button" data-comment-id="${parentComment.commentId}" data-mark="2">👎</button>
                                    <span class="comment-dislike-count">${parentComment.dislikesCount}</span>
                                    <button class="reply-button" data-comment-id="${parentComment.commentId}" onclick="toggleReplyForm(this)">Ответить</button>
                                </div>
                            </div>
                            <div class="replies">
                                ${replies.map(reply => `
                                    <div class="comment-item">
                                        <div class="comment-header">
                                            <img class="user-avatar" src="/${reply.user.imagePath}" alt="Avatar">
                                            <span class="comment-author">${reply.user.username}</span>
                                        </div>
                                        <div class="comment-body">
                                            <p class="comment-text">${reply.text}</p>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    `;
        }).join('');

        commentList.innerHTML = commentHtml;
    }

});

document.addEventListener('DOMContentLoaded', () => {
    const likeButton = document.getElementById('likeButton');

    const likeId = parseInt(document.querySelector('meta[name="_like_id"]').content, 10);

    likeButton.addEventListener('click', () => {
        onVideoMark(likeId); // DEBUG
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const likeButton = document.getElementById('dislikeButton');

    const dislikeId = parseInt(document.querySelector('meta[name="_dislike_id"]').content, 10);

    likeButton.addEventListener('click', () => {
        onVideoMark(dislikeId); // DEBUG
    });
});

window.toggleReplyForm = toggleReplyForm;