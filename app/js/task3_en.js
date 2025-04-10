nextTask = false;
function getNextTask() {
	let answers = `<select name="tempVar">
					<option value="">Not stated</option>
					<option value="answerInput">Input</option>
					<option value="answerOutput">Output</option>
					<option value="answerMutable">Mutable</option>
				</select>`;

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
		url: serverUrl + '/en/task-3/get-next',
		data: JSON.stringify(data),
		success: function (response) {
			if (response.taskId != -1) {
				$('#taskInTTL')[0].value = response.taskInTTL;
				$('#editor')[0].innerHTML = Prism.highlight(response.task.expression, Prism.languages.c, 'c');
				$('#question')[0].innerHTML = response.task.question;
				if (response.task.function_def != "") {
					$('#function-def')[0].innerHTML = '<div class="problem-situation">Function prototype: </div><pre><code id="editor-func" class="language-c">'
						+ Prism.highlight(response.task.function_def, Prism.languages.cpp, 'cpp') + '</code></pre>';
				} else {
					$('#function-def')[0].innerHTML = "";
				}
				$('#taskId')[0].value = response.taskId;
				$('#answer')[0].innerHTML = "";
				for (let i = 0; i < response.task.vars.length; i++) {
					$('#answer')[0].innerHTML += '<div>Variable ' + response.task.vars[i].name + '</div>';
					$('#answer')[0].innerHTML += answers.replaceAll('tempVar', response.task.vars[i].object);
				}
				updateTask();
				console.log(response);
			} else {
				$('#taskInTTL')[0].value = '';
				$('#editor')[0].innerHTML = '';
				$('#question')[0].innerHTML = '';
				$('#function-def')[0].innerHTML = "";
				$('.problem-situation')[1].innerHTML = '';
				$('.expression')[0].innerHTML = '';
				$('.container-btn')[0].innerHTML = '';
				$('#taskId')[0].value = '';
				$('#answer')[0].innerHTML = '';
				$('#question')[0].innerHTML = "All tasks completed.";
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
	});
}

$(document).ready(function () {
	if (getCookie('auth') == undefined) {
		auth();
	} else {
		getNextTask();
	}
});

function checkCompletedTask() {
	var allCorrect = true;
	$('#answer select').each(function () {
		if (!$(this).hasClass('correct')) {
			allCorrect = false;
			return false;
		}
	});

	if (allCorrect) {
		$('.container-btn')[0].innerHTML += '<button id="btn-complete2">Get next task</button>';
		nextTask = true;
		updateBtnComplete2();
	}
}

function updateTask() {
	$('#btn-tip').on('click', function () {
		if (nextTask) {
			return;
		}
		var answers = $("#answer select").map(function () {
			return {
				var: $(this).attr('name'),
				answer: $(this).val()
			};
		}).get();

		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			answers: answers,
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
			url: serverUrl + '/task-3/get-hint',
			data: JSON.stringify(data),
			success: function (response) {
				console.log(response);
				if (response.hint == "") {
					$('#tip-text')[0].innerHTML = "You have selected all the correct answer options. Please complete the task and proceed to the next one.";
					$('#error-text')[0].innerHTML = "";
				} else if (response.hint != "") {
					$('#tip-text')[0].innerHTML = response.hint;
					$('#error-text')[0].innerHTML = "";
				}

			},
			error: function (xhr, status, error) {
				console.error('Ошибка запроса:', error);
			}
		});
	});

	onSelectChange();
}

function onSelectChange() {
	$('select').change(function () {
		if (nextTask) {
			return;
		}
		console.log($(this).val())
		if ($(this).val() == "") {
			$('#error-text')[0].innerHTML = "";
			$('#tip-text')[0].innerHTML = "";
			return;
		}
		var data = {
			uid: getCookie('auth'),
			taskId: $('#taskId')[0].value,
			answers: [{ var: $(this).attr('name'), answer: $(this).val() }],
			taskInTTL: $('#taskInTTL')[0].value,
		};
		let thisSelect = $(this);
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
					thisSelect.removeClass('correct');
					thisSelect.addClass('incorrect');
					$('#error-text')[0].innerHTML = response.errorText;
					$('#tip-text')[0].innerHTML = "";
				} else if (response.result) {
					thisSelect.removeClass('incorrect');
					thisSelect.addClass('correct');
					$('#error-text')[0].innerHTML = "";
					$('#tip-text')[0].innerHTML = "";
				}
				checkCompletedTask();
			},
			error: function (xhr, status, error) {
				console.error('Ошибка запроса:', error);
			}
		});

	});
}