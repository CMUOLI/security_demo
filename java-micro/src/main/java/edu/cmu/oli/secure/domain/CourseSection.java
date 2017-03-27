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
@Table(name = "course_section", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"admit_code"})})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "CourseSection.findAll", query = "SELECT a FROM CourseSection a"),
        @NamedQuery(name = "CourseSection.findByGuid", query = "SELECT a FROM CourseSection a WHERE a.guid = :guid"),
        @NamedQuery(name = "CourseSection.findByAdmitCode", query = "SELECT a FROM CourseSection a WHERE a.admitCode = :admitCode"),
        @NamedQuery(name = "CourseSection.findByTitle", query = "SELECT a FROM CourseSection a WHERE a.title = :title"),
        @NamedQuery(name = "CourseSection.findByCreated", query = "SELECT a FROM CourseSection a WHERE a.dateCreated = :dateCreated")})
public class CourseSection implements Serializable {

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
    @Size(max = 50)
    @Column(name = "admit_code")
    private String admitCode;

    @Expose()
    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Expose()
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;

    @XmlTransient
    @OneToMany(mappedBy = "courseSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Question> questions;

    @XmlTransient
    @OneToMany(mappedBy = "courseSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Registration> registrations;

    public CourseSection() {
        this.dateCreated = new Date();
        this.dateUpdated = (Date) dateCreated.clone();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getAdmitCode() {
        return admitCode;
    }

    public void setAdmitCode(String admitCode) {
        this.admitCode = admitCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Collection<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Collection<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question){
        if(this.questions == null){
            this.questions = new ArrayList<>();
        }
        this.questions.add(question);
    }

    public Collection<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(Collection<Registration> registrations) {
        this.registrations = registrations;
    }

    public void addRegistration(Registration registration){
        if(this.registrations == null){
            this.registrations = new ArrayList<>();
        }
        this.registrations.add(registration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseSection that = (CourseSection) o;

        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        return admitCode.equals(that.admitCode);
    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + admitCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CourseSection{" +
                "guid='" + guid + '\'' +
                ", admitCode='" + admitCode + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
