nextTask = false;
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
		url: serverUrl + '/task-2/get-next',
		data: JSON.stringify(data),
		success: function (response) {
			if (response.taskId != -1) {
				$('#taskInTTL')[0].value = response.taskInTTL;
				$('#prefix')[0].value = response.task.prefix;
				$('#var')[0].value = response.task.var;
				$('#question')[0].innerHTML = response.task.question;
				$('#code')[0].innerHTML = '';
				response.task.code.forEach(element => {
					$('#code')[0].innerHTML += `<li data-id="` + element.line + `">` + element.text + `</li>`;
				});
				$('#taskId')[0].value = response.taskId;
				updateTask();
				console.log(response);
			} else {
				$('#taskInTTL')[0].value = '';
				$('#prefix')[0].value = '';
				$('#var')[0].value = '';
				$('#question')[0].innerHTML = '';
				$('#code')[0].innerHTML = '';
				$('#taskId')[0].value = '';
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
		nextTask = false;
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
			prefix: $('#prefix')[0].value,
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
			url: serverUrl + '/task-2/complete-task',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				for (let index = 0; index < $('.selected').length; index++) {
					$('.selected')[index].classList.value = $('.selected')[index].classList.value.replace('incorrect', '');
					$('.selected')[index].classList.value = $('.selected')[index].classList.value.replace('correct', '');
				}
				let incorrectsComplete = $('.incorrect-complete');
				for (let index = 0; index < incorrectsComplete.length; index++) {
					incorrectsComplete[index].classList.value = incorrectsComplete[index].classList.value.replace('incorrect-complete', '');
				}
				if (!response.result) {
					$('#error-text')[0].innerHTML = response.errorText;
					$('#tip-text')[0].innerHTML = "";
					response.lines.forEach(element => {
						$('[data-id="' + element + '"]').addClass("incorrect-complete");
					});
				} else if (response.result) {
					$('#error-text')[0].innerHTML = "";
					$('#tip-text')[0].innerHTML = "Задача выполнена!";
					$('#btn-complete').addClass('hidden');
					$('.container-btn')[0].innerHTML += '<button id="btn-complete2">Следующая задача</button>';
					nextTask = true;
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
		if (nextTask) {
			return;
		}
		var lines = $.map($('li'), function (elementOrValue, indexOrKey) {
			if (!elementOrValue.classList.value.includes('selected') && elementOrValue.dataset.id != "") {
				return elementOrValue.dataset.id;
			}
		});
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			prefix: $('#prefix')[0].value,
			var: $('#var')[0].value,
			lines: lines,
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
			url: serverUrl + '/task-2/get-hint',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				for (let index = 0; index < $('.selected').length; index++) {
					$('.selected')[index].classList.value = $('.selected')[index].classList.value.replace('incorrect', '');
					$('.selected')[index].classList.value = $('.selected')[index].classList.value.replace('correct', '');
				}
				let incorrectsComplete = $('.incorrect-complete');
				for (let index = 0; index < incorrectsComplete.length; index++) {
					incorrectsComplete[index].classList.value = incorrectsComplete[index].classList.value.replace('incorrect-complete', '');
				}
				if (response.line == "") {
					$('#tip-text')[0].innerHTML = response.hint;
					$('#error-text')[0].innerHTML = "";

				} else if (response.line != "") {
					$('[data-id="' + response.line + '"]').addClass("correct").addClass('selected');
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
	$('.code li[data-id!=""]').on('click', function () {
		if (nextTask) {
			return;
		}
		var answer = $(this).attr('data-id');
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			usageLine: answer,
			prefix: $('#prefix')[0].value,
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
			url: serverUrl + '/task-2/check-answer',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				for (let index = 0; index < $('.selected').length; index++) {
					$('.selected')[index].classList.value = $('.selected')[index].classList.value.replace('incorrect', '');
					$('.selected')[index].classList.value = $('.selected')[index].classList.value.replace('correct', '');
				}
				let incorrectsComplete = $('.incorrect-complete');
				for (let index = 0; index < incorrectsComplete.length; index++) {
					incorrectsComplete[index].classList.value = incorrectsComplete[index].classList.value.replace('incorrect-complete', '');
				}
				if (!response.result && !$('[data-id="' + answer + '"]').hasClass("selected")) {
					$('[data-id="' + answer + '"]').addClass("incorrect").addClass('selected');
					$('#error-text')[0].innerHTML = response.errorText;
					$('#tip-text')[0].innerHTML = "";
				} else if (response.result && !$('[data-id="' + answer + '"]').hasClass("selected")) {
					$('[data-id="' + answer + '"]').addClass("correct").addClass('selected');
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