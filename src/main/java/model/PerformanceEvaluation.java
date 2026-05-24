package model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "performance_evaluations")
public class PerformanceEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private PublicServer server;

    @Column(nullable = false)
    private LocalDate evaluationDate;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double score; // Puntuación (0-100)

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private RecognitionType recognition; // BEST_SERVER, NONE

    @Column(length = 500)
    private String comments;

    @Column(length = 255)
    private String evaluator; // Nombre de quien evaluó

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PublicServer getServer() { return server; }
    public void setServer(PublicServer server) { this.server = server; }

    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public RecognitionType getRecognition() { return recognition; }
    public void setRecognition(RecognitionType recognition) { this.recognition = recognition; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getEvaluator() { return evaluator; }
    public void setEvaluator(String evaluator) { this.evaluator = evaluator; }

    // Enum
    public enum RecognitionType {
        BEST_SERVER,
        NONE
    }
}
