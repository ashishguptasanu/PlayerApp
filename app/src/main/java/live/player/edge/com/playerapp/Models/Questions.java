package live.player.edge.com.playerapp.Models;

/**
 * Created by Ashish on 20-02-2018.
 */

public class Questions {
    public Questions(int questionId, String question, String option1, String option2, String option3) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.questionId = questionId;
    }
    public Questions(){}

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public int getQuestionId() {
        return questionId;
    }

    String question, option1, option2, option3;
    int questionId;
}
