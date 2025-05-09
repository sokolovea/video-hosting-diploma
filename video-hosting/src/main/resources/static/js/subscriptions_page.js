document.addEventListener('DOMContentLoaded', function () {
    document.querySelector('.history-table').addEventListener('submit', async function (event) {
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