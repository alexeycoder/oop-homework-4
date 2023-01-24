# ООП: Модель Сетевого Чата

## Изменения от 2023-01-24

* Мульти-модульная структура проекта теперь под управлением Maven для более корректной организации зависимостей между модулями.

* Переработан модуль `edu.alexey.oopchat.server` отвечающий за абстракцию сетевого взаимодействия.
	* Вместо передачи символьных строк используется передача посредством  сериализации/десериализации экземпляров масштабируемого класса `Message`.
	* Вместо класса соединения потребителям Серверу и Клиенту доступен аналогичный интерфейс.

## Архитектура решения

Решение простого сетевого чата состоит из трёх модулей, отвечающих за разные задачи:

* [edu.alexey.network](network/src/edu/alexey/network)
* [edu.alexey.oopchat.server](server/src/edu/alexey/oopchat/server)
* [edu.alexey.oopchat.client](client/src/edu/alexey/oopchat/client)

### [edu.alexey.network](network/src/edu/alexey/network)

Этот модуль является независимым от остальных двух.

Модуль выступает в роли абстракции сетевого взаимодействия. Так, его основной класс `class Connection` реализует одно TCP-соединение, инкапсулируя в себе низкоуровневую работу с сокетом (стандартный компонент Java `java.net.Socket`) и связанными потоками текстового ввода-вывода.

`class Connection` спроектирован так, что его потребителем могут быть как серверная, так и клиентская части.

API:

* `sendMessage(String message)` позволяет обмениваться текстовыми данными между двумя связанными соединениями.

* `authenticate(String yourId)` позволяет клиенту представится (идентифицировать себя) для сервера. Этот метод можно использовать только раз непосредственно после установления соединения с сервером.\
Сервер в дальнейшем может использовать Id, полученный при подключении, для подписания сообщений от данного пользователя чата.

* `disconnect()` разрывает соединение.

Прослушивая сообщения в отдельном потоке, экземпляр `Connection` генерирует различного рода события (установлено соединение, принято сообщение и т.п.). Для реализации такой событийной системы соединения, в которой свои обратные вызовы могут поставлять как сервер, так и клиенты, модулем network предоставляется интерфейс `interface ConnectionListener`:

* `onConnectionReady(Connection connection)` &mdash; Событие, возникающее по готовности соединения.

* `onAuthenticate(Connection connection, String subscriberId)` &mdash; Событие, возникающее когда клиент представляется серверу, непосредственно сразу после установления соединения.

* `onReceiveMessage(Connection connection, String message)` &mdash; Событие, возникающее когда слушатель принимает строку данных.

* `onDisconnect(Connection connection)` &mdash; Обрыв соединения.

* `onException(Connection connection, Exception ex)` &mdash; Исключительная ситуация (ошибка).


### [edu.alexey.oopchat.server](server/src/edu/alexey/oopchat/server)

Этот модуль зависит от [edu.alexey.network](network/src/edu/alexey/network).

`class ChatServer` &mdash; класс сервера, экземпляр которого может:
* принимать входящие соединения;
* держать несколько активных соединений;
* рассылать сообщения нескольким клиентам
Функционал класса базируется на стандартной компоненте Java `java.net.ServerSocket`, который умеет слушать входящие соединения, принимать соединение и создавать связанный с ним объект `Socket`.

`class ChatServer` реализует интерфейс `interface ConnectionListener`.

### [edu.alexey.oopchat.client](client/src/edu/alexey/oopchat/client)

Этот модуль зависит от [edu.alexey.network](network/src/edu/alexey/network).

`class ClientWindow` представляет собой простой десктопный клиент чата.

`class ClientWindow ` реализует интерфейс `interface ConnectionListener`.

## Примеры работы:

### Клиентская часть

![example-1-auth](https://user-images.githubusercontent.com/109767480/210041194-fe039688-515c-402d-a56b-47fe2f08e76f.png)

![example-2-client-a](https://user-images.githubusercontent.com/109767480/210041195-cade05be-de9f-478d-9540-6582d3831747.png)

![example-3-client-s](https://user-images.githubusercontent.com/109767480/210041197-87ccd0a1-7a39-4e6d-9f26-c8b9a197a47f.png)

### Серверная часть

![example-4-server-log](https://user-images.githubusercontent.com/109767480/210041200-fcb2106b-4947-4947-9ecf-403920fd80b8.png)
