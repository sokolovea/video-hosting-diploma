function createCard(item, toggleFn) {
    const card = document.createElement('div');
    card.classList.add('item-card', item.blocked ? 'blocked' : 'unblocked');
    card.innerHTML = `
        <span>
            <a href="${item.link}" target="_blank">${item.title || item.name || item.text || item.login}</a>
        </span>
        <div class="item-buttons">
            <button class="toggle-button">
                ${item.blocked ? 'Разблокировать' : 'Заблокировать'}
            </button>
        </div>
    `;

    const toggleButton = card.querySelector('.toggle-button');
    toggleButton.addEventListener('click', () => toggleFn(item.id, !item.blocked));

    return card;
}

async function fetchItems(url, containerId, allItems, toggleFn) {
    try {
        const response = await fetch(url);
        const items = await response.json();
        allItems.length = 0;
        allItems.push(...items);

        const container = document.getElementById(containerId);
        container.innerHTML = '';
        items.forEach(item => {
            const card = createCard(item, toggleFn);
            container.appendChild(card);
        });
    } catch (error) {
        console.error('Ошибка загрузки данных:', error);
    }
}

async function toggleItem(url, id, blocked, fetchFn) {
    try {
        const action = blocked ? 'block' : 'unblock';
        await fetch(`${url}/${action}`, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ id })
        });
        fetchFn();
    } catch (error) {
        console.error('Ошибка изменения состояния элемента:', error);
    }
}

// Пользователи
const allUsers = [];
const fetchUsers = () =>
    fetchItems(
        `${apiBase}/users`,
        'userContainer',
        allUsers,
        (id, blocked) => toggleItem(`${apiBase}/users`, id, blocked, fetchUsers)
    ).then(filterUsers);

// Видео
const allVideos = [];
const fetchVideos = () =>
    fetchItems(
        `${apiBase}/videos`,
        'videoContainer',
        allVideos,
        (id, blocked) => toggleItem(`${apiBase}/videos`, id, blocked, fetchVideos)
    ).then(filterVideos);

// Комментарии (если требуется аналогичная функциональность)
const fetchComments = () =>
    fetchItems(
        `${apiBase}/comments`,
        'commentContainer',
        [],
        (id, blocked) => toggleItem(`${apiBase}/comments`, id, blocked, fetchComments)
    );

// Функция фильтрации пользователей
function filterUsers() {
    const searchValue = document.getElementById('userSearch').value.toLowerCase();
    const container = document.getElementById('userContainer');
    container.innerHTML = '';

    allUsers
        .filter(user => user.login.toLowerCase().includes(searchValue))
        .forEach(user => {
            const card = createCard(user, (id, blocked) => toggleItem(`${apiBase}/users`, id, blocked, fetchUsers));
            container.appendChild(card);
        });
}

// Функция фильтрации видео
function filterVideos() {
    const searchValue = document.getElementById('videoSearch').value.toLowerCase();
    const container = document.getElementById('videoContainer');
    container.innerHTML = '';

    allVideos
        .filter(video => video.title.toLowerCase().includes(searchValue))
        .forEach(video => {
            const card = createCard(video, (id, blocked) => toggleItem(`${apiBase}/videos`, id, blocked, fetchVideos));
            container.appendChild(card);
        });
}

