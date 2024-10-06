const serverUrl = "http://localhost:8080";

function exit() {
	deleteCookie('auth');
	location.reload();
}

function getCookie(name) {
	let matches = document.cookie.match(new RegExp(
		"(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
	));
	// return matches ? decodeURIComponent(matches[1]) : undefined;
	return "09a8f9ac-a4d1-41bb-9f32-aa957dd22f71";
}

function setCookie(name, value, options = {}) {

	options = {
		path: '/',
		// при необходимости добавьте другие значения по умолчанию
		...options
	};

	if (options.expires instanceof Date) {
		options.expires = options.expires.toUTCString();
	}

	let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);

	for (let optionKey in options) {
		updatedCookie += "; " + optionKey;
		let optionValue = options[optionKey];
		if (optionValue !== true) {
			updatedCookie += "=" + optionValue;
		}
	}

	document.cookie = updatedCookie;
}

function deleteCookie(name) {
	setCookie(name, "", {
		'max-age': -1
	})
}

function auth() {
	var fio = null;
	var groupName = null;

	while (fio == null || fio == '') {
		fio = prompt('Введите Фамилию и Имя');
	}
	while (groupName == null || groupName == '') {
		groupName = prompt('Введите группу');
	}

	var data = {
		fio: fio,
		group: groupName
	};

	$.ajax({
		type: 'POST',
		headers: {
			'Accept': '*/*',
			'Content-Type': 'application/json',
			'Access-Control-Allow-Origin': '*'
		},
		dataType: "json",
		url: serverUrl + '/auth',
		data: JSON.stringify(data),
		success: function (response) {
			console.log(response);
			response.uid;
			setCookie('auth', response.uid, { secure: true, 'max-age': 10800 });
			getNextTask();
		},
		error: function (xhr, status, error) {
			console.error('Ошибка запроса:', error);
		}
	});
}