<h1>Сетевое хранилище файлов</h1>
<p>Клиент-серверное приложение</p>
<h3>Модули:</h3>
<ul>
    <li>
        <h4>Сервер</h4>
        <p>JDK 11 (zulu), Netty, MySql 8</p>
        <p>Основные настройки сервера хранятся в файле server.properties</p>
    </li>
    <li>
        <h4>Клиент</h4>
        <p>JDK 11 (zulu), Netty, Java FX</p>
        <p>После старта приложения, пользователю необходимо указать ip и порт сервера 
и пройти регистрацию или аутентификацию. Далее, пользователю будет доступен интерфейс для управления файлами и директориями в сетевом хранилище.
Каждому пользователю предоставляется отдельная директория
</p>
    </li>
    <li>
        <h4>Dictionary</h4>
        <p>JDK 11 (zulu)</p>
        <p>Утилитарный класс, содержащий словарь команд.</p>
    </li>
</ul>

<h4>Функции:</h4>
<ol>
    <li>Authenticate</li>
    <li>DB</li>
    <li>Remove</li>
    <li>Rename</li>
    <li>Copy</li>
    <li>Create (dir)</li>
    <li>Upload</li>
    <li>Download</li>
    <li>View size (dir, file)</li>
    <li>View creating and updating date</li>
    <li>Search</li>
    <li>Sort ( name | size | date )</li>
</ol>