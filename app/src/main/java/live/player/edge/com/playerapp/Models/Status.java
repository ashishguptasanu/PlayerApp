package live.player.edge.com.playerapp.Models;

/**
 * Created by Ashish on 21-02-2018.
 */

public class Status {
    public String questionId, question_status, question_answer;

    public Status(String questionId, String question_status, String question_answer) {
        this.questionId = questionId;
        this.question_status = question_status;
        this.question_answer = question_answer;
    }
    public Status(){

    }
}
