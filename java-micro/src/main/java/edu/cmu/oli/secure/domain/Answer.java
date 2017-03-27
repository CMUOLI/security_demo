package edu.cmu.oli.secure.domain;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Raphael Gachuhi
 */
@Entity
@Table(name = "answer")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "Answer.findAll", query = "SELECT a FROM Answer a"),
        @NamedQuery(name = "Answer.findByGuid", query = "SELECT a FROM Answer a WHERE a.guid = :guid"),
        @NamedQuery(name = "Answer.findByUserId", query = "SELECT a FROM Answer a WHERE a.userId = :userId"),
        @NamedQuery(name = "Answer.findByQuestion", query = "SELECT a FROM Answer a WHERE a.question = :question")})
public class Answer implements Serializable {

    private static final long serialVersionUID = 1L;
    @Expose()
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(max = 32)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "guid")
    private String guid;

    @Expose()
    @NotNull
    @Size(max = 250)
    @Column(name = "user_id")
    private String userId;

    @Expose()
    @NotNull
    @JoinColumn(name = "question_guid", referencedColumnName = "guid")
    @ManyToOne
    private Question question;

    @Expose()
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;


    public Answer() {
        this.dateCreated = new Date();
        this.dateUpdated = (Date) dateCreated.clone();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer = (Answer) o;

        if (!guid.equals(answer.guid)) return false;
        if (userId != null ? !userId.equals(answer.userId) : answer.userId != null) return false;
        return question != null ? question.equals(answer.question) : answer.question == null;
    }

    @Override
    public int hashCode() {
        int result = guid.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (question != null ? question.hashCode() : 0);
        return result;
    }
}
