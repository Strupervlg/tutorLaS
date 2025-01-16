// берём Express
var express = require('express');
const path = require('path');

// создаём Express-приложение
var app = express();

// Указываем папку для статических файлов (CSS, JS, изображения)
app.use(express.static(path.join(__dirname, 'app')));

// создаём маршрут для главной страницы
// http://localhost:8080/
app.get('/', function (req, res) {
	res.sendfile(path.join(__dirname, 'app', 'index.html'));
});

app.get('/task1', function (req, res) {
	res.sendfile(path.join(__dirname, 'app', 'index.html'));
});

app.get('/task2', function (req, res) {
	res.sendfile(path.join(__dirname, 'app', 'index2.html'));
});

app.get('/task3', function (req, res) {
	res.sendfile(path.join(__dirname, 'app', 'index3.html'));
});

// запускаем сервер на порту 8080
app.listen(8080);
// отправляем сообщение
console.log('Сервер стартовал!');