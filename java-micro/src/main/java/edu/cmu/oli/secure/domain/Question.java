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
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author Raphael Gachuhi
 */
@Entity
@Table(name = "question")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "Question.findAll", query = "SELECT a FROM Question a"),
        @NamedQuery(name = "Question.findByGuid", query = "SELECT a FROM Question a WHERE a.guid = :guid"),
        @NamedQuery(name = "Question.findById", query = "SELECT a FROM Question a WHERE a.id = :id"),
        @NamedQuery(name = "Question.findByPrompt", query = "SELECT a FROM Question a WHERE a.prompt = :prompt"),
        @NamedQuery(name = "Question.findByCreated", query = "SELECT a FROM Question a WHERE a.dateCreated = :dateCreated")})
public class Question implements Serializable {

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
    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Expose()
    @NotNull
    @Size(max = 250)
    @Column(name = "id")
    private String id;

    @Expose()
    @NotNull
    @Size(max = 250)
    @Column(name = "prompt")
    private String prompt;

    @Expose()
    @Size(max = 250)
    @Column(name = "author")
    private String author;

    @Expose()
    @Column(name = "published")
    private boolean published;

    @Expose()
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;

    @XmlTransient
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Answer> answers;

    @Expose()
    @JoinColumn(name = "course_section_guid", referencedColumnName = "guid")
    @ManyToOne
    private CourseSection courseSection;

    public Question() {
        this.dateCreated = new Date();
        this.dateUpdated = (Date) dateCreated.clone();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String description) {
        this.prompt = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Collection<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Collection<Answer> answers) {
        this.answers = answers;
    }

    public void addAnswer(Answer answer) {
        if (this.answers == null) {
            this.answers = new ArrayList<>();
        }
        this.answers.add(answer);
    }

    public CourseSection getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (guid != null ? !guid.equals(question.guid) : question.guid != null) return false;
        if (!id.equals(question.id)) return false;
        return author != null ? author.equals(question.author) : question.author == null;
    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + id.hashCode();
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "guid='" + guid + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
