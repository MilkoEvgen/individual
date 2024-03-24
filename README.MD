### Инструкция по запуску проекта:
1. Установить Java-21, Docker и Intellij Idea
2. Запустить Docker
3. Склонировать проект `git clone https://github.com/MilkoEvgen/data_manager.git`
4. Открыть проект в Intellij Idea, выполнить `mvn clean install`, после этого `docker-compose up -d`
5. Склонировать проект `git clone https://github.com/MilkoEvgen/individuals_api.git`
6. Добавить настройки из файла resources/settings-for-maven.txt в файл settings.xml (находится в папке m2), в раздел settings
7. Открыть проект в Intellij Idea, выполнить `mvn clean install`, после этого `docker-compose up -d`
---
### Для создания юзера:
1. В Postman отправить POST запрос на URL http://localhost:8086/api/v1/addresses с телом {
   "countryId": 1,
   "address":"134 Paddington St",
   "zipCode":"1101",
   "city":"Sydney",
   "state":"New South Wales"
   }
2. Скопировать id только что созданного адреса из ответа
3. Отправить POST запрос на URL http://localhost:8087/api/v1/auth/register с телом {
   "login": "11111",
   "password": "11111",
   "firstName":"Eugene",
   "lastName":"Milko",
   "addressId":"СЮДА ВСТАВИТЬ ID АДРЕСА",
   "passportNumber":"1100 000111",
   "phoneNumber":"123456789",
   "email":"milko@gmail.com"
   }