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
		url: serverUrl + '/task-1/get-next',
		data: JSON.stringify(data),
		success: function (response) {
			if (response.taskId != -1) {
				$('#taskInTTL')[0].value = response.taskInTTL;
				$('#stepVar')[0].value = response.task.stepVar;
				$('#var')[0].value = response.task.var;
				$('#editor')[0].innerHTML = Prism.highlight(response.task.code, Prism.languages.c, 'c');
				$('#question')[0].innerHTML = response.task.question;
				$('#trace')[0].innerHTML = '';
				response.task.trace.forEach(element => {
					$('#trace')[0].innerHTML += `<li data-id="` + element.step + `">` + element.text + `</li>`;
				});
				$('#taskId')[0].value = response.taskId;
				updateTask();
				console.log(response);
			} else {
				$('#taskInTTL')[0].value = '';
				$('#stepVar')[0].value = '';
				$('#var')[0].value = '';
				$('#editor')[0].innerHTML = '';
				$('#question')[0].innerHTML = '';
				$('#trace')[0].innerHTML = '';
				$('#taskId')[0].value = '';
				$('.answers')[0].innerHTML = '';
				$('.container-btn')[0].innerHTML = '';
				$('#question')[0].innerHTML = "Все задачи выполнены.";
			}
		},
		error: function (xhr, status, error) {
			console.error('Ошибка запроса:', error);
		}
	});
}

function updateBtnComplete2() {
	$('#btn-complete2').on('click', function () {
		$('#tip-text')[0].innerHTML = "";
		getNextTask();
		$('#btn-complete2').remove();
		$('#btn-complete').removeClass('hidden');
		updateBtnComplete();
	});
}

function updateBtnComplete() {
	$('#btn-complete').on('click', function () {
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			stepVar: $('#stepVar')[0].value,
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
			url: serverUrl + '/task-1/complete-task',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				if (!response.result) {
					$('#error-text')[0].innerHTML = response.errorText;
					$('#tip-text')[0].innerHTML = "";

				} else if (response.result) {
					$('#error-text')[0].innerHTML = "";
					$('#tip-text')[0].innerHTML = "Задача выполнена!";
					$('#btn-complete').addClass('hidden');
					$('.container-btn')[0].innerHTML += '<button id="btn-complete2">Следующая задача</button>';
					updateBtnComplete2();
				}

			},
			error: function (xhr, status, error) {
				console.error('Ошибка запроса:', error);
			}
		});
	});
}

$(document).ready(function () {
	updateBtnComplete();
	if (getCookie('auth') == undefined) {
		auth();
	} else {
		getNextTask();
	}

});

function updateTask() {
	$('#btn-tip').on('click', function () {
		var steps = $.map($('li'), function (elementOrValue, indexOrKey) {
			if (!elementOrValue.classList.value.includes('correct')) {
				return elementOrValue.dataset.id;
			}
		});
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			stepVar: $('#stepVar')[0].value,
			var: $('#var')[0].value,
			steps: steps,
			taskInTTL: $('#taskInTTL')[0].value,
		};

		$.ajax({
			type: 'POST',
			headers: {
				'Accept': '*/*',
				'Content-Type': 'application/json',
				'Access-Control-Allow-Origin': '*'
			},
			dataType: "json",
			url: serverUrl + '/task-1/get-hint',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				if (response.step == "") {
					$('#tip-text')[0].innerHTML = "Вы выбрали все правильные варианты ответов, пожалуйста, завершите задание и перейдите к следующей задаче.";
					$('#error-text')[0].innerHTML = "";

				} else if (response.step != "") {
					$('[data-id="' + response.step + '"]').addClass("correct");
					$('#taskInTTL')[0].value = response.taskInTTL;
					$('#tip-text')[0].innerHTML = response.hint;
					$('#error-text')[0].innerHTML = "";
				}

			},
			error: function (xhr, status, error) {
				console.error('Ошибка запроса:', error);
			}
		});
	});
	$('.trace li').on('click', function () {
		var answer = $(this).attr('data-id');
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			step: answer,
			stepVar: $('#stepVar')[0].value,
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
			url: serverUrl + '/task-1/check-answer',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				if (!response.result && !$('[data-id="' + answer + '"]').hasClass("incorrect")) {
					$('[data-id="' + answer + '"]').addClass("incorrect");
					$('#error-text')[0].innerHTML = response.errorText;
					$('#tip-text')[0].innerHTML = "";
				} else if (response.result && !$('[data-id="' + answer + '"]').hasClass("correct")) {
					$('[data-id="' + answer + '"]').addClass("correct");
					$('#error-text')[0].innerHTML = "";
					$('#tip-text')[0].innerHTML = "";
				}
				$('#taskInTTL')[0].value = response.taskInTTL;
			},
			error: function (xhr, status, error) {
				console.error('Ошибка запроса:', error);
			}
		});
	});
}