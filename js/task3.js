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
					$('#function-def')[0].innerHTML = '<div class="problem-situation">Прототип функции: </div><pre><code id="editor" class="language-c">' + Prism.highlight(response.task.function_def, Prism.languages.c, 'c') + '</code></pre>';

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
	//TODO: придумать как будет работать подсказка в этой задачи			
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