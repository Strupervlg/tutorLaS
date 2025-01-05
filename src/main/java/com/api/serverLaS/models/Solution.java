package com.api.serverLaS.models;

public class Solution {
    private User user;

    private Task task;

    private int countOfCorrect;

    private int countOfMistakes;

    private int countOfHints;

    private int id;

    public Solution(int id, User user, Task task, int countOfCorrect, int countOfMistakes, int countOfHints) {
        this.id = id;
        this.user = user;
        this.task = task;
        this.countOfCorrect = countOfCorrect;
        this.countOfMistakes = countOfMistakes;
        this.countOfHints = countOfHints;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getCountOfCorrect() {
        return countOfCorrect;
    }

    public void setCountOfCorrect(int countOfCorrect) {
        this.countOfCorrect = countOfCorrect;
    }

    public int getCountOfMistakes() {
        return countOfMistakes;
    }

    public void setCountOfMistakes(int countOfMistakes) {
        this.countOfMistakes = countOfMistakes;
    }

    public int getCountOfHints() {
        return countOfHints;
    }

    public void setCountOfHints(int countOfHints) {
        this.countOfHints = countOfHints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
