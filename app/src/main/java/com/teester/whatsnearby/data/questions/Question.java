package com.teester.whatsnearby.data.questions;

import com.teester.whatsnearby.R;

public abstract class Question implements QuestionsContract {

	public int question = 0;
	public int drawable = R.drawable.ic_unsure;
	public int color = 0;
	public String tag = "";
	public String answer_yes = "yes";
	public String answer_no = "no";
	public String answer_unsure = "";

	@Override
	public int getQuestion() {
		return this.question;
	}

	@Override
	public int getDrawable() {
		return this.drawable;
	}

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public String getAnswerYes() {
		return answer_yes;
	}

	@Override
	public void setAnswerYes(String answer_yes) {
		this.answer_yes = answer_yes;
	}

	@Override
	public String getAnswerNo() {
		return answer_no;
	}

	@Override
	public void setAnswerNo(String answer_no) {
		this.answer_no = answer_no;
	}

	@Override
	public String getAnswerUnsure() {
		return answer_unsure;
	}

	@Override
	public void setAnswerUnsure(String answer_unsure) {
		this.answer_unsure = answer_unsure;
	}

	@Override
	public int getIcon() {
		return this.drawable;
	}

	@Override
	public int getColor() {
		return this.color;
	}

	@Override
	public String getAnswer(String response) {
		String answer;
		switch (response) {
			case "yes":
				answer = this.answer_yes;
				break;
			case "no":
				answer = this.answer_no;
				break;
			case "unsure":
				answer = this.answer_unsure;
				break;
			default:
				answer = "";
				break;
		}
		return answer;
	}

	@Override
	public int getAnswerInt(String response) {
		int answer;
		switch (response) {
			case "yes":
				answer = R.string.yes;
				break;
			case "no":
				answer = R.string.no;
				break;
			case "unsure":
				answer = R.string.unsure;
				break;
			default:
				answer = 0;
				break;
		}
		return answer;
	}

	@Override
	public String checkPreviousAnswer(String answer) {
		return getAnswer(answer);
	}
}
