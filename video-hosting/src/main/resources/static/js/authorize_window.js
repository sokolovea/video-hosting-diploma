// Функция для отображения всплывающего окна
export function showLoginPrompt() {
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.style.position = 'fixed';
    modal.style.top = '0';
    modal.style.left = '0';
    modal.style.width = '100vw';
    modal.style.height = '100vh';
    modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
    modal.style.display = 'flex';
    modal.style.alignItems = 'center';
    modal.style.justifyContent = 'center';
    modal.style.zIndex = '1000';

    // Создаем содержимое окна
    const content = document.createElement('div');
    content.style.backgroundColor = '#fff';
    content.style.padding = '20px';
    content.style.borderRadius = '8px';
    content.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
    content.style.textAlign = 'center';
    content.style.width = '80%';
    content.style.maxWidth = '400px';

    const message = document.createElement('p');
    message.textContent = 'Войдите или зарегистрируйтесь для доступа ко всем функциям.';
    message.style.fontSize = '16px';
    message.style.marginBottom = '20px';

    const buttonContainer = document.createElement('div');
    buttonContainer.style.display = 'flex';
    buttonContainer.style.justifyContent = 'space-between';

    const loginButton = document.createElement('button');
    loginButton.textContent = 'Войти';
    loginButton.style.padding = '10px 20px';
    loginButton.style.border = 'none';
    loginButton.style.borderRadius = '5px';
    loginButton.style.backgroundColor = '#007bff';
    loginButton.style.color = '#fff';
    loginButton.style.cursor = 'pointer';
    loginButton.addEventListener('click', () => {
        window.location.href = '/login';
    });

    const registerButton = document.createElement('button');
    registerButton.textContent = 'Зарегистрироваться';
    registerButton.style.padding = '10px 20px';
    registerButton.style.border = 'none';
    registerButton.style.borderRadius = '5px';
    registerButton.style.backgroundColor = '#28a745';
    registerButton.style.color = '#fff';
    registerButton.style.cursor = 'pointer';
    registerButton.addEventListener('click', () => {
        window.location.href = '/register';
    });

    // Закрытие модального окна
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            document.body.removeChild(modal);
        }
    });

    // Добавляем элементы в содержимое
    buttonContainer.appendChild(loginButton);
    buttonContainer.appendChild(registerButton);
    content.appendChild(message);
    content.appendChild(buttonContainer);
    modal.appendChild(content);

    // Добавляем модальное окно в DOM
    document.body.appendChild(modal);
}