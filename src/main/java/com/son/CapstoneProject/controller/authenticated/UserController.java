package com.son.CapstoneProject.controller.authenticated;

import com.son.CapstoneProject.entity.Answer;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.repository.AnswerRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is for both admins and clients
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class UserController {

    // This repository is for users to add, update, and delete questions
    @Autowired
    private QuestionRepository questionRepository;

    // This repository is for users to add, update, and delete answers
    @Autowired
    private AnswerRepository answerRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    /**
     * Add a question
     * @param question
     * @return
     */
    @PostMapping(value = "/addQuestion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Question addQuestion(@RequestBody Question question) {
        return questionRepository.save(question);
    }

    /**
     * Update a question
     * @param updatedQuestion
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateQuestion/{id}")
    public ResponseEntity<Question> updateQuestion(@RequestBody Question updatedQuestion, @PathVariable Long id)
            throws Exception {
        Question oldQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));

        // Update values
        oldQuestion.setTitle(updatedQuestion.getTitle());
        oldQuestion.setContent(updatedQuestion.getContent());

        Question question = questionRepository.save(oldQuestion);
        return ResponseEntity.ok(question);
    }

    /**
     * Delete a question
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{id}")
    public Map<String, String> deleteQuestion(@PathVariable Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found to delete"));
        questionRepository.delete(question);
        Map<String, String> map = new HashMap<>();
        map.put("questionId", "" + id);
        map.put("deleted", "true");
        return map;
    }

    // ============================================================================//

    /**
     * Add answers
     * @param answer
     * @return
     */
    @PostMapping(value = "/addAnswer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Answer addAnswer(@RequestBody Answer answer) {
        return answerRepository.save(answer);
    }

    /**
     * Update answer
     * @param updatedQuestion
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateAnswer/{id}")
    public ResponseEntity<Answer> updateAnswer(@RequestBody Answer updatedQuestion, @PathVariable Long id)
            throws Exception {
        Answer oldAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
        oldAnswer.setContent(updatedQuestion.getContent());
        Answer answer = answerRepository.save(oldAnswer);
        return ResponseEntity.ok(answer);
    }

    /**
     * Delete answer
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteAnswer/{id}")
    public Map<String, String> deleteAnswer(@PathVariable Long id) throws Exception {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found to delete"));
        answerRepository.delete(answer);
        Map<String, String> map = new HashMap<>();
        map.put("answerId", "" + id);
        map.put("deleted", "true");
        return map;
    }
}
