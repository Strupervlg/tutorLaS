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

function getNextTask() {
	var data = {
		uid: getCookie('auth')
	};
	$.ajax({
		type: 'POST',
		headers: {
			'Accept': '*/*',
			'Content-Type': 'application/json',
			'Access-Control-Allow-Origin': '*'
		},
		dataType: "json",
		url: serverUrl + '/task-3/get-next',
		data: JSON.stringify(data),
		success: function (response) {
			if (response.taskId != -1) {
				$('#taskInTTL')[0].value = response.taskInTTL;
				$('#var')[0].value = response.task.var;
				$('#editor')[0].innerHTML = Prism.highlight(response.task.expression, Prism.languages.c, 'c');
				$('#question')[0].innerHTML = response.task.question;
				if (response.task.function_def != "") {
					$('#question')[0].innerHTML += " Прототип функции: <span class='mono'>" + response.task.function_def + "</span>";
				}
				$('#taskId')[0].value = response.taskId;
				updateTask();
				console.log(response);
			} else {
				$('#taskInTTL')[0].value = '';
				$('#var')[0].value = '';
				$('#editor')[0].innerHTML = '';
				$('#question')[0].innerHTML = '';
				$('#taskId')[0].value = '';
				$('#answer')[0].innerHTML = '';
				$('#question')[0].innerHTML = "Все задачи выполнены.";
			}
		},
		error: function (xhr, status, error) {
			console.error('Ошибка запроса:', error);
		}
	});
}

function updateBtnComplete2() {
	$('#answer')[0].innerHTML = '';
	$('.container-btn')[0].innerHTML += '<button id="btn-complete2">Следующая задача</button>';
	$('#btn-complete2').on('click', function () {
		$('#error-text')[0].innerHTML = "";
		$('#tip-text')[0].innerHTML = "";
		getNextTask();
		$('#btn-complete2').remove();
	});
}

$(document).ready(function () {
	if (getCookie('auth') == undefined) {
		auth();
	} else {
		getNextTask();
	}
});


function updateTask() {
	$('#answer')[0].innerHTML = `<div>
					<input type="radio" id="answerInput" name="answer" value="answerInput" checked />
					<label for="answerInput">Входная</label>
				</div>
				<div>
					<input type="radio" id="answerOutput" name="answer" value="answerOutput" />
					<label for="answerOutput">Выходная</label>
				</div>
				<div>
					<input type="radio" id="answerMutable" name="answer" value="answerMutable" />
					<label for="answerMutable">Изменяемая</label>
				</div>
				<div class="container-btn">
					<button id="btn-confirm">Принять</button>
				</div>`;
	// $('#btn-tip').on('click', function () {
	// 	var steps = $.map($('li'), function (elementOrValue, indexOrKey) {
	// 		if (!elementOrValue.classList.value.includes('correct')) {
	// 			return elementOrValue.dataset.id;
	// 		}
	// 	});
	// 	var data = {
	// 		uid: getCookie('auth'),
	// 		taskId: $('#taskId')[0].value,
	// 		stepVar: $('#stepVar')[0].value,
	// 		var: $('#var')[0].value,
	// 		steps: steps,
	// 		taskInTTL: $('#taskInTTL')[0].value,
	// 	};

	// 	$.ajax({
	// 		type: 'POST',
	// 		headers: {
	// 			'Accept': '*/*',
	// 			'Content-Type': 'application/json',
	// 			'Access-Control-Allow-Origin': '*'
	// 		},
	// 		dataType: "json",
	// 		url: serverUrl + '/task-3/get-hint',
	// 		data: JSON.stringify(data),
	// 		success: function (response) {
	// 			console.log(response);
	// 			if (response.step == "") {
	// 				$('#tip-text')[0].innerHTML = "Вы выбрали все правильные варианты ответов, пожалуйста, завершите задание и перейдите к следующей задаче.";
	// 				$('#error-text')[0].innerHTML = "";

	// 			} else if (response.step != "") {
	// 				$('[data-id="' + response.step + '"]').addClass("correct");
	// 				$('#taskInTTL')[0].value = response.taskInTTL;
	// 				$('#tip-text')[0].innerHTML = "Следующая правильная строка - номер " + response.step.replace("step", "");
	// 				$('#error-text')[0].innerHTML = "";
	// 			}

	// 		},
	// 		error: function (xhr, status, error) {
	// 			console.error('Ошибка запроса:', error);
	// 		}
	// 	});
	// });

	$('#btn-confirm').on('click', function () {

		var answer = $('input[name="answer"]:checked').val();
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			answer: answer,
			var: $('#var')[0].value,
			taskInTTL: $('#taskInTTL')[0].value,
		};

		console.log(data);

		$.ajax({
			type: 'POST',
			headers: {
				'Accept': '*/*',
				'Content-Type': 'application/json',
				'Access-Control-Allow-Origin': '*'
			},
			dataType: "json",
			url: serverUrl + '/task-3/check-answer',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				if (!response.result) {
					$('#error-text')[0].innerHTML = response.errorText;
					updateBtnComplete2();
				} else if (response.result) {
					$('#tip-text')[0].innerHTML = "Задача выполнена!";
					updateBtnComplete2();
				}
			},
			error: function (xhr, status, error) {
				console.error('Ошибка запроса:', error);
			}
		});
	});
}